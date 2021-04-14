package com.zionhuang.music.playback

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat.*
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import android.util.Log
import android.util.Pair
import androidx.core.net.toUri
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.createWithNotificationChannel
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.ResolvingDataSource
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.FROM_LOCAL
import com.zionhuang.music.constants.MediaConstants.EXTRA_ARTIST_ID
import com.zionhuang.music.constants.MediaConstants.EXTRA_LINK_HANDLER
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUERY_STRING
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_DESC
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_ORDER
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_TYPE
import com.zionhuang.music.constants.MediaConstants.EXTRA_SONG
import com.zionhuang.music.constants.MediaConstants.EXTRA_SONG_ID
import com.zionhuang.music.constants.MediaConstants.QUEUE_ALL_SONG
import com.zionhuang.music.constants.MediaConstants.QUEUE_ARTIST
import com.zionhuang.music.constants.MediaConstants.QUEUE_SEARCH
import com.zionhuang.music.constants.MediaConstants.QUEUE_SINGLE
import com.zionhuang.music.constants.MediaConstants.QUEUE_YT_PLAYLIST
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_ADD_TO_LIBRARY
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_SEEK_TO_QUEUE_ITEM
import com.zionhuang.music.constants.MediaSessionConstants.EXTRA_MEDIA_ID
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.*
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.utils.GlideApp
import com.zionhuang.music.youtube.YouTubeExtractor
import com.zionhuang.music.youtube.newpipe.ExtractorHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler
import org.schabi.newpipe.extractor.stream.StreamInfo
import kotlin.system.measureTimeMillis

typealias OnNotificationPosted = (notificationId: Int, notification: Notification, ongoing: Boolean) -> Unit

/**
 * A wrapper around [ExoPlayer]
 */
class SongPlayer(private val context: Context, private val scope: CoroutineScope) : Player.EventListener {

    private val songRepository = SongRepository(context)
    private val youTubeExtractor = YouTubeExtractor.getInstance(context)
    private val playlistData = PlaylistData()

    private val _mediaSession = MediaSessionCompat(context, context.getString(R.string.app_name)).apply {
        isActive = true
    }
    val mediaSession: MediaSessionCompat get() = _mediaSession

    val player: SimpleExoPlayer = SimpleExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(ResolvingDataSource.Factory(DefaultDataSourceFactory(context)) { dataSpec ->
                val id = dataSpec.uri.host
                        ?: throw IllegalArgumentException("Cannot find media id from uri host")
                if (dataSpec.uri.getQueryParameter(FROM_LOCAL) == "1") {
                    return@Factory dataSpec.withUri(context.getAudioFile(id).toUri())
                }
                val streamInfo: StreamInfo
                val duration = measureTimeMillis {
                    streamInfo = runBlocking {
                        ExtractorHelper.getStreamInfo(id)
                    }
                }
                Log.d(TAG, "Extract duration: ${duration}ms")
                val uri = streamInfo.audioStreams.maxByOrNull { it.bitrate }?.url?.toUri()

//                val stream: YouTubeStream.Success
//                val duration = measureTimeMillis {
//                    stream = YouTubeExtractor.getInstance(context).extractStreamBlocking(id) as YouTubeStream.Success
//                }
//                Log.d(TAG, "Extract duration: ${duration}ms")
//                val uri = stream.formats.maxByOrNull { it.abr ?: 0 }?.url?.toUri()
                if (uri != null) dataSpec.withUri(uri) else dataSpec
            }))
            .build()
            .apply {
                addListener(this@SongPlayer)
                val audioAttributes = AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.CONTENT_TYPE_MUSIC)
                        .build()
                setAudioAttributes(audioAttributes, true)
            }

    private fun playMedia(mediaId: String, playWhenReady: Boolean, extras: Bundle) {
        scope.launch {
            playlistData.queueType = extras.getInt(EXTRA_QUEUE_TYPE)
            when (playlistData.queueType) {
                QUEUE_ALL_SONG -> {
                    val items = songRepository.getAllSongsList(extras.getInt(EXTRA_QUEUE_ORDER), extras.getBoolean(EXTRA_QUEUE_DESC)).toMediaItems(context)
                    player.setMediaItems(items)
                    items.indexOfFirst { it.mediaId == mediaId }.takeIf { it != -1 }?.let { index ->
                        player.seekToDefaultPosition(index)
                    }
                }
                QUEUE_ARTIST -> {
                    val items = songRepository.getArtistSongsList(extras.getInt(EXTRA_ARTIST_ID), extras.getInt(EXTRA_QUEUE_ORDER), extras.getBoolean(EXTRA_QUEUE_DESC)).toMediaItems(context)
                    player.setMediaItems(items)
                    items.indexOfFirst { it.mediaId == mediaId }.takeIf { it != -1 }?.let { index ->
                        player.seekToDefaultPosition(index)
                    }
                }
                QUEUE_SEARCH -> {
                    val queryHandler = extras.getSerializable(EXTRA_LINK_HANDLER) as SearchQueryHandler
                    val initialItems = ExtractorHelper.search(queryHandler)
                    var nextPage: Page? = initialItems.nextPage

                    player.clearMediaItems()

                    var idx = -1
                    initialItems.relatedItems.toMediaItems().let { items ->
                        val lastItemCount = player.mediaItemCount
                        player.addMediaItems(items)
                        idx = items.indexOfFirst { it.mediaId == mediaId }
                        if (idx != -1) {
                            player.seekToDefaultPosition(lastItemCount + idx)
                        }
                    }
                    while (idx == -1 && nextPage != null) {
                        val infoItemsPage = ExtractorHelper.search(queryHandler, nextPage)
                        nextPage = infoItemsPage.nextPage
                        infoItemsPage.items.toMediaItems().let { items ->
                            val lastItemCount = player.mediaItemCount
                            player.addMediaItems(items)
                            idx = items.indexOfFirst { it.mediaId == mediaId }
                            if (idx != -1) {
                                player.seekToDefaultPosition(lastItemCount + idx)
                            }
                        }
                    }
                    if (idx == -1) {
                        return@launch mediaSessionConnector.setCustomErrorMessage("Search items not found.", ERROR_CODE_UNKNOWN_ERROR)
                    }
                    playlistData.linkHandler = queryHandler
                    playlistData.nextPage = nextPage
                }
                QUEUE_YT_PLAYLIST -> {
                    val linkHandler = extras.getSerializable(EXTRA_LINK_HANDLER) as ListLinkHandler
                    val initialItems = ExtractorHelper.getPlaylist(linkHandler.url)
                    var nextPage: Page? = initialItems.nextPage

                    player.clearMediaItems()

                    var idx = -1
                    initialItems.relatedItems.toMediaItems().let { items ->
                        val lastItemCount = player.mediaItemCount
                        player.addMediaItems(items)
                        idx = items.indexOfFirst { it.mediaId == mediaId }
                        if (idx != -1) {
                            player.seekToDefaultPosition(lastItemCount + idx)
                        }
                    }
                    while (idx == -1 && nextPage != null) {
                        val infoItemsPage = ExtractorHelper.getPlaylist(linkHandler.url, nextPage)
                        nextPage = infoItemsPage.nextPage
                        infoItemsPage.items.toMediaItems().let { items ->
                            val lastItemCount = player.mediaItemCount
                            player.addMediaItems(items)
                            idx = items.indexOfFirst { it.mediaId == mediaId }
                            if (idx != -1) {
                                player.seekToDefaultPosition(lastItemCount + idx)
                            }
                        }
                    }
                    if (idx == -1) {
                        return@launch mediaSessionConnector.setCustomErrorMessage("Search items not found.", ERROR_CODE_UNKNOWN_ERROR)
                    }
                    playlistData.linkHandler = linkHandler
                    playlistData.nextPage = nextPage
                }
                QUEUE_SINGLE -> {
                    player.setMediaItem(extras.getParcelable<SongParcel>(EXTRA_SONG)?.toMediaItem()
                            ?: mediaId.toMediaItem())
                }
            }
            player.prepare()
            player.playWhenReady = playWhenReady
        }
    }

    private val mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
        setPlayer(player)
        setPlaybackPreparer(object : MediaSessionConnector.PlaybackPreparer {
            override fun onCommand(player: Player, controlDispatcher: ControlDispatcher, command: String, extras: Bundle?, cb: ResultReceiver?) = false

            override fun getSupportedPrepareActions() =
                    ACTION_PREPARE_FROM_MEDIA_ID or ACTION_PREPARE_FROM_SEARCH or ACTION_PREPARE_FROM_URI or
                            ACTION_PLAY_FROM_MEDIA_ID or ACTION_PLAY_FROM_SEARCH or ACTION_PLAY_FROM_URI

            override fun onPrepare(playWhenReady: Boolean) {
                player.playWhenReady = playWhenReady
                player.prepare()
            }

            override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) =
                    playMedia(mediaId, playWhenReady, extras!!)

            override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
                val mediaId = extras?.getString(EXTRA_SONG_ID)
                        ?: return setCustomErrorMessage("Media id not found.", ERROR_CODE_UNKNOWN_ERROR)
                playMedia(mediaId, playWhenReady, extras.apply {
                    putInt(EXTRA_QUEUE_TYPE, QUEUE_SEARCH)
                    putString(EXTRA_QUERY_STRING, query)
                })
            }

            override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {
                val mediaId = youTubeExtractor.extractId(uri.toString())
                        ?: return setCustomErrorMessage("Can't extract video id from the url.", ERROR_CODE_UNKNOWN_ERROR)
                playMedia(mediaId, playWhenReady, extras!!.apply {
                    putInt(EXTRA_QUEUE_TYPE, QUEUE_SINGLE)
                })
            }
        })
        registerCustomCommandReceiver { player, _, command, extras, _ ->
            if (command == COMMAND_SEEK_TO_QUEUE_ITEM) {
                val mediaId = extras?.getString(EXTRA_MEDIA_ID)
                        ?: return@registerCustomCommandReceiver true
                player.mediaItemIndexOf(mediaId)?.let {
                    player.seekToDefaultPosition(it)
                }
                return@registerCustomCommandReceiver true
            }
            return@registerCustomCommandReceiver false
        }
        setCustomActionProviders(context.createCustomAction(ACTION_ADD_TO_LIBRARY, R.string.custom_action_add_to_library, R.drawable.ic_library_add) { _, _, _, _ ->
            scope.launch {
                player.currentMetadata?.let {
                    songRepository.insert(Song(
                            songId = it.id,
                            title = it.title,
                            artistName = it.artist ?: "",
                            duration = (player.duration / 1000).toInt()
                    ), it.artwork)
                    if (autoDownload) {
                        //downloadCurrentSong()
                    }
                }
            }
        })
        setQueueNavigator { player, windowIndex -> player.getMediaItemAt(windowIndex).metadata.toMediaDescription() }
        setErrorMessageProvider { e ->
            return@setErrorMessageProvider Pair(ERROR_CODE_UNKNOWN_ERROR, e.localizedMessage)
        }
        setQueueEditor(object : MediaSessionConnector.QueueEditor {
            override fun onCommand(player: Player, controlDispatcher: ControlDispatcher, command: String, extras: Bundle?, cb: ResultReceiver?): Boolean {
                if (COMMAND_MOVE_QUEUE_ITEM != command || extras == null) {
                    return false
                }
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

    private var onNotificationPosted: OnNotificationPosted = { _, _, _ -> }

    private val playerNotificationManager = createWithNotificationChannel(context, CHANNEL_ID, R.string.channel_name_playback, 0, NOTIFICATION_ID, object : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence = player.currentMetadata?.title.orEmpty()

        override fun getCurrentContentText(player: Player): CharSequence? = player.currentMetadata?.artist

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
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                callback.onBitmap(resource)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) = Unit
                        })
            }
            return bitmap
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? =
                PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0)
    }, object : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) =
                this@SongPlayer.onNotificationPosted(notificationId, notification, ongoing)
    }).apply {
        setPlayer(player)
        setMediaSessionToken(mediaSession.sessionToken)
        setSmallIcon(R.drawable.ic_notification)
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        if (reason == MEDIA_ITEM_TRANSITION_REASON_REPEAT || !playlistData.hasMoreItems) return
        if (player.mediaItemCount - player.currentWindowIndex > 5) return
        scope.launch {
            when (playlistData.queueType) {
                QUEUE_SEARCH -> {
                    if (playlistData.linkHandler != null && playlistData.nextPage != null) {
                        val searchInfo = ExtractorHelper.search(playlistData.linkHandler as SearchQueryHandler, playlistData.nextPage!!)
                        playlistData.nextPage = searchInfo.nextPage
                        player.addMediaItems(searchInfo.items.toMediaItems())
                    }
                }
                QUEUE_YT_PLAYLIST -> {
                    if (playlistData.linkHandler != null && playlistData.nextPage != null) {
                        val playlistInfo = ExtractorHelper.getPlaylist(playlistData.linkHandler!!.url, playlistData.nextPage!!)
                        playlistData.nextPage = playlistInfo.nextPage
                        player.addMediaItems(playlistInfo.items.toMediaItems())
                    }
                }
            }
        }
    }

    private var autoDownload by context.preference(R.string.pref_auto_download, false)
    private var autoAddSong by context.preference(R.string.pref_auto_add_song, true)

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

    fun onNotificationPosted(block: OnNotificationPosted) {
        onNotificationPosted = block
    }

    companion object {
        const val TAG = "SongPlayer"
        const val CHANNEL_ID = "music_channel_01"
        const val NOTIFICATION_ID = 888
    }
}