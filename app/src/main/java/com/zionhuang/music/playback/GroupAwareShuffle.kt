package com.zionhuang.music.playback

import kotlin.random.Random

/**
 * Generates a group-aware shuffle permutation: shuffles [entries] (which must be built from
 * the ORIGINAL/unshuffled flat queue order -- see QueueEntry's consecutive-grouping rule) as
 * whole top-level units rather than individual flat indices, so every member of a
 * [QueueEntry.Group] stays contiguous and in its original internal order after shuffling.
 *
 * The entry containing [currentFlatIndex] (a standalone song, or an entire group) is swapped
 * into position 0, mirroring the "shuffle everything, then swap the current item to the front"
 * technique the flat (pre-group-aware) shuffle algorithm used -- see
 * MusicService.onShuffleModeEnabledChanged. If no entry contains [currentFlatIndex] (e.g. an
 * empty queue), the shuffled order is returned as-is with no pinning.
 *
 * Returns a flat IntArray of the real underlying Media3 timeline (flat) indices in the desired
 * new playback order -- i.e. exactly the shape DefaultShuffleOrder's constructor expects,
 * identical in kind to what the flat algorithm it replaces already produced.
 */
fun buildGroupAwareShuffleOrder(
    entries: List<QueueEntry>,
    currentFlatIndex: Int,
    random: Random = Random.Default,
): IntArray {
    if (entries.isEmpty()) return IntArray(0)

    val shuffled = entries.shuffled(random).toMutableList()
    val currentEntryIndex = shuffled.indexOfFirst { it.containsFlatIndex(currentFlatIndex) }
    if (currentEntryIndex > 0) {
        val displaced = shuffled[0]
        shuffled[0] = shuffled[currentEntryIndex]
        shuffled[currentEntryIndex] = displaced
    }

    return shuffled.flatMap { entry ->
        when (entry) {
            is QueueEntry.SingleSong -> listOf(entry.flatIndex)
            is QueueEntry.Group -> entry.flatIndices
        }
    }.toIntArray()
}

private fun QueueEntry.containsFlatIndex(flatIndex: Int): Boolean = when (this) {
    is QueueEntry.SingleSong -> this.flatIndex == flatIndex
    is QueueEntry.Group -> flatIndex in flatIndices
}
