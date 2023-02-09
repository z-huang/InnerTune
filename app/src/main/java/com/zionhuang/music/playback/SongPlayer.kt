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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
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
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.music.ACTION_SHOW_BOTTOM_SHEET
import com.zionhuang.music.MainActivity
import com.zionhuang.music.R
import com.zionhuang.music.constants.*
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_ADD_TO_LIBRARY
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_LIKE
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_REMOVE_FROM_LIBRARY
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_TOGGLE_LIBRARY
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_TOGGLE_LIKE
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_TOGGLE_SHUFFLE
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_UNLIKE
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.entities.FormatEntity
import com.zionhuang.music.db.entities.LyricsEntity
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.DOWNLOADED_PLAYLIST_ID
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.LIKED_PLAYLIST_ID
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.db.entities.SongEntity.Companion.STATE_DOWNLOADED
import com.zionhuang.music.extensions.*
import com.zionhuang.music.lyrics.LyricsHelper
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.PersistQueue
import com.zionhuang.music.playback.MusicService.Companion.ALBUM
import com.zionhuang.music.playback.MusicService.Companion.ARTIST
import com.zionhuang.music.playback.MusicService.Companion.PLAYLIST
import com.zionhuang.music.playback.MusicService.Companion.SONG
import com.zionhuang.music.playback.queues.EmptyQueue
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.playback.queues.Queue
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.ui.utils.resize
import com.zionhuang.music.utils.*
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
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SongPlayer(
    private val context: Context,
    private val database: MusicDatabase,
    private val lyricsHelper: LyricsHelper,
    private val scope: CoroutineScope,
    notificationListener: PlayerNotificationManager.NotificationListener,
) : Listener, PlaybackStatsListener.Callback {
    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!
    val bitmapProvider = BitmapProvider(context)

    private val autoAddSong by preference(context, AutoAddToLibraryKey, true)
    private val audioQuality by enumPreference(context, AudioQualityKey, AudioQuality.AUTO)

    private var currentQueue: Queue = EmptyQueue
    var queueTitle: String? = null

    val currentMediaMetadata = MutableStateFlow<MediaMetadata?>(null)
    private val currentSongFlow = currentMediaMetadata.flatMapLatest { mediaMetadata ->
        database.song(mediaMetadata?.id)
    }
    private val currentFormat = currentMediaMetadata.flatMapLatest { mediaMetadata ->
        database.format(mediaMetadata?.id)
    }
    var currentSong: Song? = null

    private val cacheEvictor = when (val cacheSize = context.dataStore[MaxSongCacheSizeKey] ?: 1024) {
        -1 -> NoOpCacheEvictor()
        else -> LeastRecentlyUsedCacheEvictor(cacheSize * 1024 * 1024L)
    }
    val cache = SimpleCache(context.cacheDir.resolve("exoplayer"), cacheEvictor, StandaloneDatabaseProvider(context))
    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(createMediaSourceFactory())
        .setRenderersFactory(createRenderersFactory())
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(WAKE_MODE_NETWORK)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(), true
        )
        .setSeekBackIncrementMs(5000)
        .setSeekForwardIncrementMs(5000)
        .build()
        .apply {
            addListener(this@SongPlayer)
            addAnalyticsListener(PlaybackStatsListener(false, this@SongPlayer))
        }

    private val normalizeFactor = MutableStateFlow(1f)
    val playerVolume = MutableStateFlow(context.dataStore.get(PlayerVolumeKey, 1f).coerceIn(0f, 1f))

    var sleepTimerJob: Job? = null
    var sleepTimerTriggerTime by mutableStateOf(-1L)
    var pauseWhenSongEnd by mutableStateOf(false)

    val mediaSession = MediaSessionCompat(context, context.getString(R.string.app_name)).apply {
        isActive = true
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
                            val allSongs = database.songsByCreateDateDesc().first()
                            playQueue(ListQueue(
                                title = context.getString(R.string.queue_all_songs),
                                items = allSongs.map { it.toMediaItem() },
                                startIndex = allSongs.indexOfFirst { it.id == songId }.takeIf { it != -1 } ?: 0
                            ), playWhenReady)
                        }
                        ARTIST -> {
                            val songId = path.getOrNull(2) ?: return@launch
                            val artistId = path.getOrNull(1) ?: return@launch
                            val artist = database.artist(artistId).first() ?: return@launch
                            val songs = database.artistSongsByCreateDateDesc(artistId).first()
                            playQueue(ListQueue(
                                title = artist.name,
                                items = songs.map { it.toMediaItem() },
                                startIndex = songs.indexOfFirst { it.id == songId }.takeIf { it != -1 } ?: 0
                            ), playWhenReady)
                        }
                        ALBUM -> {
                            val songId = path.getOrNull(2) ?: return@launch
                            val albumId = path.getOrNull(1) ?: return@launch
                            val albumWithSongs = database.albumWithSongs(albumId).first() ?: return@launch
                            playQueue(ListQueue(
                                title = albumWithSongs.album.title,
                                items = albumWithSongs.songs.map { it.toMediaItem() },
                                startIndex = albumWithSongs.songs.indexOfFirst { it.id == songId }.takeIf { it != -1 } ?: 0
                            ), playWhenReady)
                        }
                        PLAYLIST -> {
                            val songId = path.getOrNull(2) ?: return@launch
                            val playlistId = path.getOrNull(1) ?: return@launch
                            val songs = when (playlistId) {
                                LIKED_PLAYLIST_ID -> database.likedSongs(SongSortType.CREATE_DATE, descending = true).first()
                                DOWNLOADED_PLAYLIST_ID -> database.downloadedSongs(SongSortType.CREATE_DATE, descending = true).first()
                                else -> database.playlistSongs(playlistId).first()
                            }
                            playQueue(ListQueue(
                                title = when (playlistId) {
                                    LIKED_PLAYLIST_ID -> context.getString(R.string.liked_songs)
                                    DOWNLOADED_PLAYLIST_ID -> context.getString(R.string.downloaded_songs)
                                    else -> database.playlist(playlistId).first()?.playlist?.name ?: return@launch
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
                        context.getString(R.string.shuffle),
                        if (player.shuffleModeEnabled) R.drawable.ic_shuffle_on else R.drawable.ic_shuffle
                    ).build()
            }
        )
        setQueueNavigator { player, windowIndex -> player.getMediaItemAt(windowIndex).metadata!!.toMediaDescription() }
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
                    bitmapProvider.load(url.resize(544, 544)) {
                        callback.onBitmap(it)
                    }
                }

            override fun createCurrentContentIntent(player: Player): PendingIntent? =
                PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java).apply {
                    action = ACTION_SHOW_BOTTOM_SHEET
                }, FLAG_IMMUTABLE)
        })
        .setChannelNameResourceId(R.string.music_player)
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
                if (player.currentMetadata != null && context.dataStore.get(NotificationMoreActionKey, true)) {
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
            combine(playerVolume, normalizeFactor) { playerVolume, normalizeFactor ->
                playerVolume * normalizeFactor
            }.collectLatest {
                player.volume = it
            }
        }
        scope.launch {
            playerVolume.debounce(1000).collect { volume ->
                context.dataStore.edit { settings ->
                    settings[PlayerVolumeKey] = volume
                }
            }
        }
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
            combine(
                currentMediaMetadata.distinctUntilChangedBy { it?.id },
                context.dataStore.data.map { it[ShowLyricsKey] ?: false }.distinctUntilChanged()
            ) { mediaMetadata, showLyrics ->
                mediaMetadata to showLyrics
            }.collectLatest { (mediaMetadata, showLyrics) ->
                if (showLyrics && mediaMetadata != null && database.lyrics(mediaMetadata.id).first() == null) {
                    val lyrics = lyricsHelper.getLyrics(mediaMetadata)
                    database.query {
                        upsert(
                            LyricsEntity(
                                id = mediaMetadata.id,
                                lyrics = lyrics
                            )
                        )
                    }
                }
            }
        }
        scope.launch {
            context.dataStore.data
                .map { it[SkipSilenceKey] ?: true }
                .distinctUntilChanged()
                .collectLatest {
                    player.skipSilenceEnabled = it
                }
        }
        scope.launch {
            combine(
                currentFormat,
                context.dataStore.data
                    .map { it[AudioNormalizationKey] ?: true }
                    .distinctUntilChanged()
            ) { format, normalizeAudio ->
                format to normalizeAudio
            }.collectLatest { (format, normalizeAudio) ->
                normalizeFactor.value = if (normalizeAudio && format?.loudnessDb != null) {
                    min(10f.pow(-format.loudnessDb.toFloat() / 20), 1f)
                } else {
                    1f
                }
            }
        }
        scope.launch {
            context.dataStore.data
                .map { it[NotificationMoreActionKey] ?: true }
                .distinctUntilChanged()
                .collectLatest {
                    playerNotificationManager.invalidate()
                }
        }
        if (context.dataStore.get(PersistentQueueKey, true)) {
            runCatching {
                context.filesDir.resolve(PERSISTENT_QUEUE_FILE).inputStream().use { fis ->
                    ObjectInputStream(fis).use { oos ->
                        oos.readObject() as PersistQueue
                    }
                }
            }.onSuccess { queue ->
                playQueue(
                    queue = ListQueue(
                        title = queue.title,
                        items = queue.items.map { it.toMediaItem() },
                        startIndex = queue.mediaItemIndex,
                        position = queue.position
                    ),
                    playWhenReady = false
                )
            }
        }
    }

    private fun createOkHttpDataSourceFactory() =
        OkHttpDataSource.Factory(
            OkHttpClient.Builder()
                .proxy(YouTube.proxy)
                .build()
        )

    private fun createCacheDataSource() = CacheDataSource.Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context, createOkHttpDataSourceFactory()))

    private fun createMediaSourceFactory() = DefaultMediaSourceFactory(ResolvingDataSource.Factory(createCacheDataSource()) { dataSpec ->
        runBlocking {
            val mediaId = dataSpec.key ?: error("No media id")

            if (cache.isCached(mediaId, dataSpec.position, CHUNK_LENGTH)) {
                return@runBlocking dataSpec
            }

            // Check whether format exists so that users from older version can view format details
            // There may be inconsistent between the downloaded file and the displayed info if user change audio quality frequently
            val playedFormat = database.format(mediaId).firstOrNull()
            val song = database.song(mediaId).firstOrNull()
            if (playedFormat != null && song?.song?.downloadState == STATE_DOWNLOADED) {
                return@runBlocking dataSpec.withUri(getSongFile(context, mediaId).toUri())
            }

            val playerResponse = withContext(IO) {
                YouTube.player(mediaId)
            }.getOrElse { throwable ->
                if (throwable is ConnectException || throwable is UnknownHostException) {
                    throw PlaybackException(context.getString(R.string.error_no_internet), throwable, ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)
                }
                if (throwable is SocketTimeoutException) {
                    throw PlaybackException(context.getString(R.string.error_timeout), throwable, ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT)
                }
                throw PlaybackException(context.getString(R.string.error_unknown), throwable, ERROR_CODE_REMOTE_ERROR)
            }
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
                        } + (if (it.mimeType.startsWith("audio/webm")) 10240 else 0) // prefer opus stream
                    }
            } ?: throw PlaybackException(context.getString(R.string.error_no_stream), null, ERROR_CODE_NO_STREAM)

            database.query {
                upsert(
                    FormatEntity(
                        id = mediaId,
                        itag = format.itag,
                        mimeType = format.mimeType.split(";")[0],
                        codecs = format.mimeType.split("codecs=")[1].removeSurrounding("\""),
                        bitrate = format.bitrate,
                        sampleRate = format.audioSampleRate,
                        contentLength = format.contentLength!!,
                        loudnessDb = playerResponse.playerConfig?.audioConfig?.loudnessDb
                    )
                )
            }
            dataSpec.withUri(format.url.toUri()).subrange(dataSpec.uriPositionOffset, CHUNK_LENGTH)
        }
    })

    private fun createRenderersFactory() = object : DefaultRenderersFactory(context) {
        override fun buildAudioSink(context: Context, enableFloatOutput: Boolean, enableAudioTrackPlaybackParams: Boolean, enableOffload: Boolean) =
            DefaultAudioSink.Builder()
                .setAudioCapabilities(AudioCapabilities.getCapabilities(context))
                .setEnableFloatOutput(enableFloatOutput)
                .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                .setOffloadMode(if (enableOffload) OFFLOAD_MODE_ENABLED_GAPLESS_REQUIRED else OFFLOAD_MODE_DISABLED)
                .setAudioProcessorChain(
                    DefaultAudioProcessorChain(
                        emptyArray(),
                        SilenceSkippingAudioProcessor(2_000_000, 20_000, DEFAULT_SILENCE_THRESHOLD_LEVEL),
                        SonicAudioProcessor()
                    )
                )
                .build()
    }

    private fun updateQueueTitle(title: String?) {
        mediaSession.setQueueTitle(title)
        queueTitle = title
    }

    fun playQueue(queue: Queue, playWhenReady: Boolean = true) {
        currentQueue = queue
        updateQueueTitle(null)
        player.shuffleModeEnabled = false
        if (queue.preloadItem != null) {
            player.setMediaItem(queue.preloadItem!!.toMediaItem())
            player.prepare()
            player.playWhenReady = playWhenReady
        }

        scope.launch {
            val initialStatus = withContext(IO) { queue.getInitialStatus() }
            if (queue.preloadItem != null && player.playbackState == STATE_IDLE) return@launch
            initialStatus.title?.let { queueTitle ->
                updateQueueTitle(queueTitle)
            }
            if (queue.preloadItem != null) {
                player.addMediaItems(0, initialStatus.items.subList(0, initialStatus.mediaItemIndex))
                player.addMediaItems(initialStatus.items.subList(initialStatus.mediaItemIndex + 1, initialStatus.items.size))
            } else {
                player.setMediaItems(initialStatus.items, if (initialStatus.mediaItemIndex > 0) initialStatus.mediaItemIndex else 0, initialStatus.position)
                player.prepare()
                player.playWhenReady = playWhenReady
            }
        }
    }

    fun startRadioSeamlessly() {
        val currentMediaMetadata = player.currentMetadata ?: return
        if (player.currentMediaItemIndex > 0) player.removeMediaItems(0, player.currentMediaItemIndex)
        if (player.currentMediaItemIndex < player.mediaItemCount - 1) player.removeMediaItems(player.currentMediaItemIndex + 1, player.mediaItemCount)
        scope.launch {
            val radioQueue = YouTubeQueue(endpoint = WatchEndpoint(videoId = currentMediaMetadata.id))
            val initialStatus = radioQueue.getInitialStatus()
            initialStatus.title?.let { queueTitle ->
                updateQueueTitle(queueTitle)
            }
            player.addMediaItems(initialStatus.items.drop(1))
            currentQueue = radioQueue
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
        database.query {
            val song = currentSong
            val mediaMetadata = currentMediaMetadata.value ?: return@query
            if (song == null) {
                insert(mediaMetadata)
            } else {
                delete(song)
            }
        }
    }

    fun toggleLike() {
        database.query {
            val song = currentSong
            val mediaMetadata = currentMediaMetadata.value ?: return@query
            if (song == null) {
                insert(mediaMetadata, SongEntity::toggleLike)
            } else {
                update(song.song.toggleLike())
            }
        }
    }

    private fun addToLibrary(mediaMetadata: MediaMetadata) {
        database.query {
            insert(mediaMetadata)
        }
    }

    fun setSleepTimer(minute: Int) {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        if (minute == -1) {
            pauseWhenSongEnd = true
        } else {
            sleepTimerTriggerTime = System.currentTimeMillis() + minute.minutes.inWholeMilliseconds
            sleepTimerJob = scope.launch {
                delay(minute.minutes)
                player.pause()
                sleepTimerTriggerTime = -1L
            }
        }
    }

    fun clearSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        pauseWhenSongEnd = false
        sleepTimerTriggerTime = -1L
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
        if (reason != MEDIA_ITEM_TRANSITION_REASON_REPEAT &&
            player.playbackState != STATE_IDLE &&
            player.mediaItemCount - player.currentMediaItemIndex <= 5 &&
            currentQueue.hasNextPage()
        ) {
            scope.launch {
                val mediaItems = currentQueue.nextPage()
                if (player.playbackState != STATE_IDLE) {
                    player.addMediaItems(mediaItems)
                }
            }
        }
        if (mediaItem == null) {
            bitmapProvider.clear()
        }
        if (pauseWhenSongEnd) {
            pauseWhenSongEnd = false
            player.pause()
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
        if (playbackState == STATE_IDLE) {
            currentQueue = EmptyQueue
            player.shuffleModeEnabled = false
            mediaSession.setQueueTitle("")
        }
        if (playbackState == STATE_ENDED) {
            if (autoAddSong) {
                player.currentMetadata?.let {
                    addToLibrary(it)
                }
            }
            if (pauseWhenSongEnd) {
                pauseWhenSongEnd = false
                player.pause()
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
        database.query {
            incrementTotalPlayTime(mediaItem.mediaId, playbackStats.totalPlayTimeMs)
        }
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        if (shuffleModeEnabled) {
            // Always put current playing item at first
            val shuffledIndices = IntArray(player.mediaItemCount) { it }
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
        if (context.dataStore.get(PersistentQueueKey, true)) {
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
