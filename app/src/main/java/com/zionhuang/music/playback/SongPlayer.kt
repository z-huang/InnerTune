package com.zionhuang.music.playback

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.audiofx.AudioEffect
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import android.util.Pair
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.util.component1
import androidx.core.util.component2
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.C.WAKE_MODE_NETWORK
import com.google.android.exoplayer2.PlaybackException.*
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.Player.State
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.analytics.PlaybackStats
import com.google.android.exoplayer2.analytics.PlaybackStatsListener
import com.google.android.exoplayer2.audio.*
import com.google.android.exoplayer2.audio.DefaultAudioSink.*
import com.google.android.exoplayer2.audio.SilenceSkippingAudioProcessor.DEFAULT_SILENCE_THRESHOLD_LEVEL
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor.*
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ShuffleOrder.DefaultShuffleOrder
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.CustomActionReceiver
import com.google.android.exoplayer2.ui.PlayerNotificationManager.EXTRA_INSTANCE_ID
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.ResolvingDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.*
import com.zionhuang.innertube.models.QueueAddEndpoint.Companion.INSERT_AFTER_CURRENT_VIDEO
import com.zionhuang.innertube.models.QueueAddEndpoint.Companion.INSERT_AT_END
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.ACTION_SHOW_BOTTOM_SHEET
import com.zionhuang.music.constants.Constants.DOWNLOADED_PLAYLIST_ID
import com.zionhuang.music.constants.Constants.LIKED_PLAYLIST_ID
import com.zionhuang.music.constants.MediaConstants.EXTRA_MEDIA_METADATA_ITEMS
import com.zionhuang.music.constants.MediaConstants.STATE_DOWNLOADED
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_ADD_TO_LIBRARY
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_LIKE
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_REMOVE_FROM_LIBRARY
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_TOGGLE_LIBRARY
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_TOGGLE_LIKE
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_TOGGLE_SHUFFLE
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_UNLIKE
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_ADD_TO_QUEUE
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_PLAY_NEXT
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_SEEK_TO_QUEUE_ITEM
import com.zionhuang.music.constants.MediaSessionConstants.EXTRA_QUEUE_INDEX
import com.zionhuang.music.db.entities.FormatEntity
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.*
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.sortInfo.SongSortInfoPreference
import com.zionhuang.music.playback.MusicService.Companion.ALBUM
import com.zionhuang.music.playback.MusicService.Companion.ARTIST
import com.zionhuang.music.playback.MusicService.Companion.PLAYLIST
import com.zionhuang.music.playback.MusicService.Companion.SONG
import com.zionhuang.music.playback.queues.EmptyQueue
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.playback.queues.Queue
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.bindings.resizeThumbnailUrl
import com.zionhuang.music.ui.fragments.settings.StorageSettingsFragment.Companion.VALUE_TO_MB
import com.zionhuang.music.utils.InfoCache
import com.zionhuang.music.utils.lyrics.LyricsHelper
import com.zionhuang.music.utils.preference.enumPreference
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * A wrapper around [ExoPlayer]
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SongPlayer(
    private val context: Context,
    private val scope: CoroutineScope,
    notificationListener: PlayerNotificationManager.NotificationListener,
) : Listener, PlaybackStatsListener.Callback {
    private val songRepository = SongRepository(context)
    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!
    private val bitmapProvider = BitmapProvider(context)

    private var autoAddSong by context.preference(R.string.pref_auto_add_song, true)
    private var audioQuality by enumPreference(context, R.string.pref_audio_quality, AudioQuality.AUTO)

    private var currentQueue: Queue = EmptyQueue()

    val playerVolume = MutableStateFlow(1f)
    val currentMediaMetadata = MutableStateFlow<MediaMetadata?>(null)
    private val currentSongFlow = currentMediaMetadata.flatMapLatest { mediaMetadata ->
        songRepository.getSongById(mediaMetadata?.id).flow
    }
    private val currentFormat = currentMediaMetadata.flatMapLatest { mediaMetadata ->
        songRepository.getSongFormat(mediaMetadata?.id).flow
    }
    var currentSong: Song? = null

    private val showLyrics = context.sharedPreferences.booleanFlow(context.getString(R.string.pref_show_lyrics), false)

    val mediaSession = MediaSessionCompat(context, context.getString(R.string.app_name)).apply {
        isActive = true
    }

    private val cacheEvictor = when (val cacheSize = (VALUE_TO_MB.getOrNull(
        context.sharedPreferences.getInt(context.getString(R.string.pref_song_max_cache_size), 0))
        ?: 1024)) {
        -1 -> NoOpCacheEvictor()
        else -> LeastRecentlyUsedCacheEvictor(cacheSize * 1024 * 1024L)
    }
    val cache = SimpleCache(context.cacheDir.resolve("exoplayer"), cacheEvictor, StandaloneDatabaseProvider(context))
    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(createMediaSourceFactory())
        .setRenderersFactory(createRenderersFactory())
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(WAKE_MODE_NETWORK)
        .setAudioAttributes(AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build(), true)
        .build()
        .apply {
            addListener(this@SongPlayer)
            addAnalyticsListener(PlaybackStatsListener(false, this@SongPlayer))
        }

    private val mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
        setPlayer(player)
        setPlaybackPreparer(object : MediaSessionConnector.PlaybackPreparer {
            override fun onCommand(player: Player, command: String, extras: Bundle?, cb: ResultReceiver?) = false
            override fun getSupportedPrepareActions(): Long = ACTION_PREPARE or ACTION_PREPARE_FROM_MEDIA_ID or ACTION_PLAY_FROM_MEDIA_ID
            override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
                scope.launch {
                    val path = mediaId.split("/")
                    when (path.firstOrNull()) {
                        SONG -> {
                            val songId = path.getOrNull(1) ?: return@launch
                            val allSongs = songRepository.getAllSongs(SongSortInfoPreference).flow.first()
                            playQueue(ListQueue(
                                title = context.getString(R.string.queue_all_songs),
                                items = allSongs.map { it.toMediaItem() },
                                startIndex = allSongs.indexOfFirst { it.id == songId }.takeIf { it != -1 } ?: 0
                            ), playWhenReady)
                        }
                        ARTIST -> {
                            val songId = path.getOrNull(2) ?: return@launch
                            val artistId = path.getOrNull(1) ?: return@launch
                            val artist = songRepository.getArtistById(artistId) ?: return@launch
                            val songs = songRepository.getArtistSongs(artistId, SongSortInfoPreference).flow.first()
                            playQueue(ListQueue(
                                title = artist.name,
                                items = songs.map { it.toMediaItem() },
                                startIndex = songs.indexOfFirst { it.id == songId }.takeIf { it != -1 } ?: 0
                            ), playWhenReady)
                        }
                        ALBUM -> {
                            val songId = path.getOrNull(2) ?: return@launch
                            val albumId = path.getOrNull(1) ?: return@launch
                            val album = songRepository.getAlbum(albumId) ?: return@launch
                            val songs = songRepository.getAlbumSongs(albumId)
                            playQueue(ListQueue(
                                title = album.title,
                                items = songs.map { it.toMediaItem() },
                                startIndex = songs.indexOfFirst { it.id == songId }.takeIf { it != -1 } ?: 0
                            ), playWhenReady)
                        }
                        PLAYLIST -> {
                            val songId = path.getOrNull(2) ?: return@launch
                            val playlistId = path.getOrNull(1) ?: return@launch
                            val songs = when (playlistId) {
                                LIKED_PLAYLIST_ID -> songRepository.getLikedSongs(SongSortInfoPreference).flow.first()
                                DOWNLOADED_PLAYLIST_ID -> songRepository.getDownloadedSongs(SongSortInfoPreference).flow.first()
                                else -> songRepository.getPlaylistSongs(playlistId).getList()
                            }
                            playQueue(ListQueue(
                                title = when (playlistId) {
                                    LIKED_PLAYLIST_ID -> context.getString(R.string.liked_songs)
                                    DOWNLOADED_PLAYLIST_ID -> context.getString(R.string.downloaded_songs)
                                    else -> songRepository.getPlaylistById(playlistId).playlist.name
                                },
                                items = songs.map { it.toMediaItem() },
                                startIndex = songs.indexOfFirst { it.id == songId }.takeIf { it != -1 } ?: 0
                            ), playWhenReady)
                        }
                    }
                }
            }

            override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {}
            override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {}
            override fun onPrepare(playWhenReady: Boolean) {
                player.playWhenReady = playWhenReady
                player.prepare()
            }
        })
        registerCustomCommandReceiver { player, command, extras, _ ->
            if (extras == null) return@registerCustomCommandReceiver false
            when (command) {
                COMMAND_MOVE_QUEUE_ITEM -> {
                    val from = extras.getInt(EXTRA_FROM_INDEX, C.INDEX_UNSET)
                    val to = extras.getInt(EXTRA_TO_INDEX, C.INDEX_UNSET)
                    if (from != C.INDEX_UNSET && to != C.INDEX_UNSET) {
                        player.moveMediaItem(from, to)
                    }
                    true
                }
                COMMAND_SEEK_TO_QUEUE_ITEM -> {
                    player.seekToDefaultPosition(extras.getInt(EXTRA_QUEUE_INDEX))
                    true
                }
                COMMAND_PLAY_NEXT -> {
                    player.addMediaItems(
                        if (player.mediaItemCount == 0) 0 else player.currentMediaItemIndex + 1,
                        extras.getParcelableArray(EXTRA_MEDIA_METADATA_ITEMS)!!.filterIsInstance<MediaMetadata>().map { it.toMediaItem() }
                    )
                    player.prepare()
                    true
                }
                COMMAND_ADD_TO_QUEUE -> {
                    player.addMediaItems(extras.getParcelableArray(EXTRA_MEDIA_METADATA_ITEMS)!!.filterIsInstance<MediaMetadata>().map { it.toMediaItem() })
                    player.prepare()
                    true
                }
                else -> false
            }
        }
        setCustomActionProviders(
            object : MediaSessionConnector.CustomActionProvider {
                override fun onCustomAction(player: Player, action: String, extras: Bundle?) = toggleLike()
                override fun getCustomAction(player: Player) = if (currentMediaMetadata.value != null) {
                    CustomAction.Builder(
                        ACTION_TOGGLE_LIKE,
                        context.getString(if (currentSong?.song?.liked == true) R.string.action_remove_like else R.string.action_like),
                        if (currentSong?.song?.liked == true) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                    ).build()
                } else null
            },
            object : MediaSessionConnector.CustomActionProvider {
                override fun onCustomAction(player: Player, action: String, extras: Bundle?) = toggleLibrary()
                override fun getCustomAction(player: Player) = if (currentMediaMetadata.value != null) {
                    CustomAction.Builder(
                        ACTION_TOGGLE_LIBRARY,
                        context.getString(if (currentSong != null) R.string.action_remove_from_library else R.string.action_add_to_library),
                        if (currentSong != null) R.drawable.ic_library_add_check else R.drawable.ic_library_add
                    ).build()
                } else null
            },
            object : MediaSessionConnector.CustomActionProvider {
                override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
                    player.shuffleModeEnabled = !player.shuffleModeEnabled
                }

                override fun getCustomAction(player: Player) =
                    CustomAction.Builder(
                        ACTION_TOGGLE_SHUFFLE,
                        context.getString(R.string.btn_shuffle),
                        if (player.shuffleModeEnabled) R.drawable.ic_shuffle_on else R.drawable.ic_shuffle
                    ).build()
            }
        )
        setQueueNavigator { player, windowIndex -> player.getMediaItemAt(windowIndex).metadata!!.toMediaDescription(context) }
        setErrorMessageProvider { e -> // e is ExoPlaybackException
            val cause = e.cause?.cause as? PlaybackException // what we throw from resolving data source
            Pair(cause?.errorCode ?: e.errorCode, cause?.message ?: e.message)
        }
        setQueueEditor(object : MediaSessionConnector.QueueEditor {
            override fun onCommand(player: Player, command: String, extras: Bundle?, cb: ResultReceiver?) = false
            override fun onAddQueueItem(player: Player, description: MediaDescriptionCompat) = throw UnsupportedOperationException()
            override fun onAddQueueItem(player: Player, description: MediaDescriptionCompat, index: Int) = throw UnsupportedOperationException()
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
                player.currentMetadata?.artists?.joinToString { it.name }

            override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? =
                player.currentMetadata?.thumbnailUrl?.let { url ->
                    bitmapProvider.load(resizeThumbnailUrl(url, (512 * context.resources.displayMetrics.density).roundToInt(), null)) {
                        callback.onBitmap(it)
                    }
                }

            override fun createCurrentContentIntent(player: Player): PendingIntent? =
                PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java).apply {
                    action = ACTION_SHOW_BOTTOM_SHEET
                }, FLAG_IMMUTABLE)
        })
        .setChannelNameResourceId(R.string.channel_name_playback)
        .setNotificationListener(notificationListener)
        .setCustomActionReceiver(object : CustomActionReceiver {
            override fun createCustomActions(context: Context, instanceId: Int): Map<String, NotificationCompat.Action> = mapOf(
                ACTION_ADD_TO_LIBRARY to NotificationCompat.Action.Builder(
                    R.drawable.ic_library_add, context.getString(R.string.action_add_to_library), createPendingIntent(context, ACTION_ADD_TO_LIBRARY, instanceId)
                ).build(),
                ACTION_REMOVE_FROM_LIBRARY to NotificationCompat.Action.Builder(
                    R.drawable.ic_library_add_check, context.getString(R.string.action_remove_from_library), createPendingIntent(context, ACTION_REMOVE_FROM_LIBRARY, instanceId)
                ).build(),
                ACTION_LIKE to NotificationCompat.Action.Builder(
                    R.drawable.ic_favorite_border, context.getString(R.string.action_like), createPendingIntent(context, ACTION_LIKE, instanceId)
                ).build(),
                ACTION_UNLIKE to NotificationCompat.Action.Builder(
                    R.drawable.ic_favorite, context.getString(R.string.action_remove_like), createPendingIntent(context, ACTION_UNLIKE, instanceId)
                ).build()
            )

            override fun getCustomActions(player: Player): List<String> {
                val actions = mutableListOf<String>()
                if (player.currentMetadata != null && context.sharedPreferences.getBoolean(context.getString(R.string.pref_notification_more_action), true)) {
                    actions.add(if (currentSong == null) ACTION_ADD_TO_LIBRARY else ACTION_REMOVE_FROM_LIBRARY)
                    actions.add(if (currentSong?.song?.liked == true) ACTION_UNLIKE else ACTION_LIKE)
                }
                return actions
            }

            override fun onCustomAction(player: Player, action: String, intent: Intent) {
                when (action) {
                    ACTION_ADD_TO_LIBRARY, ACTION_REMOVE_FROM_LIBRARY -> toggleLibrary()
                    ACTION_LIKE, ACTION_UNLIKE -> toggleLike()
                }
            }
        })
        .build()
        .apply {
            setPlayer(player)
            setMediaSessionToken(mediaSession.sessionToken)
            setSmallIcon(R.drawable.ic_notification)
            setUseRewindAction(false)
            setUseFastForwardAction(false)
        }

    init {
        scope.launch {
            currentSongFlow.collect { song ->
                val shouldInvalidate = currentSong == null || song == null || currentSong?.song?.liked != song.song.liked
                currentSong = song
                if (shouldInvalidate) {
                    mediaSessionConnector.invalidateMediaSessionPlaybackState()
                    playerNotificationManager.invalidate()
                }
            }
        }
        scope.launch {
            combine(currentMediaMetadata.distinctUntilChangedBy { it?.id }, showLyrics) { mediaMetadata, showLyrics ->
                Pair(mediaMetadata, showLyrics)
            }.collectLatest { (mediaMetadata, showLyrics) ->
                if (showLyrics && mediaMetadata != null && !songRepository.hasLyrics(mediaMetadata.id)) {
                    LyricsHelper.loadLyrics(context, mediaMetadata)
                }
            }
        }
        scope.launch {
            context.sharedPreferences.booleanFlow(context.getString(R.string.pref_skip_silence), true).collectLatest {
                player.skipSilenceEnabled = it
            }
        }
        scope.launch {
            combine(currentFormat, context.sharedPreferences.booleanFlow(context.getString(R.string.pref_audio_normalization), true)) { format, normalizeAudio ->
                format to normalizeAudio
            }.collectLatest { (format, normalizeAudio) ->
                player.volume = if (normalizeAudio && format?.loudnessDb != null) {
                    min(10f.pow(-format.loudnessDb.toFloat() / 20), 1f)
                } else {
                    1f
                }
            }
        }
        scope.launch {
            context.sharedPreferences.booleanFlow(context.getString(R.string.pref_notification_more_action), true).collectLatest {
                playerNotificationManager.invalidate()
            }
        }
        if (context.sharedPreferences.getBoolean(context.getString(R.string.pref_persistent_queue), true)) {
            runCatching {
                context.filesDir.resolve(PERSISTENT_QUEUE_FILE).inputStream().use { fis ->
                    ObjectInputStream(fis).use { oos ->
                        oos.readObject() as PersistQueue
                    }
                }
            }.onSuccess { queue ->
                playQueue(ListQueue(
                    title = queue.title,
                    items = queue.items.map { it.toMediaItem() },
                    startIndex = queue.mediaItemIndex,
                    position = queue.position
                ), playWhenReady = false)
            }
        }
    }

    private fun createOkHttpDataSourceFactory() = OkHttpDataSource.Factory(OkHttpClient.Builder()
        .proxy(YouTube.proxy)
        .build())

    private fun createCacheDataSource() = CacheDataSource.Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context, createOkHttpDataSourceFactory()))

    private fun createMediaSourceFactory() = DefaultMediaSourceFactory(ResolvingDataSource.Factory(createCacheDataSource()) { dataSpec ->
        runBlocking {
            val mediaId = dataSpec.key ?: error("No media id")

            if (cache.isCached(mediaId, dataSpec.position, CHUNK_LENGTH)) {
                return@runBlocking dataSpec
            }

            (InfoCache.getInfo(mediaId) as? String)?.let { url ->
                return@runBlocking dataSpec.withUri(url.toUri())
            }

            // Check whether format exists so that users from older version can view format details
            // There may be inconsistent between the downloaded file and the displayed info if user change audio quality frequently
            val playedFormat = songRepository.getSongFormat(mediaId).getValueAsync()
            if (playedFormat != null && songRepository.getSongById(mediaId).getValueAsync()?.song?.downloadState == STATE_DOWNLOADED) {
                return@runBlocking dataSpec.withUri(songRepository.getSongFile(mediaId).toUri())
            }

            withContext(IO) {
                YouTube.player(mediaId)
            }.map { playerResponse ->
                if (playerResponse.playabilityStatus.status != "OK") {
                    throw PlaybackException(playerResponse.playabilityStatus.reason, null, ERROR_CODE_REMOTE_ERROR)
                }
                val format = if (playedFormat != null) {
                    playerResponse.streamingData?.adaptiveFormats?.find {
                        // Use itag to identify previous played format
                        it.itag == playedFormat.itag
                    }
                } else {
                    playerResponse.streamingData?.adaptiveFormats
                        ?.filter { it.isAudio }
                        ?.maxByOrNull {
                            it.bitrate * when (audioQuality) {
                                AudioQuality.AUTO -> if (connectivityManager.isActiveNetworkMetered) -1 else 1
                                AudioQuality.HIGH -> 1
                                AudioQuality.LOW -> -1
                            }
                        }
                } ?: throw PlaybackException(context.getString(R.string.error_no_stream), null, ERROR_CODE_NO_STREAM)
                songRepository.upsert(FormatEntity(
                    id = mediaId,
                    itag = format.itag,
                    mimeType = format.mimeType.split(";")[0],
                    codecs = format.mimeType.split("codecs=")[1].removeSurrounding("\""),
                    bitrate = format.bitrate,
                    sampleRate = format.audioSampleRate,
                    contentLength = format.contentLength!!,
                    loudnessDb = playerResponse.playerConfig?.audioConfig?.loudnessDb
                ))
                InfoCache.putInfo(mediaId, format.url, playerResponse.streamingData!!.expiresInSeconds * 1000L)
                dataSpec.withUri(format.url.toUri()).subrange(dataSpec.uriPositionOffset, CHUNK_LENGTH)
            }.getOrElse { throwable ->
                if (throwable is ConnectException || throwable is UnknownHostException) {
                    throw PlaybackException(context.getString(R.string.error_no_internet), throwable, ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)
                }
                if (throwable is SocketTimeoutException) {
                    throw PlaybackException(context.getString(R.string.error_timeout), throwable, ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT)
                }
                throw PlaybackException(context.getString(R.string.error_unknown), throwable, ERROR_CODE_REMOTE_ERROR)
            }
        }
    })

    private fun createRenderersFactory() = object : DefaultRenderersFactory(context) {
        override fun buildAudioSink(context: Context, enableFloatOutput: Boolean, enableAudioTrackPlaybackParams: Boolean, enableOffload: Boolean) =
            DefaultAudioSink.Builder()
                .setAudioCapabilities(AudioCapabilities.getCapabilities(context))
                .setEnableFloatOutput(enableFloatOutput)
                .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                .setOffloadMode(if (enableOffload) OFFLOAD_MODE_ENABLED_GAPLESS_REQUIRED else OFFLOAD_MODE_DISABLED)
                .setAudioProcessorChain(DefaultAudioProcessorChain(
                    emptyArray(),
                    SilenceSkippingAudioProcessor(2_000_000, 20_000, DEFAULT_SILENCE_THRESHOLD_LEVEL),
                    SonicAudioProcessor()
                ))
                .build()
    }

    fun playQueue(queue: Queue, playWhenReady: Boolean = true) {
        currentQueue = queue
        mediaSession.setQueueTitle(null)
        player.clearMediaItems()
        player.shuffleModeEnabled = false

        scope.launch(context.exceptionHandler) {
            val initialStatus = withContext(IO) { queue.getInitialStatus() }
            initialStatus.title?.let { queueTitle ->
                mediaSession.setQueueTitle(queueTitle)
            }
            player.setMediaItems(initialStatus.items, if (initialStatus.index > 0) initialStatus.index else 0, initialStatus.position)
            player.prepare()
            if (playWhenReady) {
                player.playWhenReady = true
            }
        }
    }

    fun startRadioSeamlessly() {
        val currentMediaMetadata = player.currentMetadata ?: return
        if (player.currentMediaItemIndex > 0) player.removeMediaItems(0, player.currentMediaItemIndex)
        if (player.currentMediaItemIndex < player.mediaItemCount - 1) player.removeMediaItems(player.currentMediaItemIndex + 1, player.mediaItemCount)
        scope.launch(context.exceptionHandler) {
            val radioQueue = YouTubeQueue(endpoint = WatchEndpoint(videoId = currentMediaMetadata.id))
            val initialStatus = radioQueue.getInitialStatus()
            initialStatus.title?.let { queueTitle ->
                mediaSession.setQueueTitle(queueTitle)
            }
            player.addMediaItems(initialStatus.items.drop(1))
            currentQueue = radioQueue
        }
    }

    fun handleQueueAddEndpoint(endpoint: QueueAddEndpoint, item: YTItem?) {
        scope.launch(context.exceptionHandler) {
            val items = when (item) {
                is SongItem -> YouTube.getQueue(videoIds = listOf(item.id)).getOrThrow().map { it.toMediaItem() }
                is AlbumItem -> withContext(IO) {
                    YouTube.browse(BrowseEndpoint(browseId = "VL" + item.playlistId)).getOrThrow().items.filterIsInstance<SongItem>().map { it.toMediaItem() }
                    // consider refetch by [YouTube.getQueue] if needed
                }
                is PlaylistItem -> withContext(IO) {
                    YouTube.getQueue(playlistId = endpoint.queueTarget.playlistId!!).getOrThrow().map { it.toMediaItem() }
                }
                is ArtistItem -> return@launch
                null -> when {
                    endpoint.queueTarget.videoId != null -> withContext(IO) {
                        YouTube.getQueue(videoIds = listOf(endpoint.queueTarget.videoId!!)).getOrThrow().map { it.toMediaItem() }
                    }
                    endpoint.queueTarget.playlistId != null -> withContext(IO) {
                        YouTube.getQueue(playlistId = endpoint.queueTarget.playlistId).getOrThrow().map { it.toMediaItem() }
                    }
                    else -> error("Unknown queue target")
                }
            }
            when (endpoint.queueInsertPosition) {
                INSERT_AFTER_CURRENT_VIDEO -> player.addMediaItems((if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex) + 1, items)
                INSERT_AT_END -> player.addMediaItems(items)
                else -> {}
            }
            player.prepare()
        }
    }

    fun playNext(items: List<MediaItem>) {
        player.addMediaItems(if (player.mediaItemCount == 0) 0 else player.currentMediaItemIndex + 1, items)
        player.prepare()
    }

    fun addToQueue(items: List<MediaItem>) {
        player.addMediaItems(items)
        player.prepare()
    }

    fun toggleLibrary() {
        scope.launch(context.exceptionHandler) {
            val song = currentSong
            val mediaMetadata = currentMediaMetadata.value ?: return@launch
            if (song == null) {
                songRepository.addSong(mediaMetadata)
            } else {
                songRepository.deleteSong(song)
            }
        }
    }

    fun toggleLike() {
        scope.launch(context.exceptionHandler) {
            val song = currentSong
            val mediaMetadata = currentMediaMetadata.value ?: return@launch
            if (song == null) {
                songRepository.addSong(mediaMetadata)
                songRepository.getSongById(mediaMetadata.id).getValueAsync()?.let {
                    songRepository.toggleLiked(it)
                }
            } else {
                songRepository.toggleLiked(song)
            }
        }
    }

    private fun addToLibrary(mediaMetadata: MediaMetadata) {
        scope.launch(context.exceptionHandler) {
            songRepository.addSong(mediaMetadata)
        }
    }

    private fun openAudioEffectSession() {
        context.sendBroadcast(
            Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.audioSessionId)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            }
        )
    }

    private fun closeAudioEffectSession() {
        context.sendBroadcast(
            Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.audioSessionId)
            }
        )
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
        scope.launch(context.exceptionHandler) {
            player.addMediaItems(currentQueue.nextPage())
        }
    }

    override fun onPositionDiscontinuity(oldPosition: PositionInfo, newPosition: PositionInfo, @DiscontinuityReason reason: Int) {
        if (reason == DISCONTINUITY_REASON_AUTO_TRANSITION && autoAddSong) {
            oldPosition.mediaItem?.metadata?.let {
                addToLibrary(it)
            }
        }
    }

    override fun onPlaybackStateChanged(@State playbackState: Int) {
        if (playbackState == STATE_ENDED && autoAddSong) {
            player.currentMetadata?.let {
                addToLibrary(it)
            }
        }
    }

    override fun onEvents(player: Player, events: Events) {
        if (events.containsAny(EVENT_PLAYBACK_STATE_CHANGED, EVENT_PLAY_WHEN_READY_CHANGED, EVENT_IS_PLAYING_CHANGED, EVENT_POSITION_DISCONTINUITY)) {
            if (player.playbackState != STATE_ENDED && player.playWhenReady) {
                openAudioEffectSession()
            } else {
                closeAudioEffectSession()
            }
        }
        if (events.containsAny(EVENT_TIMELINE_CHANGED, EVENT_POSITION_DISCONTINUITY)) {
            currentMediaMetadata.value = player.currentMetadata
        }
    }

    override fun onPlaybackStatsReady(eventTime: AnalyticsListener.EventTime, playbackStats: PlaybackStats) {
        val mediaItem = eventTime.timeline.getWindow(eventTime.windowIndex, Timeline.Window()).mediaItem
        scope.launch {
            songRepository.incrementSongTotalPlayTime(mediaItem.mediaId, playbackStats.totalPlayTimeMs)
        }
    }

    override fun onVolumeChanged(volume: Float) {
        playerVolume.value = volume
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        if (shuffleModeEnabled) {
            // Always put current playing item at first
            val shuffledIndices = IntArray(player.mediaItemCount)
            for (i in 0 until player.mediaItemCount) {
                shuffledIndices[i] = i
            }
            shuffledIndices.shuffle()
            shuffledIndices[shuffledIndices.indexOf(player.currentMediaItemIndex)] = shuffledIndices[0]
            shuffledIndices[0] = player.currentMediaItemIndex
            player.setShuffleOrder(DefaultShuffleOrder(shuffledIndices, System.currentTimeMillis()))
        }
    }

    private fun saveQueueToDisk() {
        if (player.playbackState == STATE_IDLE) {
            context.filesDir.resolve(PERSISTENT_QUEUE_FILE).delete()
            return
        }
        val persistQueue = PersistQueue(
            title = mediaSession.controller.queueTitle?.toString(),
            items = player.mediaItems.mapNotNull { it.metadata },
            mediaItemIndex = player.currentMediaItemIndex,
            position = player.currentPosition
        )
        runCatching {
            context.filesDir.resolve(PERSISTENT_QUEUE_FILE).outputStream().use { fos ->
                ObjectOutputStream(fos).use { oos ->
                    oos.writeObject(persistQueue)
                }
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun onDestroy() {
        if (context.sharedPreferences.getBoolean(context.getString(R.string.pref_persistent_queue), true)) {
            saveQueueToDisk()
        }
        mediaSession.apply {
            isActive = false
            release()
        }
        mediaSessionConnector.setPlayer(null)
        playerNotificationManager.setPlayer(null)
        player.removeListener(this)
        player.release()
        cache.release()
    }

    enum class AudioQuality {
        AUTO, HIGH, LOW
    }

    companion object {
        const val CHANNEL_ID = "music_channel_01"
        const val NOTIFICATION_ID = 888
        const val ERROR_CODE_NO_STREAM = 1000001
        const val CHUNK_LENGTH = 512 * 1024L
        const val PERSISTENT_QUEUE_FILE = "persistent_queue.data"

        fun createPendingIntent(context: Context, action: String, instanceId: Int): PendingIntent = PendingIntent.getBroadcast(
            context,
            instanceId,
            Intent(action).setPackage(context.packageName).putExtra(EXTRA_INSTANCE_ID, instanceId),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )
    }
}