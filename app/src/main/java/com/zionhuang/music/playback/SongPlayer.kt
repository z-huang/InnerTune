package com.zionhuang.music.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
import android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import android.util.Log
import androidx.core.net.toUri
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.createWithNotificationChannel
import com.google.android.exoplayer2.ui.PlayerView
import com.zionhuang.music.R
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.download.DownloadService
import com.zionhuang.music.download.DownloadService.Companion.DOWNLOAD_MUSIC_INTENT
import com.zionhuang.music.download.DownloadTask
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADED
import com.zionhuang.music.extractor.YouTubeExtractor
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.playback.queue.AllSongsQueue
import com.zionhuang.music.playback.queue.EmptyQueue.Companion.EMPTY_QUEUE
import com.zionhuang.music.playback.queue.Queue
import com.zionhuang.music.playback.queue.Queue.Companion.QUEUE_ALL_SONG
import com.zionhuang.music.playback.queue.Queue.Companion.QUEUE_SINGLE
import com.zionhuang.music.playback.queue.SingleSongQueue
import com.zionhuang.music.utils.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

/**
 * A wrapper around [MusicPlayer] to support actions from [MediaSessionCallback]
 */

class SongPlayer(private val context: Context, private val scope: CoroutineScope) {
    companion object {
        const val TAG = "SongPlayer"
        const val CHANNEL_ID = "music_channel_01"
        const val NOTIFICATION_ID = 888
    }

    private val songRepository = SongRepository(context)
    private val youTubeExtractor = YouTubeExtractor.getInstance(context)
    private val musicPlayer = MusicPlayer(context)

    private var metadataBuilder = MediaMetadataCompat.Builder()
    private var stateBuilder = PlaybackStateCompat.Builder().setActions(
            ACTION_PLAY
                    or ACTION_PAUSE
                    or ACTION_PLAY_PAUSE
                    or ACTION_SKIP_TO_NEXT
                    or ACTION_SKIP_TO_PREVIOUS
                    or ACTION_SEEK_TO)
            .setState(STATE_NONE, 0, 1f)

    private val _mediaSession = MediaSessionCompat(context, context.getString(R.string.app_name)).apply {
        setFlags(FLAG_HANDLES_MEDIA_BUTTONS or FLAG_HANDLES_TRANSPORT_CONTROLS)
        setCallback(MediaSessionCallback(this, this@SongPlayer))
        setPlaybackState(stateBuilder.build())
        isActive = true
    }
    val mediaSession: MediaSessionCompat
        get() = _mediaSession

    private val playerNotificationManager = createWithNotificationChannel(context, CHANNEL_ID, R.string.channel_name_playback, 0, NOTIFICATION_ID, object : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence = currentSong?.title ?: ""
        override fun getCurrentContentText(player: Player): CharSequence? = currentSong?.artist
        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? = null
        override fun createCurrentContentIntent(player: Player): PendingIntent? = null
    }).apply {
        setPlayer(musicPlayer.exoPlayer)
        setMediaSessionToken(mediaSession.sessionToken)
    }

    private var queue: Queue = EMPTY_QUEUE

    private val currentSong: SongEntity?
        get() = queue.currentSong

    private val autoDownload = PreferenceHelper(context, R.string.auto_download, false)

    init {
        musicPlayer.onDurationSet { duration ->
            currentSong?.duration = (duration / 1000).toInt()
            mediaSession.setMetadata(metadataBuilder.apply {
                putString(METADATA_KEY_TITLE, currentSong?.title)
                putString(METADATA_KEY_ARTIST, currentSong?.artist)
                putLong(METADATA_KEY_DURATION, duration)
            }.build())
        }
        musicPlayer.onPlaybackStateChanged { playbackState ->
            val state = when (playbackState) {
                Player.STATE_IDLE -> STATE_NONE
                Player.STATE_BUFFERING -> STATE_BUFFERING
                Player.STATE_READY -> if (musicPlayer.isPlaying) STATE_PLAYING else STATE_PAUSED
                Player.STATE_ENDED -> {
                    addToLibrary()
                    playNext()
                    STATE_BUFFERING
                }
                else -> STATE_NONE
            }
            updatePlaybackState {
                setState(state, musicPlayer.position, musicPlayer.playbackSpeed)
            }
        }
    }

    private var job: Job? = null

    fun playSong() {
        musicPlayer.stop()
        currentSong?.let { song ->
            job?.cancel()
            job = scope.launch extractScope@{
                if (song.downloadState == STATE_DOWNLOADED) {
                    musicPlayer.setSource(File("${context.getExternalFilesDir(null)?.absolutePath}/audio", song.id).toUri())
                    return@extractScope
                }
                when (val result = youTubeExtractor.extract(currentSong!!.id)) {
                    is YouTubeExtractor.Result.Success -> {
                        mediaSession.setMetadata(metadataBuilder.apply {
                            putString(METADATA_KEY_TITLE, result.title)
                            putString(METADATA_KEY_ARTIST, result.channelTitle)
                            putLong(METADATA_KEY_DURATION, result.duration * 1000.toLong())
                        }.build())
                        result.formats.maxByOrNull { it.abr ?: 0 }?.let { format ->
                            Log.d(TAG, "Song url: ${format.url}")
                            musicPlayer.setSource(Uri.parse(format.url))
                            if (autoDownload.value) {
                                context.startService(Intent(context, DownloadService::class.java).apply {
                                    action = DOWNLOAD_MUSIC_INTENT
                                    putExtra("task", DownloadTask(
                                            id = result.id,
                                            songTitle = result.title,
                                            url = format.url!!
                                    ))
                                })
                            }
                        }
                        songRepository.insert(SongEntity(
                                id = result.id,
                                title = result.title,
                                artist = result.channelTitle,
                                duration = result.duration
                        ))
                    }
                    is YouTubeExtractor.Result.Error -> {
                        Log.d(TAG, """${result.errorCode}: ${result.errorMessage}""")
                    }
                }
            }
        }
    }

    fun play() = musicPlayer.play()

    fun pause() {
        musicPlayer.pause()
    }

    fun seekTo(pos: Long) {
        musicPlayer.seekTo(pos)
        updatePlaybackState {
            setState(mediaSession.controller.playbackState.state, pos, musicPlayer.playbackSpeed)
        }
    }

    fun playNext() {
        queue.playNext()
        playSong()
    }

    fun playPrevious() {
        queue.playPrevious()
        playSong()
    }

    fun stop() {
        musicPlayer.stop()
        updatePlaybackState {
            setState(STATE_NONE, 0, musicPlayer.playbackSpeed)
        }
    }

    fun setQueue(queueType: Int, currentSongId: String) {
        queue = when (queueType) {
            QUEUE_ALL_SONG -> AllSongsQueue(songRepository, scope)
            QUEUE_SINGLE -> SingleSongQueue(songRepository, currentSongId)
            else -> EMPTY_QUEUE
        }.apply {
            this.currentSongId = currentSongId
        }
    }

    fun updateSongMeta(id: String, song: SongParcel) = queue.updateSongMeta(id, song)

    fun addToLibrary() {
        scope.launch {
            currentSong?.let {
                songRepository.insert(it)
            }
        }
    }

    fun toggleLike() {
    }

    private fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit) {
        applier(stateBuilder)
        setPlaybackState(stateBuilder.build())
    }

    private fun setPlaybackState(state: PlaybackStateCompat) = mediaSession.setPlaybackState(state)

    fun release() {
        mediaSession.apply {
            isActive = false
            release()
        }
        playerNotificationManager.setPlayer(null)
        musicPlayer.release()
    }

    fun setPlayerView(playerView: PlayerView?) = musicPlayer.setPlayerView(playerView)
}