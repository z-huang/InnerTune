package com.zionhuang.music.playback

import android.content.Context
import android.graphics.Bitmap
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.Timeline
import com.zionhuang.music.extensions.currentMetadata
import com.zionhuang.music.extensions.getCurrentQueueIndex
import com.zionhuang.music.extensions.getQueueWindows
import com.zionhuang.music.extensions.metadata
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.playback.MusicService.MusicBinder
import com.zionhuang.music.playback.queues.Queue
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerConnection(context: Context, val binder: MusicBinder) : Listener {
    val songRepository by lazy { SongRepository(context) }
    val songPlayer = binder.songPlayer
    val player = binder.player

    val playbackState = MutableStateFlow(STATE_IDLE)
    val playWhenReady = MutableStateFlow(false)
    val mediaMetadata = MutableStateFlow<MediaMetadata?>(null)
    val currentSong = mediaMetadata.flatMapLatest {
        songRepository.getSongById(it?.id).flow
    }
    val currentLyrics = mediaMetadata.flatMapLatest { mediaMetadata ->
        songRepository.getLyrics(mediaMetadata?.id)
    }
    val currentFormat = mediaMetadata.flatMapLatest { mediaMetadata ->
        songRepository.getSongFormat(mediaMetadata?.id).flow
    }

    val queueTitle = MutableStateFlow<String?>(null)
    val queueItems = MutableStateFlow<List<Timeline.Window>>(emptyList())
    val currentMediaItemIndex = MutableStateFlow(-1)
    val currentWindowIndex = MutableStateFlow(-1)

    val shuffleModeEnabled = MutableStateFlow(false)
    val repeatMode = MutableStateFlow(REPEAT_MODE_OFF)

    val canSkipPrevious = MutableStateFlow(true)
    val canSkipNext = MutableStateFlow(true)

    var onBitmapChanged: (Bitmap?) -> Unit = {}
        set(value) {
            field = value
            binder.songPlayer.bitmapProvider.onBitmapChanged = value
        }

    init {
        binder.player.addListener(this)
        binder.songPlayer.bitmapProvider.onBitmapChanged = onBitmapChanged

        playbackState.value = binder.player.playbackState
        playWhenReady.value = binder.player.playWhenReady
        mediaMetadata.value = binder.player.currentMetadata
        queueTitle.value = binder.songPlayer.queueTitle
        queueItems.value = binder.player.getQueueWindows()
        currentWindowIndex.value = binder.player.getCurrentQueueIndex()
        currentMediaItemIndex.value = binder.player.currentMediaItemIndex
        shuffleModeEnabled.value = binder.player.shuffleModeEnabled
        repeatMode.value = binder.player.repeatMode
    }

    fun playQueue(queue: Queue) {
        binder.songPlayer.playQueue(queue)
    }

    fun playNext(item: MediaItem) = playNext(listOf(item))
    fun playNext(items: List<MediaItem>) {
        binder.songPlayer.playNext(items)
    }

    fun addToQueue(item: MediaItem) = addToQueue(listOf(item))
    fun addToQueue(items: List<MediaItem>) {
        binder.songPlayer.addToQueue(items)
    }

    fun toggleRepeatMode() {
        binder.player.let {
            it.repeatMode = when (it.repeatMode) {
                REPEAT_MODE_OFF -> REPEAT_MODE_ALL
                REPEAT_MODE_ALL -> REPEAT_MODE_ONE
                REPEAT_MODE_ONE -> REPEAT_MODE_OFF
                else -> throw IllegalStateException()
            }
        }
    }

    fun toggleLike() {
        binder.songPlayer.toggleLike()
    }

    fun toggleLibrary() {
        binder.songPlayer.toggleLibrary()
    }

    override fun onPlaybackStateChanged(state: Int) {
        playbackState.value = state
    }

    override fun onPlayWhenReadyChanged(newPlayWhenReady: Boolean, reason: Int) {
        playWhenReady.value = newPlayWhenReady
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        mediaMetadata.value = mediaItem?.metadata
        currentMediaItemIndex.value = player.currentMediaItemIndex
        currentWindowIndex.value = binder.player.getCurrentQueueIndex()
        updateCanSkipPreviousAndNext()
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        queueItems.value = binder.player.getQueueWindows()
        queueTitle.value = binder.songPlayer.queueTitle
        currentMediaItemIndex.value = player.currentMediaItemIndex
        currentWindowIndex.value = binder.player.getCurrentQueueIndex()
        updateCanSkipPreviousAndNext()
    }

    override fun onShuffleModeEnabledChanged(enabled: Boolean) {
        shuffleModeEnabled.value = enabled
        queueItems.value = binder.player.getQueueWindows()
        currentWindowIndex.value = binder.player.getCurrentQueueIndex()
        updateCanSkipPreviousAndNext()
    }

    override fun onRepeatModeChanged(mode: Int) {
        repeatMode.value = mode
        updateCanSkipPreviousAndNext()
    }

    private fun updateCanSkipPreviousAndNext() {
        if (!player.currentTimeline.isEmpty) {
            val window = player.currentTimeline.getWindow(player.currentMediaItemIndex, Timeline.Window())
            canSkipPrevious.value = player.isCommandAvailable(COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                    || !window.isLive()
                    || player.isCommandAvailable(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            canSkipNext.value = window.isLive() && window.isDynamic
                    || player.isCommandAvailable(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
        } else {
            canSkipPrevious.value = false
            canSkipNext.value = false
        }
    }

    fun dispose() {
        songPlayer.bitmapProvider.onBitmapChanged = {}
        player.removeListener(this)
    }
}