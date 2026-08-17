package com.zionhuang.music.playback

import com.zionhuang.music.extensions.move

/**
 * Moves the [QueueEntry] at [fromEntryIndex] to [toEntryIndex] within [entries] (given in
 * current display order) as one atomic unit -- for a [QueueEntry.Group], every member flat
 * index moves together, in its original internal order, never split or internally reordered;
 * for a [QueueEntry.SingleSong], its one flat index moves. [toEntryIndex] follows the same
 * "post-removal position" convention as [MutableList.move]/`Player.moveMediaItem` -- i.e.
 * where the entry should land in the list once it has already been removed from
 * [fromEntryIndex].
 *
 * Returns the resulting flat index order: a flat IntArray of the real underlying Media3
 * timeline (flat) indices in the new desired order -- exactly the shape DefaultShuffleOrder
 * expects (when shuffled), or that a [computeMoveSequence]-derived moveMediaItem sequence
 * should physically reproduce (when not shuffled).
 */
fun moveQueueEntry(entries: List<QueueEntry>, fromEntryIndex: Int, toEntryIndex: Int): IntArray {
    val reordered = entries.toMutableList().move(fromEntryIndex, toEntryIndex)
    return reordered.flatMap { entry ->
        when (entry) {
            is QueueEntry.SingleSong -> listOf(entry.flatIndex)
            is QueueEntry.Group -> entry.flatIndices
        }
    }.toIntArray()
}

/**
 * Computes a sequence of single-item moves -- each an (fromIndex, toIndex) pair interpreted
 * with the same "post-removal position" convention as `Player.moveMediaItem` -- that
 * transforms the identity order `[0, 1, ..., targetOrder.size - 1]` into
 * `[targetOrder[0], targetOrder[1], ...]` when applied in sequence, each move simulated as
 * `add(toIndex, removeAt(fromIndex))` (the same operation [MutableList.move] performs).
 *
 * Used to physically reorder the flat, unshuffled ExoPlayer MediaItem list to match a target
 * flat order (e.g. from [moveQueueEntry]) using only the single-item `Player.moveMediaItem`
 * API already relied on elsewhere in this codebase, rather than depending on the semantics of
 * Media3's range-based `moveMediaItems`.
 */
fun computeMoveSequence(targetOrder: IntArray): List<Pair<Int, Int>> {
    if (targetOrder.isEmpty()) return emptyList()
    val moves = mutableListOf<Pair<Int, Int>>()
    val current = MutableList(targetOrder.size) { it }

    for (targetPos in targetOrder.indices) {
        val identity = targetOrder[targetPos]
        val fromPos = current.indexOf(identity)
        if (fromPos != targetPos) {
            moves += fromPos to targetPos
            current.add(targetPos, current.removeAt(fromPos))
        }
    }
    return moves
}
