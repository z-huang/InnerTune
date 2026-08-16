package com.zionhuang.music.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GroupAwareShuffleTest {
    private fun singleSong(flatIndex: Int) = QueueEntry.SingleSong(flatIndex)
    private fun group(groupId: String, vararg flatIndices: Int, title: String = "Album $groupId") =
        QueueEntry.Group(groupId, title, flatIndices.toList())

    private fun expectedFlatIndices(entries: List<QueueEntry>): Set<Int> = entries.flatMap {
        when (it) {
            is QueueEntry.SingleSong -> listOf(it.flatIndex)
            is QueueEntry.Group -> it.flatIndices
        }
    }.toSet()

    /** Asserts every flat index from [entries] appears exactly once in [result] -- a valid permutation. */
    private fun assertValidPermutation(result: IntArray, entries: List<QueueEntry>) {
        val expected = expectedFlatIndices(entries)
        assertEquals("wrong number of items in permutation", expected.size, result.size)
        assertEquals("permutation does not contain exactly the expected flat indices", expected, result.toSet())
    }

    /** Asserts [groupFlatIndices] appears as one contiguous block, in that exact internal order, in [result]. */
    private fun assertContiguousAndOrdered(result: IntArray, groupFlatIndices: List<Int>) {
        val start = result.indexOf(groupFlatIndices.first())
        assertTrue("group's first member (${groupFlatIndices.first()}) not found in $result", start != -1)
        assertTrue("group extends past array bounds: start=$start size=${groupFlatIndices.size} result=${result.toList()}", start + groupFlatIndices.size <= result.size)
        val slice = result.toList().subList(start, start + groupFlatIndices.size)
        assertEquals("group members are not contiguous/in original order", groupFlatIndices, slice)
    }

    @Test
    fun `empty queue produces an empty permutation`() {
        val result = buildGroupAwareShuffleOrder(emptyList(), currentFlatIndex = 0)
        assertEquals(0, result.size)
    }

    @Test
    fun `one standalone song`() {
        val entries = listOf(singleSong(0))
        val result = buildGroupAwareShuffleOrder(entries, currentFlatIndex = 0)
        assertEquals(intArrayOf(0).toList(), result.toList())
    }

    @Test
    fun `all standalone songs -- valid permutation, current first`() {
        val entries = (0..4).map { singleSong(it) }
        repeat(50) {
            val result = buildGroupAwareShuffleOrder(entries, currentFlatIndex = 2, random = Random(it.toLong()))
            assertValidPermutation(result, entries)
            assertEquals(2, result[0])
        }
    }

    @Test
    fun `one group plus standalone songs -- group stays contiguous and ordered`() {
        val g = group("x", 1, 2, 3)
        val entries = listOf(singleSong(0), g, singleSong(4))
        repeat(50) {
            val result = buildGroupAwareShuffleOrder(entries, currentFlatIndex = 0, random = Random(it.toLong()))
            assertValidPermutation(result, entries)
            assertContiguousAndOrdered(result, g.flatIndices)
        }
    }

    @Test
    fun `multiple groups of different sizes -- each stays contiguous and ordered`() {
        val g1 = group("x", 0, 1)
        val g2 = group("y", 2, 3, 4, 5)
        val entries = listOf(g1, singleSong(6), g2, singleSong(7))
        repeat(50) {
            val result = buildGroupAwareShuffleOrder(entries, currentFlatIndex = 6, random = Random(it.toLong()))
            assertValidPermutation(result, entries)
            assertContiguousAndOrdered(result, g1.flatIndices)
            assertContiguousAndOrdered(result, g2.flatIndices)
        }
    }

    @Test
    fun `current item is a standalone song -- its entry is first, all others follow`() {
        val entries = listOf(singleSong(10), singleSong(11), singleSong(12), singleSong(13))
        repeat(50) {
            val result = buildGroupAwareShuffleOrder(entries, currentFlatIndex = 12, random = Random(it.toLong()))
            assertValidPermutation(result, entries)
            assertEquals(12, result[0])
        }
    }

    @Test
    fun `current item is inside a group -- containing group is first, contiguous, internally unchanged`() {
        val g = group("x", 4, 5, 6)
        val entries = listOf(singleSong(0), g, singleSong(7))
        repeat(50) {
            val result = buildGroupAwareShuffleOrder(entries, currentFlatIndex = 5, random = Random(it.toLong()))
            assertValidPermutation(result, entries)
            // The containing group's block must start at position 0.
            assertEquals(listOf(4, 5, 6), result.toList().subList(0, 3))
        }
    }

    @Test
    fun `every flat index appears exactly once across many randomized runs`() {
        val entries = listOf(singleSong(0), group("x", 1, 2, 3), singleSong(4), group("y", 5, 6), singleSong(7))
        repeat(200) { seed ->
            val result = buildGroupAwareShuffleOrder(entries, currentFlatIndex = 0, random = Random(seed.toLong()))
            assertValidPermutation(result, entries)
        }
    }

    @Test
    fun `no group is ever split across many randomized runs`() {
        val g1 = group("x", 1, 2, 3)
        val g2 = group("y", 5, 6)
        val entries = listOf(singleSong(0), g1, singleSong(4), g2, singleSong(7))
        repeat(200) { seed ->
            val result = buildGroupAwareShuffleOrder(entries, currentFlatIndex = 4, random = Random(seed.toLong()))
            assertContiguousAndOrdered(result, g1.flatIndices)
            assertContiguousAndOrdered(result, g2.flatIndices)
        }
    }

    @Test
    fun `group internal order is always preserved, never internally reordered`() {
        val g = group("x", 9, 8, 7) // deliberately non-ascending to prove order is preserved as-given, not sorted
        val entries = listOf(singleSong(0), g, singleSong(1))
        repeat(50) {
            val result = buildGroupAwareShuffleOrder(entries, currentFlatIndex = 0, random = Random(it.toLong()))
            assertContiguousAndOrdered(result, listOf(9, 8, 7))
        }
    }

    @Test
    fun `two separate Group entries with the same groupId remain two independent shuffle units`() {
        // Mirrors QueueEntry's "interrupted group" output: same groupId, two distinct instances.
        val g1 = group("x", 0, 1)
        val g2 = group("x", 5, 6)
        val entries = listOf(g1, singleSong(9), g2)
        repeat(50) {
            val result = buildGroupAwareShuffleOrder(entries, currentFlatIndex = 9, random = Random(it.toLong()))
            assertValidPermutation(result, entries)
            // Each instance's own members stay contiguous/ordered independently -- if they had
            // been accidentally merged by groupId, this combined 4-element expectation would be
            // the only way to satisfy contiguity, which the independent checks below don't require.
            assertContiguousAndOrdered(result, g1.flatIndices)
            assertContiguousAndOrdered(result, g2.flatIndices)
        }
    }

    @Test
    fun `interrupted group -- Group X, Song A, Group X -- remains two separate shuffle units`() {
        val g1 = group("x", 0, 1)
        val a = singleSong(2)
        val g2 = group("x", 3)
        val entries = listOf(g1, a, g2)
        repeat(50) {
            val result = buildGroupAwareShuffleOrder(entries, currentFlatIndex = 2, random = Random(it.toLong()))
            assertValidPermutation(result, entries)
            assertContiguousAndOrdered(result, g1.flatIndices)
            assertContiguousAndOrdered(result, g2.flatIndices)
        }
    }

    @Test
    fun `singleton group behaves as one atomic shuffle unit`() {
        val g = group("x", 7)
        val entries = listOf(singleSong(0), singleSong(1), g, singleSong(2))
        repeat(50) {
            val result = buildGroupAwareShuffleOrder(entries, currentFlatIndex = 0, random = Random(it.toLong()))
            assertValidPermutation(result, entries)
            assertContiguousAndOrdered(result, listOf(7))
        }
    }

    @Test
    fun `repeated randomized runs -- invariants hold every time across many distinct entries`() {
        val g1 = group("x", 10, 11, 12)
        val g2 = group("y", 20, 21)
        val g3 = group("x", 30) // separate instance, same id as g1
        val entries = listOf(singleSong(0), g1, singleSong(1), g2, g3, singleSong(2))
        repeat(300) { seed ->
            val result = buildGroupAwareShuffleOrder(entries, currentFlatIndex = 11, random = Random(seed.toLong()))
            assertValidPermutation(result, entries)
            assertContiguousAndOrdered(result, g1.flatIndices)
            assertContiguousAndOrdered(result, g2.flatIndices)
            assertContiguousAndOrdered(result, g3.flatIndices)
            // current item (11) is inside g1, so g1's block must start at position 0.
            assertEquals(listOf(10, 11, 12), result.toList().subList(0, 3))
        }
    }

    @Test
    fun `explicit current-item semantics -- group 10,11,12 with 11 current stays exactly in that order first`() {
        val g = group("x", 10, 11, 12)
        val entries = listOf(g, singleSong(20), singleSong(21))
        repeat(100) {
            val result = buildGroupAwareShuffleOrder(entries, currentFlatIndex = 11, random = Random(it.toLong()))
            assertValidPermutation(result, entries)
            val firstThree = result.toList().subList(0, 3)
            assertEquals("expected [10, 11, 12] contiguous in original order first, got $firstThree", listOf(10, 11, 12), firstThree)
            // Explicitly rule out the incorrect orderings called out in the task.
            assertTrue(firstThree != listOf(11, 10, 12))
            assertTrue(firstThree != listOf(10, 12, 11))
            assertTrue(result.toList() != listOf(11, 20, 21, 10, 12))
        }
    }
}
