package com.zionhuang.music.playback

import com.zionhuang.music.models.MediaMetadata
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for buildQueueEntriesIndexed(List<Pair<Int, MediaMetadata>>) -- the API
 * PlayerConnection.queueEntries actually calls with queueWindows-derived data, where list
 * position (display/shuffle order) and flatIndex (underlying Media3 timeline index,
 * Timeline.Window.firstPeriodIndex) can differ.
 */
class QueueEntryFlatIndexTest {
    private fun song(id: String, queueGroupId: String? = null, queueGroupTitle: String? = null) = MediaMetadata(
        id = id,
        title = id,
        artists = emptyList(),
        duration = 180,
        queueGroupId = queueGroupId,
        queueGroupTitle = queueGroupTitle,
    )

    @Test
    fun `standalone queue in flat order`() {
        val items = listOf(0 to song("a"), 1 to song("b"), 2 to song("c"))

        val entries = buildQueueEntriesIndexed(items)

        assertEquals(
            listOf(QueueEntry.SingleSong(0), QueueEntry.SingleSong(1), QueueEntry.SingleSong(2)),
            entries
        )
    }

    @Test
    fun `grouped album in flat order`() {
        val items = listOf(
            0 to song("a"),
            1 to song("x1", queueGroupId = "x", queueGroupTitle = "Album X"),
            2 to song("x2", queueGroupId = "x", queueGroupTitle = "Album X"),
            3 to song("x3", queueGroupId = "x", queueGroupTitle = "Album X"),
            4 to song("b"),
        )

        val entries = buildQueueEntriesIndexed(items)

        assertEquals(
            listOf(
                QueueEntry.SingleSong(0),
                QueueEntry.Group(groupId = "x", title = "Album X", flatIndices = listOf(1, 2, 3)),
                QueueEntry.SingleSong(4),
            ),
            entries
        )
    }

    @Test
    fun `interrupted group in flat order is not merged`() {
        val items = listOf(
            0 to song("x1", queueGroupId = "x", queueGroupTitle = "Album X"),
            1 to song("x2", queueGroupId = "x", queueGroupTitle = "Album X"),
            2 to song("a"),
            3 to song("x3", queueGroupId = "x", queueGroupTitle = "Album X"),
        )

        val entries = buildQueueEntriesIndexed(items)

        assertEquals(
            listOf(
                QueueEntry.Group(groupId = "x", title = "Album X", flatIndices = listOf(0, 1)),
                QueueEntry.SingleSong(2),
                QueueEntry.Group(groupId = "x", title = "Album X", flatIndices = listOf(3)),
            ),
            entries
        )
    }

    @Test
    fun `shuffled display order is preserved in the resulting entry sequence`() {
        // Real, underlying flat indices: A=2, X1=5, X2=6, B=9 -- deliberately non-sequential
        // and out of numeric order to rule out any accidental reliance on sort order.
        // Display/queue order (as if shuffled): B, X1, X2, A.
        val items = listOf(
            9 to song("b"),
            5 to song("x1", queueGroupId = "x", queueGroupTitle = "Album X"),
            6 to song("x2", queueGroupId = "x", queueGroupTitle = "Album X"),
            2 to song("a"),
        )

        val entries = buildQueueEntriesIndexed(items)

        // Entry sequence must follow display order (B, then group X, then A) -- not flat order.
        assertEquals(3, entries.size)
        assertEquals(QueueEntry.SingleSong(9), entries[0])
        assertEquals(QueueEntry.Group(groupId = "x", title = "Album X", flatIndices = listOf(5, 6)), entries[1])
        assertEquals(QueueEntry.SingleSong(2), entries[2])
    }

    @Test
    fun `flat indices refer to the underlying timeline index, not the position in the display list`() {
        // If flat indices were mistakenly derived from position in the input list rather than
        // the paired Int, this group would come out as flatIndices = [1, 2] instead of [5, 6].
        val items = listOf(
            9 to song("b"),
            5 to song("x1", queueGroupId = "x", queueGroupTitle = "Album X"),
            6 to song("x2", queueGroupId = "x", queueGroupTitle = "Album X"),
            2 to song("a"),
        )

        val entries = buildQueueEntriesIndexed(items)

        val group = entries.filterIsInstance<QueueEntry.Group>().single()
        assertEquals(listOf(5, 6), group.flatIndices)

        val singleSongIndices = entries.filterIsInstance<QueueEntry.SingleSong>().map { it.flatIndex }
        assertEquals(listOf(9, 2), singleSongIndices)
    }
}
