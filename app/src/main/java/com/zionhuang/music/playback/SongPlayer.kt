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
import android.support.v4.media.MediaMetadataCompat.*
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import android.util.Pair
import androidx.core.net.toUri
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.createWithNotificationChannel
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.ResolvingDataSource
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.QUEUE_ALL_SONG
import com.zionhuang.music.constants.MediaConstants.QUEUE_DESC
import com.zionhuang.music.constants.MediaConstants.QUEUE_ORDER
import com.zionhuang.music.constants.MediaConstants.QUEUE_TYPE
import com.zionhuang.music.constants.MediaConstants.SONG
import com.zionhuang.music.constants.MediaConstants.SONG_ID
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_ADD_TO_LIBRARY
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.*
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.utils.GlideApp
import com.zionhuang.music.youtube.YouTubeExtractor
import com.zionhuang.music.youtube.models.YouTubeStream
import com.zionhuang.music.youtube.models.YtFormat
import com.zionhuang.music.youtube.newpipe.SearchCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias OnNotificationPosted = (notificationId: Int, notification: Notification, ongoing: Boolean) -> Unit

/**
 * A wrapper around [ExoPlayer]
 */
class SongPlayer(private val context: Context, private val scope: CoroutineScope) {

    private val songRepository = SongRepository(context)
    private val youTubeExtractor = YouTubeExtractor.getInstance(context)

    private val _mediaSession = MediaSessionCompat(context, context.getString(R.string.app_name)).apply {
        isActive = true
    }
    val mediaSession: MediaSessionCompat get() = _mediaSession

    val player: SimpleExoPlayer = SimpleExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(ResolvingDataSource.Factory(DefaultDataSourceFactory(context)) { dataSpec ->
                val id = dataSpec.uri.host
                        ?: throw IllegalArgumentException("Cannot find media id from uri host")
                if (dataSpec.uri.getQueryParameter("fromLocal") == "1") {
                    return@Factory dataSpec.withUri(context.getAudioFile(id).toUri())
                }
                val stream: YtFormat? = (YouTubeExtractor.getInstance(context).extractStreamBlocking(id) as? YouTubeStream.Success)?.formats?.maxByOrNull {
                    it.abr ?: 0
                }
                return@Factory if (stream != null) dataSpec.withUri(stream.url!!.toUri()) else dataSpec
            }))
            .build()
            .apply {
                val audioAttributes = AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.CONTENT_TYPE_MUSIC)
                        .build()
                setAudioAttributes(audioAttributes, true)
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

            override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
                scope.launch {
                    when (extras!!.getInt(QUEUE_TYPE)) {
                        QUEUE_ALL_SONG -> {
                            val items = songRepository.getAllSongsList(extras.getInt(QUEUE_ORDER), extras.getBoolean(QUEUE_DESC)).toMediaItems(context)
                            player.setMediaItems(items)
                            player.seekToDefaultPosition(items.indexOfFirst { it.mediaId == mediaId })
                        }
                        else -> {
                            player.setMediaItem(extras.getParcelable<SongParcel>(SONG)?.toMediaItem()
                                    ?: mediaId.toMediaItem())

                        }
                    }
                    player.prepare()
                    player.playWhenReady = playWhenReady
                }
            }

            override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
                val mediaId = extras!!.getString(SONG_ID)
                        ?: return setCustomErrorMessage("Media id not found.", ERROR_CODE_UNKNOWN_ERROR)
                val items = SearchCache[query]?.toMediaItems()
                        ?: return setCustomErrorMessage("Search items not found.", ERROR_CODE_UNKNOWN_ERROR)
                player.run {
                    setMediaItems(items)
                    seekToDefaultPosition(items.indexOfFirst { it.mediaId == mediaId })
                    prepare()
                    this.playWhenReady = playWhenReady
                }
            }


            override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {
                val id = youTubeExtractor.extractId(uri.toString())
                        ?: return setCustomErrorMessage("Can't extract video id from the url.", ERROR_CODE_UNKNOWN_ERROR)
                player.setMediaItem(id.toMediaItem())
                player.prepare()
                player.playWhenReady = playWhenReady
            }
        })
        setCustomActionProviders(context.createCustomAction(ACTION_ADD_TO_LIBRARY, R.string.add_to_library, R.drawable.ic_library_add) { _, _, _, _ ->
            scope.launch {
                player.currentMetadata?.let {
                    songRepository.insert(Song(
                            id = it.id,
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