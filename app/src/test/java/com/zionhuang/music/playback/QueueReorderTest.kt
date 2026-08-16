package com.zionhuang.music.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QueueReorderTest {
    private fun singleSong(flatIndex: Int) = QueueEntry.SingleSong(flatIndex)
    private fun group(groupId: String, vararg flatIndices: Int) =
        QueueEntry.Group(groupId, "Album $groupId", flatIndices.toList())

    private fun expectedFlatIndices(entries: List<QueueEntry>): Set<Int> = entries.flatMap {
        when (it) {
            is QueueEntry.SingleSong -> listOf(it.flatIndex)
            is QueueEntry.Group -> it.flatIndices
        }
    }.toSet()

    private fun assertValidPermutation(result: IntArray, entries: List<QueueEntry>) {
        val expected = expectedFlatIndices(entries)
        assertEquals("wrong number of items in permutation", expected.size, result.size)
        assertEquals("permutation does not contain exactly the expected flat indices", expected, result.toSet())
    }

    private fun assertContiguousAndOrdered(result: IntArray, groupFlatIndices: List<Int>) {
        val start = result.indexOf(groupFlatIndices.first())
        assertTrue("group's first member (${groupFlatIndices.first()}) not found in ${result.toList()}", start != -1)
        assertTrue("group extends past array bounds", start + groupFlatIndices.size <= result.size)
        assertEquals(
            "group members are not contiguous/in original order",
            groupFlatIndices,
            result.toList().subList(start, start + groupFlatIndices.size)
        )
    }

    /** Simulates applying [moves] (Player.moveMediaItem-style) to the identity order [0, size). */
    private fun applyMoveSequence(size: Int, moves: List<Pair<Int, Int>>): List<Int> {
        val list = MutableList(size) { it }
        moves.forEach { (from, to) -> list.add(to, list.removeAt(from)) }
        return list
    }

    // ---- moveQueueEntry ----

    @Test
    fun `moving a group forward`() {
        val entries = listOf(singleSong(0), group("x", 1, 2, 3), singleSong(4), singleSong(5))
        val result = moveQueueEntry(entries, fromEntryIndex = 1, toEntryIndex = 3)
        assertValidPermutation(result, entries)
        assertEquals(listOf(0, 4, 5, 1, 2, 3), result.toList())
    }

    @Test
    fun `moving a group backward`() {
        val entries = listOf(singleSong(0), singleSong(1), group("x", 2, 3, 4), singleSong(5))
        val result = moveQueueEntry(entries, fromEntryIndex = 2, toEntryIndex = 0)
        assertValidPermutation(result, entries)
        assertEquals(listOf(2, 3, 4, 0, 1, 5), result.toList())
    }

    @Test
    fun `moving a group across multiple standalone songs`() {
        val entries = listOf(singleSong(0), singleSong(1), group("x", 2, 3), singleSong(4), singleSong(5), singleSong(6))
        val result = moveQueueEntry(entries, fromEntryIndex = 2, toEntryIndex = 5)
        assertValidPermutation(result, entries)
        assertContiguousAndOrdered(result, listOf(2, 3))
        // toEntryIndex=5 is a post-removal position: after removing the group, the remaining 5
        // entries are [0,1,4,5,6] (indices 0..4), so index 5 means "append at the end".
        assertEquals(listOf(0, 1, 4, 5, 6, 2, 3), result.toList())
    }

    @Test
    fun `moving a standalone song around a group`() {
        val entries = listOf(singleSong(0), group("x", 1, 2, 3), singleSong(4))
        val result = moveQueueEntry(entries, fromEntryIndex = 0, toEntryIndex = 2)
        assertValidPermutation(result, entries)
        // Group stays intact; toEntryIndex=2 is a post-removal position: after removing song 0,
        // the remaining entries are [group, song4] (indices 0..1), so index 2 means "append at
        // the end" -- the song ends up after both the group and song 4, not squeezed before them.
        assertContiguousAndOrdered(result, listOf(1, 2, 3))
        assertEquals(listOf(1, 2, 3, 4, 0), result.toList())
    }

    @Test
    fun `multiple groups -- only the moved group relocates, others keep their own contiguity`() {
        val g1 = group("x", 0, 1)
        val g2 = group("y", 4, 5, 6)
        val entries = listOf(g1, singleSong(2), singleSong(3), g2, singleSong(7))
        val result = moveQueueEntry(entries, fromEntryIndex = 3, toEntryIndex = 0)
        assertValidPermutation(result, entries)
        assertContiguousAndOrdered(result, g1.flatIndices)
        assertContiguousAndOrdered(result, g2.flatIndices)
        assertEquals(listOf(4, 5, 6, 0, 1, 2, 3, 7), result.toList())
    }

    @Test
    fun `groups with 2+ songs -- internal order is always preserved`() {
        val g = group("x", 9, 8, 7) // deliberately non-ascending to prove order isn't "fixed"
        val entries = listOf(singleSong(0), g, singleSong(1), singleSong(2))
        for (to in entries.indices) {
            val result = moveQueueEntry(entries, fromEntryIndex = 1, toEntryIndex = to)
            assertValidPermutation(result, entries)
            assertContiguousAndOrdered(result, listOf(9, 8, 7))
        }
    }

    @Test
    fun `every flat index appears exactly once across many from-to combinations`() {
        val entries = listOf(singleSong(0), group("x", 1, 2, 3), singleSong(4), group("y", 5, 6), singleSong(7))
        for (from in entries.indices) {
            for (to in entries.indices) {
                val result = moveQueueEntry(entries, from, to)
                assertValidPermutation(result, entries)
            }
        }
    }

    @Test
    fun `no-group queue -- moveQueueEntry reduces to a plain flat-index move`() {
        val entries = (0..4).map { singleSong(it) }
        val result = moveQueueEntry(entries, fromEntryIndex = 1, toEntryIndex = 3)
        // Identical to MutableList(0..4).move(1, 3): pure flat-index reordering, no grouping.
        assertEquals(listOf(0, 2, 3, 1, 4), result.toList())
    }

    @Test
    fun `no-op move (from equals to) leaves order unchanged`() {
        val entries = listOf(singleSong(0), group("x", 1, 2), singleSong(3))
        val result = moveQueueEntry(entries, fromEntryIndex = 1, toEntryIndex = 1)
        assertEquals(listOf(0, 1, 2, 3), result.toList())
    }

    // ---- computeMoveSequence ----

    @Test
    fun `computeMoveSequence reproduces the target order via simulated moveMediaItem calls`() {
        val cases = listOf(
            intArrayOf(0, 4, 5, 1, 2, 3),
            intArrayOf(2, 3, 4, 0, 1, 5),
            intArrayOf(1, 2, 3, 0, 4),
            intArrayOf(4, 5, 6, 0, 1, 2, 3, 7),
            intArrayOf(0, 1, 2, 3, 4), // identity, no moves needed
        )
        for (target in cases) {
            val moves = computeMoveSequence(target)
            val applied = applyMoveSequence(target.size, moves)
            assertEquals("move sequence $moves did not reproduce target ${target.toList()}", target.toList(), applied)
        }
    }

    @Test
    fun `computeMoveSequence produces no moves for an already-identity order`() {
        val target = intArrayOf(0, 1, 2, 3, 4)
        assertEquals(emptyList<Pair<Int, Int>>(), computeMoveSequence(target))
    }

    @Test
    fun `computeMoveSequence handles empty input`() {
        assertEquals(emptyList<Pair<Int, Int>>(), computeMoveSequence(IntArray(0)))
    }

    @Test
    fun `computeMoveSequence handles single element`() {
        assertEquals(emptyList<Pair<Int, Int>>(), computeMoveSequence(intArrayOf(0)))
    }

    // ---- end-to-end: moveQueueEntry -> computeMoveSequence -> simulated apply ----

    @Test
    fun `end-to-end -- moving a group forward reproduces exactly via moveMediaItem-style calls`() {
        val entries = listOf(singleSong(0), group("x", 1, 2, 3), singleSong(4), singleSong(5))
        val target = moveQueueEntry(entries, fromEntryIndex = 1, toEntryIndex = 3)
        val applied = applyMoveSequence(entries.sumOf { if (it is QueueEntry.Group) it.flatIndices.size else 1 }, computeMoveSequence(target))
        assertEquals(target.toList(), applied)
    }

    @Test
    fun `end-to-end -- full unshuffled pipeline reproduces moveQueueEntry's target via moveMediaItem-style calls`() {
        // Mirrors Queue.kt's real unshuffled-branch usage: entries built from contiguous flat
        // indices (0..n-1, as queueEntries always is when not shuffled), moveQueueEntry
        // computes the target flat order, computeMoveSequence derives the moveMediaItem calls,
        // and applying them must reproduce the target exactly.
        val g = group("x", 2, 3, 4)
        val entries = listOf(singleSong(0), singleSong(1), g, singleSong(5), singleSong(6))
        val totalFlatCount = entries.sumOf { if (it is QueueEntry.Group) it.flatIndices.size else 1 }
        for (from in entries.indices) {
            for (to in entries.indices) {
                val target = moveQueueEntry(entries, from, to)
                val applied = applyMoveSequence(totalFlatCount, computeMoveSequence(target))
                assertEquals("from=$from to=$to", target.toList(), applied)
            }
        }
    }

    @Test
    fun `shuffled-style ordering -- moveQueueEntry stays correct with non-monotonic flat indices`() {
        // Simulates a shuffled display order (entries not in ascending flat-index order) being
        // reordered by a drag -- moveQueueEntry must not assume ascending/sequential input,
        // since for the shuffled case its output feeds DefaultShuffleOrder directly (no
        // computeMoveSequence/physical reorder involved).
        val g = group("x", 10, 11, 12)
        val entries = listOf(singleSong(20), g, singleSong(21), singleSong(5))
        val target = moveQueueEntry(entries, fromEntryIndex = 0, toEntryIndex = 3)
        assertValidPermutation(target, entries)
        assertContiguousAndOrdered(target, g.flatIndices)
    }

    @Test
    fun `interrupted group -- moving the second instance does not affect the first`() {
        val g1 = group("x", 0, 1)
        val a = singleSong(2)
        val g2 = group("x", 3, 4)
        val entries = listOf(g1, a, g2, singleSong(5))
        val result = moveQueueEntry(entries, fromEntryIndex = 2, toEntryIndex = 0)
        assertValidPermutation(result, entries)
        assertContiguousAndOrdered(result, g1.flatIndices)
        assertContiguousAndOrdered(result, g2.flatIndices)
        assertEquals(listOf(3, 4, 0, 1, 2, 5), result.toList())
    }
}
