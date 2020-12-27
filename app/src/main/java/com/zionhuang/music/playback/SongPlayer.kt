package com.zionhuang.music.playback

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
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
import com.zionhuang.music.db.entities.ChannelEntity
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.download.DownloadService
import com.zionhuang.music.download.DownloadService.Companion.ACTION_DOWNLOAD_MUSIC
import com.zionhuang.music.download.DownloadTask
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADED
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.playback.queue.AllSongsQueue
import com.zionhuang.music.playback.queue.EmptyQueue.Companion.EMPTY_QUEUE
import com.zionhuang.music.playback.queue.Queue
import com.zionhuang.music.playback.queue.Queue.Companion.QUEUE_ALL_SONG
import com.zionhuang.music.playback.queue.Queue.Companion.QUEUE_SINGLE
import com.zionhuang.music.playback.queue.SingleSongQueue
import com.zionhuang.music.utils.PreferenceHelper
import com.zionhuang.music.youtube.YouTubeExtractor
import com.zionhuang.music.youtube.models.YouTubeStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

typealias OnNotificationPosted = (notificationId: Int, notification: Notification, ongoing: Boolean) -> Unit

/**
 * A wrapper around [MusicPlayer] to support actions from [MediaSessionCallback]
 */

class SongPlayer(private val context: Context, private val scope: CoroutineScope) : AudioManager.OnAudioFocusChangeListener {
    companion object {
        const val TAG = "SongPlayer"
        const val CHANNEL_ID = "music_channel_01"
        const val NOTIFICATION_ID = 888
    }

    private val songRepository = SongRepository(context)
    private val youTubeExtractor = YouTubeExtractor.getInstance(context)
    private val musicPlayer = MusicPlayer(context)

    private val audioManager: AudioManager
    private val focusRequest: AudioFocusRequest

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
        setCallback(MediaSessionCallback(context, this@SongPlayer, scope))
        setPlaybackState(stateBuilder.build())
        isActive = true
    }
    val mediaSession: MediaSessionCompat
        get() = _mediaSession

    private var onNotificationPosted: OnNotificationPosted = { _, _, _ -> }

    private val playerNotificationManager = createWithNotificationChannel(
            context,
            CHANNEL_ID,
            R.string.channel_name_playback,
            0,
            NOTIFICATION_ID,
            object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence = currentSong?.title
                        ?: "No Song"

                override fun getCurrentContentText(player: Player): CharSequence? = currentSong?.artistName
                override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? = null
                override fun createCurrentContentIntent(player: Player): PendingIntent? = null
            },
            object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                    this@SongPlayer.onNotificationPosted(notificationId, notification, ongoing)
                }
            }
    ).apply {
        setPlayer(musicPlayer.exoPlayer)
        setMediaSessionToken(mediaSession.sessionToken)
    }

    private var queue: Queue = EMPTY_QUEUE

    private val currentSong: Song?
        get() = queue.currentSong

    private val autoDownload = PreferenceHelper(context, R.string.auto_download, false)

    init {
        musicPlayer.onDurationSet { duration ->
            currentSong?.duration = (duration / 1000).toInt()
            mediaSession.setMetadata(metadataBuilder.apply {
                putString(METADATA_KEY_TITLE, currentSong?.title)
                putString(METADATA_KEY_ARTIST, currentSong?.artistName)
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

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
            setAudioAttributes(AudioAttributes.Builder().run {
                setUsage(AudioAttributes.USAGE_MEDIA)
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                build()
            })
            setAcceptsDelayedFocusGain(true)
            setOnAudioFocusChangeListener(this@SongPlayer, Handler(Looper.getMainLooper()))
            build()
        }
    }

    private var job: Job? = null

    fun playSong() {
        musicPlayer.stop()
        currentSong?.let { song ->
            job?.cancel()
            job = scope.launch extractScope@{
                audioManager.requestAudioFocus(focusRequest)
                if (song.downloadState == STATE_DOWNLOADED) {
                    musicPlayer.setSource(File("${context.getExternalFilesDir(null)?.absolutePath}/audio", song.id).toUri())
                    return@extractScope
                }
                when (val result = youTubeExtractor.extractStream(currentSong!!.id)) {
                    is YouTubeStream.Success -> {
                        mediaSession.setMetadata(metadataBuilder.apply {
                            putString(METADATA_KEY_TITLE, result.title)
                            putString(METADATA_KEY_ARTIST, result.channelTitle)
                            putLong(METADATA_KEY_DURATION, result.duration * 1000.toLong())
                        }.build())
                        if (!songRepository.hasSong(result.id)) {
                            if (!songRepository.hasChannel(result.channelId)) {
                                songRepository.insertChannel(ChannelEntity(id = result.channelId, name = result.channelTitle))
                            }
                            songRepository.insert(SongEntity(
                                    id = result.id,
                                    title = result.title,
                                    // use channel title as artist name temporarily
                                    artistId = songRepository.getArtistIdByName(result.channelTitle)
                                            ?: songRepository.insertArtist(result.channelTitle).toInt(),
                                    channelId = result.channelId,
                                    duration = result.duration
                            ))
                        }
                        result.formats.maxByOrNull { it.abr ?: 0 }?.let { format ->
                            Log.d(TAG, "Song url: ${format.url}")
                            musicPlayer.setSource(Uri.parse(format.url))
                            if (autoDownload.value) {
                                context.startService(Intent(context, DownloadService::class.java).apply {
                                    action = ACTION_DOWNLOAD_MUSIC
                                    putExtra("task", DownloadTask(
                                            id = result.id,
                                            title = result.title,
                                            url = format.url!!
                                    ))
                                })
                            }
                        }
                    }
                    is YouTubeStream.Error -> {
                        Log.d(TAG, """${result.errorCode}: ${result.errorMessage}""")
                    }
                }
            }
        }
    }

    fun play() = musicPlayer.play()

    fun pause() = musicPlayer.pause()

    fun seekTo(pos: Long) {
        musicPlayer.seekTo(pos)
        updatePlaybackState {
            setState(mediaSession.controller.playbackState.state, pos, musicPlayer.playbackSpeed)
        }
    }

    var volume: Float
        get() = musicPlayer.volume
        set(value) {
            musicPlayer.volume = value
        }

    fun fastForward() = musicPlayer.fastForward()
    fun rewind() = musicPlayer.rewind()

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
        scope.launch {
            currentSong?.let {
                songRepository.insert(it)
                songRepository.toggleLike(it.id)
            }
        }
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

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> volume = 0.5F
            AudioManager.AUDIOFOCUS_GAIN -> {
                volume = 1F
                play()
            }
        }
    }

    fun onNotificationPosted(block: OnNotificationPosted) {
        onNotificationPosted = block
    }
}