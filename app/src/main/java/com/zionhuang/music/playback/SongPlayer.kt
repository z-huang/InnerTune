package com.zionhuang.music.playback

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import android.util.Pair
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.ResolvingDataSource
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_DATA
import com.zionhuang.music.constants.MediaConstants.EXTRA_SONGS
import com.zionhuang.music.constants.MediaConstants.EXTRA_SONG_ID
import com.zionhuang.music.constants.MediaConstants.QUEUE_YT_SEARCH
import com.zionhuang.music.constants.MediaConstants.QUEUE_YT_SINGLE
import com.zionhuang.music.constants.MediaConstants.STATE_DOWNLOADED
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_ADD_TO_LIBRARY
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_ADD_TO_QUEUE
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_PLAY_NEXT
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_SEEK_TO_QUEUE_ITEM
import com.zionhuang.music.constants.MediaSessionConstants.EXTRA_MEDIA_ID
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.*
import com.zionhuang.music.models.MediaData
import com.zionhuang.music.models.QueueData
import com.zionhuang.music.models.toMediaDescription
import com.zionhuang.music.playback.queues.EmptyQueue
import com.zionhuang.music.playback.queues.Queue
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.repos.YouTubeRepository
import com.zionhuang.music.repos.base.LocalRepository
import com.zionhuang.music.repos.base.RemoteRepository
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.utils.GlideApp
import com.zionhuang.music.utils.logTimeMillis
import com.zionhuang.music.youtube.NewPipeYouTubeHelper
import com.zionhuang.music.youtube.StreamHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * A wrapper around [ExoPlayer]
 */
class SongPlayer(
    private val context: Context,
    private val scope: CoroutineScope,
    notificationListener: PlayerNotificationManager.NotificationListener,
) : Player.Listener {
    private val localRepository: LocalRepository = SongRepository
    private val remoteRepository: RemoteRepository = YouTubeRepository
    private var currentQueue: Queue = EmptyQueue()

    private val _mediaSession = MediaSessionCompat(context, context.getString(R.string.app_name)).apply {
        isActive = true
    }
    val mediaSession: MediaSessionCompat get() = _mediaSession

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(ResolvingDataSource.Factory(
                DefaultDataSource.Factory(context)
            ) { dataSpec ->
                runBlocking {
                    val mediaId = dataSpec.uri.host ?: throw IllegalArgumentException("Cannot find media id from uri host")
                    if (localRepository.getSongById(mediaId)?.downloadState == STATE_DOWNLOADED) {
                        return@runBlocking dataSpec.withUri(localRepository.getSongFile(mediaId).toUri())
                    }
//                    val uri = kotlin.runCatching {
//                        runBlocking(Dispatchers.IO) {
//                            YouTube.player(mediaId)
//                        }
//                    }.mapCatching { playerResponse ->
//                        if (playerResponse.playabilityStatus.status != "OK") {
//                            throw PlaybackException(playerResponse.playabilityStatus.status, null, ERROR_CODE_REMOTE_ERROR)
//                        }
//                        playerResponse.streamingData?.adaptiveFormats
//                            ?.filter { it.isAudio }
//                            ?.maxByOrNull { it.bitrate }
//                            ?.url
//                            ?.toUri()
//                            ?: throw PlaybackException("No stream available", null, ERROR_CODE_NO_STREAM)
//                    }.getOrThrow()
                    val streamInfo = logTimeMillis(TAG, "Extractor duration: %d") {
                        runBlocking {
                            remoteRepository.getStream(mediaId)
                        }
                    }
                    val connectivityManager = context.getSystemService<ConnectivityManager>()!!
                    val stream = if (connectivityManager.isActiveNetworkMetered) {
                        StreamHelper.getMostCompactAudioStream(streamInfo.audioStreams)
                    } else {
                        StreamHelper.getHighestQualityAudioStream(streamInfo.audioStreams)
                    }
                    val uri = stream?.url?.toUri()
                    updateMediaData(mediaId) {
                        if (artwork == null || (artwork!!.startsWith("http") && artwork != streamInfo.thumbnailUrl)) {
                            artwork = streamInfo.thumbnailUrl
                            mediaSessionConnector.invalidateMediaSessionMetadata()
                        }
                    }
                    if (uri != null) dataSpec.withUri(uri) else dataSpec
                }
            })
        )
        .build()
        .apply {
            addListener(this@SongPlayer)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build()
            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)
        }

    private var autoAddSong by context.preference(R.string.pref_auto_add_song, true)

    private fun updateMediaData(mediaId: String, applier: MediaData.() -> Unit) {
        scope.launch(Dispatchers.Main) {
            (player.currentMediaItem.takeIf { it?.mediaId == mediaId } ?: player.findMediaItemById(mediaId))?.metadata?.let {
                applier(it)
            }
        }
    }

    private fun playMedia(mediaId: String?, playWhenReady: Boolean, queueData: QueueData) {
        scope.launch {
            currentQueue = queueData.toQueue()
            player.loadQueue(currentQueue, mediaId)
            player.prepare()
            player.playWhenReady = playWhenReady
        }
    }

    private val mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
        setPlayer(player)
        setPlaybackPreparer(object : MediaSessionConnector.PlaybackPreparer {
            override fun onCommand(player: Player, command: String, extras: Bundle?, cb: ResultReceiver?) = false

            override fun getSupportedPrepareActions() =
                ACTION_PREPARE_FROM_MEDIA_ID or ACTION_PREPARE_FROM_SEARCH or ACTION_PREPARE_FROM_URI or
                        ACTION_PLAY_FROM_MEDIA_ID or ACTION_PLAY_FROM_SEARCH or ACTION_PLAY_FROM_URI

            override fun onPrepare(playWhenReady: Boolean) {
                player.playWhenReady = playWhenReady
                player.prepare()
            }

            override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) =
                playMedia(mediaId, playWhenReady, extras!!.getParcelable(EXTRA_QUEUE_DATA)!!)

            override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
                val mediaId = extras?.getString(EXTRA_SONG_ID)
                playMedia(mediaId, playWhenReady, QueueData(QUEUE_YT_SEARCH, query))
            }

            override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {
                val mediaId = NewPipeYouTubeHelper.extractVideoId(uri.toString()) ?: return setCustomErrorMessage(
                    "Can't extract video id from the url.",
                    ERROR_CODE_UNKNOWN_ERROR
                )
                playMedia(mediaId, playWhenReady, QueueData(QUEUE_YT_SINGLE, mediaId))
            }
        })
        registerCustomCommandReceiver { player, command, extras, _ ->
            if (extras == null) return@registerCustomCommandReceiver false
            when (command) {
                COMMAND_SEEK_TO_QUEUE_ITEM -> {
                    val mediaId = extras.getString(EXTRA_MEDIA_ID)
                        ?: return@registerCustomCommandReceiver true
                    player.mediaItemIndexOf(mediaId)?.let {
                        player.seekToDefaultPosition(it)
                    }
                    true
                }
                COMMAND_PLAY_NEXT -> {
                    val songs = extras.getParcelableArray(EXTRA_SONGS)!!
                    player.addMediaItems(
                        (if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex) + 1,
                        songs.mapNotNull { (it as? MediaData)?.toMediaItem() }
                    )
                    player.prepare()
                    true
                }
                COMMAND_ADD_TO_QUEUE -> {
                    val songs = extras.getParcelableArray(EXTRA_SONGS)!!
                    player.addMediaItems(songs.mapNotNull { (it as? MediaData)?.toMediaItem() })
                    player.prepare()
                    true
                }
                else -> false
            }
        }
        setCustomActionProviders(context.createCustomAction(ACTION_ADD_TO_LIBRARY, R.string.custom_action_add_to_library, R.drawable.ic_library_add) { _, _, _ ->
            player.currentMetadata?.let {
                addToLibrary(it)
            }
        })
        setQueueNavigator { player, windowIndex -> player.getMediaItemAt(windowIndex).metadata.toMediaDescription() }
        setErrorMessageProvider { e ->
            return@setErrorMessageProvider Pair(ERROR_CODE_UNKNOWN_ERROR, e.localizedMessage)
        }
        setQueueEditor(object : MediaSessionConnector.QueueEditor {
            override fun onCommand(player: Player, command: String, extras: Bundle?, cb: ResultReceiver?): Boolean {
                if (COMMAND_MOVE_QUEUE_ITEM != command || extras == null) return false
                val from = extras.getInt(EXTRA_FROM_INDEX, C.INDEX_UNSET)
                val to = extras.getInt(EXTRA_TO_INDEX, C.INDEX_UNSET)
                if (from != C.INDEX_UNSET && to != C.INDEX_UNSET) {
                    player.moveMediaItem(from, to)
                }
                return true
            }

            override fun onAddQueueItem(player: Player, description: MediaDescriptionCompat) =
                player.addMediaItem(description.toMediaItem())

            override fun onAddQueueItem(player: Player, description: MediaDescriptionCompat, index: Int) =
                player.addMediaItem(index, description.toMediaItem())

            override fun onRemoveQueueItem(player: Player, description: MediaDescriptionCompat) {
                player.mediaItemIndexOf(description.mediaId)?.let { i ->
                    player.removeMediaItem(i)
                }
            }
        })
    }

    private val playerNotificationManager = PlayerNotificationManager.Builder(context, NOTIFICATION_ID, CHANNEL_ID)
        .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence =
                player.currentMetadata?.title.orEmpty()

            override fun getCurrentContentText(player: Player): CharSequence? =
                player.currentMetadata?.artist

            override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
                val url = player.currentMetadata?.artwork
                val bitmap = GlideApp.with(context)
                    .asBitmap()
                    .load(url)
                    .onlyRetrieveFromCache(true)
                    .getBlocking()
                if (bitmap == null) {
                    GlideApp.with(context)
                        .asBitmap()
                        .load(url)
                        .onlyRetrieveFromCache(false)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) = callback.onBitmap(resource)
                            override fun onLoadCleared(placeholder: Drawable?) = Unit
                        })
                }
                return bitmap
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? =
                PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), FLAG_IMMUTABLE)
        })
        .setChannelNameResourceId(R.string.channel_name_playback)
        .setNotificationListener(notificationListener)
        .build()
        .apply {
            setPlayer(player)
            setMediaSessionToken(mediaSession.sessionToken)
            setSmallIcon(R.drawable.ic_notification)
        }

    /**
     * Auto load more
     */
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        if (reason == MEDIA_ITEM_TRANSITION_REASON_REPEAT ||
            player.playbackState == STATE_IDLE ||
            player.mediaItemCount - player.currentMediaItemIndex > 5 ||
            !currentQueue.hasNextPage()
        ) return
        scope.launch {
            player.addMediaItems(currentQueue.nextPage())
        }
    }

    override fun onPositionDiscontinuity(oldPosition: PositionInfo, newPosition: PositionInfo, @Player.DiscontinuityReason reason: Int) {
        if (reason == DISCONTINUITY_REASON_AUTO_TRANSITION && autoAddSong) {
            oldPosition.mediaItem?.metadata?.let {
                addToLibrary(it)
            }
        }
    }

    override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
        if (playbackState == STATE_ENDED && autoAddSong) {
            player.currentMetadata?.let {
                addToLibrary(it)
            }
        }
    }

    private fun addToLibrary(mediaData: MediaData) {
        scope.launch {
            localRepository.addSong(Song(
                id = mediaData.id,
                title = mediaData.title,
                artistName = mediaData.artist,
                duration = if (player.duration != C.TIME_UNSET) (player.duration / 1000).toInt() else -1,
                artworkType = mediaData.artworkType
            ))
        }
    }

    fun release() {
        mediaSession.apply {
            isActive = false
            release()
        }
        mediaSessionConnector.setPlayer(null)
        playerNotificationManager.setPlayer(null)
        player.release()
    }

    fun setPlayerView(playerView: PlayerView?) {
        playerView?.player = player
    }

    init {
        context.getLifeCycleOwner()?.let { lifeCycleOwner ->
            // TODO
//            oldSongRepository.deletedSongs.observe(lifeCycleOwner) { deletedSongs ->
//                Log.d(TAG, deletedSongs.toString())
//                val deletedIds = deletedSongs.map { it.songId }
//                player.mediaItems.forEachIndexed { index, mediaItem ->
//                    if (mediaItem.mediaId in deletedIds) {
//                        player.removeMediaItem(index)
//                    }
//                }
//            }
        }
    }

    companion object {
        const val TAG = "SongPlayer"
        const val CHANNEL_ID = "music_channel_01"
        const val NOTIFICATION_ID = 888

        const val ERROR_CODE_NO_STREAM = 1000001
    }
}