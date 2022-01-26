package com.zionhuang.music.extensions

import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.zionhuang.music.models.MediaData
import com.zionhuang.music.playback.queues.Queue
import org.schabi.newpipe.extractor.Page

fun Player.findMediaItemById(mediaId: String): MediaItem? {
    for (i in 0 until mediaItemCount) {
        val item = getMediaItemAt(i)
        if (item.mediaId == mediaId) {
            return item
        }
    }
    return null
}

fun Player.mediaItemIndexOf(mediaId: String?): Int? {
    if (mediaId == null) return null
    for (i in 0 until mediaItemCount) {
        val item = getMediaItemAt(i)
        if (item.mediaId == mediaId) {
            return i
        }
    }
    return null
}

val Player.currentMetadata: MediaData?
    get() = currentMediaItem?.metadata

suspend fun Player.loadQueue(queue: Queue, mediaId: String?) {
    setMediaItems(queue.items)
    if (mediaId == null) return
    var idx = queue.items.indexOfFirst { it.mediaId == mediaId }
    while (idx != -1 && queue.hasNextPage()) {
        val lastItemCount = mediaItemCount
        val newItems = queue.nextPage().also {
            addMediaItems(it)
        }
        idx = newItems.indexOfFirst { it.mediaId == mediaId }
        if (idx != -1) idx += lastItemCount
    }
    if (idx != -1) seekToDefaultPosition(idx)

}

suspend fun Player.loadItems(
    targetId: String,
    initialItems: List<MediaItem>,
    page: Page?,
    get: suspend (Page) -> Pair<List<MediaItem>, Page>,
): Pair<Boolean, Page?> {
    var info = Pair(initialItems, page)
    var idx: Int
    val update = suspend { info = get(info.second!!); true }
    do {
        val lastItemCount = mediaItemCount
        addMediaItems(info.first)
        idx = info.first.indexOfFirst { it.mediaId == targetId }
        if (idx != -1) idx += lastItemCount
    } while (idx == -1 && Page.isValid(info.second) && update())
    seekToDefaultPosition(idx)
    return Pair(idx == -1, info.second)
}

val Player.mediaItems: List<MediaItem>
    get() = object : AbstractList<MediaItem>() {
        override val size: Int
            get() = mediaItemCount

        override fun get(index: Int): MediaItem = getMediaItemAt(index)
    }