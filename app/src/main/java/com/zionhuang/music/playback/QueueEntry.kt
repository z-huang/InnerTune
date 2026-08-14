package com.zionhuang.music.playback

import com.zionhuang.music.models.MediaMetadata

/**
 * A derived, read-only view over the flat, live queue: either a standalone song or a
 * run of consecutive songs sharing the same [MediaMetadata.queueGroupId]. This is never
 * a source of truth -- the flat ExoPlayer item list remains the only real queue state.
 */
sealed interface QueueEntry {
    data class SingleSong(val flatIndex: Int) : QueueEntry
    data class Group(
        val groupId: String,
        val title: String,
        val flatIndices: List<Int>,
    ) : QueueEntry
}

/**
 * Folds [items] (in current queue order, one [MediaMetadata] per flat index) into
 * [QueueEntry] runs. Grouping is based solely on [MediaMetadata.queueGroupId] and only
 * merges *consecutive* items -- the same group id reappearing later after being
 * interrupted by a different item starts a new, separate [QueueEntry.Group].
 */
fun buildQueueEntries(items: List<MediaMetadata>): List<QueueEntry> {
    val entries = mutableListOf<QueueEntry>()
    var runStart = 0
    while (runStart < items.size) {
        val groupId = items[runStart].queueGroupId
        var runEnd = runStart + 1
        if (groupId != null) {
            while (runEnd < items.size && items[runEnd].queueGroupId == groupId) {
                runEnd++
            }
            entries += QueueEntry.Group(
                groupId = groupId,
                title = items[runStart].queueGroupTitle.orEmpty(),
                flatIndices = (runStart until runEnd).toList()
            )
        } else {
            entries += QueueEntry.SingleSong(runStart)
        }
        runStart = runEnd
    }
    return entries
}
