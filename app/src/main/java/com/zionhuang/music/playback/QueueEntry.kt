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
 * Folds [items] -- pairs of (underlying flat Media3 timeline index, that item's
 * [MediaMetadata]) given in current queue/display order, which may differ from flat-index
 * order when shuffled -- into [QueueEntry] runs. Grouping adjacency is evaluated over the
 * given display order, but each resulting index is the real flat index carried alongside
 * its metadata, never the item's position in [items]. Grouping is based solely on
 * [MediaMetadata.queueGroupId] and only merges *consecutive* items -- the same group id
 * reappearing later after being interrupted by a different item starts a new, separate
 * [QueueEntry.Group].
 *
 * Named distinctly from [buildQueueEntries] (rather than overloaded) because a same-named
 * overload taking `List<Pair<Int, MediaMetadata>>` collides with `List<MediaMetadata>` under
 * JVM generic erasure ("platform declaration clash").
 */
fun buildQueueEntriesIndexed(items: List<Pair<Int, MediaMetadata>>): List<QueueEntry> {
    val entries = mutableListOf<QueueEntry>()
    var runStart = 0
    while (runStart < items.size) {
        val groupId = items[runStart].second.queueGroupId
        var runEnd = runStart + 1
        if (groupId != null) {
            while (runEnd < items.size && items[runEnd].second.queueGroupId == groupId) {
                runEnd++
            }
            entries += QueueEntry.Group(
                groupId = groupId,
                title = items[runStart].second.queueGroupTitle.orEmpty(),
                flatIndices = (runStart until runEnd).map { items[it].first }
            )
        } else {
            entries += QueueEntry.SingleSong(items[runStart].first)
        }
        runStart = runEnd
    }
    return entries
}

/**
 * Convenience for callers whose [MediaMetadata] list is already in flat-index order (list
 * position == flat index) -- e.g. a source with no shuffle/reordering concept. Delegates to
 * [buildQueueEntriesIndexed] with an identity index mapping, so behavior for existing
 * callers (Stage 2's tests included) is unchanged.
 */
fun buildQueueEntries(items: List<MediaMetadata>): List<QueueEntry> =
    buildQueueEntriesIndexed(items.mapIndexed { index, metadata -> index to metadata })
