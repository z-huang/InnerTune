package com.zionhuang.music.playback

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
import com.google.android.exoplayer2.Player.STATE_IDLE
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.ResolvingDataSource
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_ARTIST_ID
import com.zionhuang.music.constants.MediaConstants.EXTRA_LINK_HANDLER
import com.zionhuang.music.constants.MediaConstants.EXTRA_PLAYLIST_ID
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUERY_STRING
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_DESC
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_ORDER
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_TYPE
import com.zionhuang.music.constants.MediaConstants.EXTRA_SONGS
import com.zionhuang.music.constants.MediaConstants.EXTRA_SONG_ID
import com.zionhuang.music.constants.MediaConstants.QUEUE_ALL_SONG
import com.zionhuang.music.constants.MediaConstants.QUEUE_ARTIST
import com.zionhuang.music.constants.MediaConstants.QUEUE_PLAYLIST
import com.zionhuang.music.constants.MediaConstants.QUEUE_SEARCH
import com.zionhuang.music.constants.MediaConstants.QUEUE_SINGLE
import com.zionhuang.music.constants.MediaConstants.QUEUE_YT_CHANNEL
import com.zionhuang.music.constants.MediaConstants.QUEUE_YT_PLAYLIST
import com.zionhuang.music.constants.MediaConstants.STATE_DOWNLOADED
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_ADD_TO_LIBRARY
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_ADD_TO_QUEUE
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_PLAY_NEXT
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_SEEK_TO_QUEUE_ITEM
import com.zionhuang.music.constants.MediaSessionConstants.EXTRA_MEDIA_ID
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.*
import com.zionhuang.music.models.MediaData
import com.zionhuang.music.models.toMediaDescription
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.utils.GlideApp
import com.zionhuang.music.utils.downloadSong
import com.zionhuang.music.utils.logTimeMillis
import com.zionhuang.music.youtube.YouTubeExtractor
import com.zionhuang.music.youtube.newpipe.ExtractorHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler

/**
 * A wrapper around [ExoPlayer]
 */
class SongPlayer(
    private val context: Context,
    private val scope: CoroutineScope,
    notificationListener: PlayerNotificationManager.NotificationListener,
) : Player.Listener {

    private val songRepository = SongRepository(context)
    private val youTubeExtractor = YouTubeExtractor.getInstance(context)
    private val playlistData = PlaylistData()

    private val _mediaSession =
        MediaSessionCompat(context, context.getString(R.string.app_name)).apply {
            isActive = true
        }
    val mediaSession: MediaSessionCompat get() = _mediaSession

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(
                ResolvingDataSource.Factory(
                    DefaultDataSource.Factory(context)
                ) { dataSpec ->
                    val mediaId = dataSpec.uri.host
                        ?: throw IllegalArgumentException("Cannot find media id from uri host")
                    if (runBlocking { songRepository.getSongEntityById(mediaId)?.downloadState == STATE_DOWNLOADED })
                        return@Factory dataSpec.withUri(context.getAudioFile(mediaId).toUri())

                    val streamInfo = logTimeMillis(TAG, "Extractor duration: %d") {
                        runBlocking {
                            ExtractorHelper.getStreamInfo(mediaId)
                        }
                    }
                    val uri = streamInfo.audioStreams.maxByOrNull { it.bitrate }?.url?.toUri()
                    updateMediadata(mediaId) {
                        if (artwork == null || (artwork!!.startsWith("http") && artwork != streamInfo.thumbnailUrl)) {
                            artwork = streamInfo.thumbnailUrl
                            mediaSessionConnector.invalidateMediaSessionMetadata()
                        }
                    }
                    if (uri != null) dataSpec.withUri(uri) else dataSpec
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
        }

    private var autoDownload by context.preference(R.string.pref_auto_download, false)
    private var autoAddSong by context.preference(R.string.pref_auto_add_song, true)

    private fun updateMediadata(mediaId: String, applier: MediaData.() -> Unit) {
        scope.launch(Dispatchers.Main) {
            (player.currentMediaItem.takeIf { mediaId == mediaId }
                ?: player.findMediaItemById(mediaId))?.metadata?.let {
                applier(it)
            }
        }
    }

    private fun playMedia(mediaId: String, playWhenReady: Boolean, extras: Bundle) {
        scope.launch {
            playlistData.queueType = extras.getInt(EXTRA_QUEUE_TYPE)
            when (playlistData.queueType) {
                QUEUE_ALL_SONG -> {
                    val items = songRepository.getAllSongsList(
                        extras.getInt(EXTRA_QUEUE_ORDER),
                        extras.getBoolean(EXTRA_QUEUE_DESC)
                    ).toMediaItems(context)
                    player.setMediaItems(items)
                    items.indexOfFirst { it.mediaId == mediaId }.takeIf { it != -1 }?.let { index ->
                        player.seekToDefaultPosition(index)
                    }
                }
                QUEUE_ARTIST -> {
                    val items = songRepository.getArtistSongsList(
                        extras.getInt(EXTRA_ARTIST_ID),
                        extras.getInt(EXTRA_QUEUE_ORDER),
                        extras.getBoolean(EXTRA_QUEUE_DESC)
                    ).toMediaItems(context)
                    player.setMediaItems(items)
                    items.indexOfFirst { it.mediaId == mediaId }.takeIf { it != -1 }?.let { index ->
                        player.seekToDefaultPosition(index)
                    }
                }
                QUEUE_PLAYLIST -> {
                    val items =
                        songRepository.getPlaylistSongsList(extras.getInt(EXTRA_PLAYLIST_ID))
                            .toMediaItems(context)
                    player.setMediaItems(items)
                    items.indexOfFirst { it.mediaId == mediaId }.takeIf { it != -1 }?.let { index ->
                        player.seekToDefaultPosition(index)
                    }
                }
                QUEUE_SEARCH -> {
                    val queryHandler =
                        extras.getSerializable(EXTRA_LINK_HANDLER) as SearchQueryHandler
                    val initialInfo = ExtractorHelper.search(queryHandler)

                    player.clearMediaItems()
                    val res = player.loadItems(
                        mediaId,
                        initialInfo.relatedItems.toMediaItems(),
                        initialInfo.nextPage
                    ) { page ->
                        val info = ExtractorHelper.search(queryHandler, page)
                        kotlin.Pair(info.items.toMediaItems(), info.nextPage)
                    }
                    if (res.first) return@launch mediaSessionConnector.setCustomErrorMessage(
                        "Search items not found.",
                        ERROR_CODE_UNKNOWN_ERROR
                    )
                    playlistData.linkHandler = queryHandler
                    playlistData.nextPage = res.second
                }
                QUEUE_YT_PLAYLIST -> {
                    val linkHandler = extras.getSerializable(EXTRA_LINK_HANDLER) as ListLinkHandler
                    val initialInfo = ExtractorHelper.getPlaylist(linkHandler.url)

                    player.clearMediaItems()
                    val res = player.loadItems(
                        mediaId,
                        initialInfo.relatedItems.toMediaItems(),
                        initialInfo.nextPage
                    ) { page ->
                        val info = ExtractorHelper.getPlaylist(linkHandler.url, page)
                        kotlin.Pair(info.items.toMediaItems(), info.nextPage)
                    }
                    if (res.first) return@launch mediaSessionConnector.setCustomErrorMessage(
                        "Playlist items not found.",
                        ERROR_CODE_UNKNOWN_ERROR
                    )
                    playlistData.linkHandler = linkHandler
                    playlistData.nextPage = res.second
                }
                QUEUE_YT_CHANNEL -> {
                    val linkHandler = extras.getSerializable(EXTRA_LINK_HANDLER) as ListLinkHandler
                    val initialInfo = ExtractorHelper.getChannel(linkHandler.url)

                    player.clearMediaItems()
                    val res = player.loadItems(
                        mediaId,
                        initialInfo.relatedItems.toMediaItems(),
                        initialInfo.nextPage
                    ) { page ->
                        val info = ExtractorHelper.getChannel(linkHandler.url, page)
                        kotlin.Pair(info.items.toMediaItems(), info.nextPage)
                    }
                    if (res.first) return@launch mediaSessionConnector.setCustomErrorMessage(
                        "Channel items not found.",
                        ERROR_CODE_UNKNOWN_ERROR
                    )
                    playlistData.linkHandler = linkHandler
                    playlistData.nextPage = res.second
                }
            }
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

            override fun onPrepareFromMediaId(
                mediaId: String,
                playWhenReady: Boolean,
                extras: Bundle?
            ) =
                playMedia(mediaId, playWhenReady, extras!!)

            override fun onPrepareFromSearch(
                query: String,
                playWhenReady: Boolean,
                extras: Bundle?
            ) {
                val mediaId = extras?.getString(EXTRA_SONG_ID)
                    ?: return setCustomErrorMessage("Media id not found.", ERROR_CODE_UNKNOWN_ERROR)
                playMedia(mediaId, playWhenReady, extras.apply {
                    putInt(EXTRA_QUEUE_TYPE, QUEUE_SEARCH)
                    putString(EXTRA_QUERY_STRING, query)
                })
            }

            override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {
                val mediaId =
                    youTubeExtractor.extractId(uri.toString()) ?: return setCustomErrorMessage(
                        "Can't extract video id from the url.",
                        ERROR_CODE_UNKNOWN_ERROR
                    )
                playMedia(mediaId, playWhenReady, extras!!.apply {
                    putInt(EXTRA_QUEUE_TYPE, QUEUE_SINGLE)
                })
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
            scope.launch {
                player.currentMetadata?.let {
                    songRepository.insert(
                        Song(
                            songId = it.id,
                            title = it.title,
                            artistName = it.artist ?: "",
                            duration = if (player.duration != C.TIME_UNSET) (player.duration / 1000).toInt() else -1,
                            artworkType = it.artworkType
                        )
                    )
                    if (autoDownload) {
                        player.currentMetadata?.let { metadata ->
                            context.downloadSong(metadata.id, songRepository)
                        }
                    }
                }
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
                PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0)
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
            !playlistData.hasMoreItems ||
            player.mediaItemCount - player.currentMediaItemIndex > 5 ||
            playlistData.linkHandler == null ||
            playlistData.nextPage == null
        ) return
        scope.launch {
            when (playlistData.queueType) {
                QUEUE_SEARCH -> {
                    val searchInfo = ExtractorHelper.search(
                        playlistData.linkHandler as SearchQueryHandler,
                        playlistData.nextPage!!
                    )
                    playlistData.nextPage = searchInfo.nextPage
                    player.addMediaItems(searchInfo.items.toMediaItems())
                }
                QUEUE_YT_PLAYLIST -> {
                    val playlistInfo = ExtractorHelper.getPlaylist(
                        playlistData.linkHandler!!.url,
                        playlistData.nextPage!!
                    )
                    playlistData.nextPage = playlistInfo.nextPage
                    player.addMediaItems(playlistInfo.items.toMediaItems())
                }
                QUEUE_YT_CHANNEL -> {
                    val channelInfo = ExtractorHelper.getChannel(
                        playlistData.linkHandler!!.url,
                        playlistData.nextPage!!
                    )
                    playlistData.nextPage = channelInfo.nextPage
                    player.addMediaItems(channelInfo.items.toMediaItems())
                }
            }
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
            songRepository.deletedSongs.observe(lifeCycleOwner) { deletedSongs ->
                Log.d(TAG, deletedSongs.toString())
                val deletedIds = deletedSongs.map { it.songId }
                player.mediaItems.forEachIndexed { index, mediaItem ->
                    if (mediaItem.mediaId in deletedIds) {
                        player.removeMediaItem(index)
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "SongPlayer"
        const val CHANNEL_ID = "music_channel_01"
        const val NOTIFICATION_ID = 888
    }
}