package com.zionhuang.music.playback

import com.zionhuang.music.models.MediaMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QueueEntryTest {
    private fun song(
        id: String,
        queueGroupId: String? = null,
        queueGroupTitle: String? = null,
        queueGroupIndex: Int? = null,
        queueGroupSize: Int? = null,
    ) = MediaMetadata(
        id = id,
        title = id,
        artists = emptyList(),
        duration = 180,
        queueGroupId = queueGroupId,
        queueGroupTitle = queueGroupTitle,
        queueGroupIndex = queueGroupIndex,
        queueGroupSize = queueGroupSize,
    )

    @Test
    fun `empty queue produces no entries`() {
        assertEquals(emptyList<QueueEntry>(), buildQueueEntries(emptyList()))
    }

    @Test
    fun `queue of only standalone songs produces one SingleSong per item in order`() {
        val items = listOf(song("s0"), song("s1"), song("s2"))

        val entries = buildQueueEntries(items)

        assertEquals(
            listOf(
                QueueEntry.SingleSong(0),
                QueueEntry.SingleSong(1),
                QueueEntry.SingleSong(2),
            ),
            entries
        )
    }

    @Test
    fun `one group in the middle of standalone songs`() {
        val items = listOf(
            song("s0"),
            song("a1", queueGroupId = "album-a", queueGroupTitle = "Album A", queueGroupIndex = 0, queueGroupSize = 3),
            song("a2", queueGroupId = "album-a", queueGroupTitle = "Album A", queueGroupIndex = 1, queueGroupSize = 3),
            song("a3", queueGroupId = "album-a", queueGroupTitle = "Album A", queueGroupIndex = 2, queueGroupSize = 3),
            song("s4"),
        )

        val entries = buildQueueEntries(items)

        assertEquals(
            listOf(
                QueueEntry.SingleSong(0),
                QueueEntry.Group(groupId = "album-a", title = "Album A", flatIndices = listOf(1, 2, 3)),
                QueueEntry.SingleSong(4),
            ),
            entries
        )
    }

    @Test
    fun `group at the beginning of the queue`() {
        val items = listOf(
            song("a1", queueGroupId = "album-a", queueGroupTitle = "Album A"),
            song("a2", queueGroupId = "album-a", queueGroupTitle = "Album A"),
            song("s2"),
        )

        val entries = buildQueueEntries(items)

        assertEquals(
            listOf(
                QueueEntry.Group(groupId = "album-a", title = "Album A", flatIndices = listOf(0, 1)),
                QueueEntry.SingleSong(2),
            ),
            entries
        )
    }

    @Test
    fun `group at the end of the queue`() {
        val items = listOf(
            song("s0"),
            song("a1", queueGroupId = "album-a", queueGroupTitle = "Album A"),
            song("a2", queueGroupId = "album-a", queueGroupTitle = "Album A"),
        )

        val entries = buildQueueEntries(items)

        assertEquals(
            listOf(
                QueueEntry.SingleSong(0),
                QueueEntry.Group(groupId = "album-a", title = "Album A", flatIndices = listOf(1, 2)),
            ),
            entries
        )
    }

    @Test
    fun `two different adjacent groups are kept separate`() {
        val items = listOf(
            song("a1", queueGroupId = "album-a", queueGroupTitle = "Album A"),
            song("a2", queueGroupId = "album-a", queueGroupTitle = "Album A"),
            song("b1", queueGroupId = "album-b", queueGroupTitle = "Album B"),
            song("b2", queueGroupId = "album-b", queueGroupTitle = "Album B"),
        )

        val entries = buildQueueEntries(items)

        assertEquals(
            listOf(
                QueueEntry.Group(groupId = "album-a", title = "Album A", flatIndices = listOf(0, 1)),
                QueueEntry.Group(groupId = "album-b", title = "Album B", flatIndices = listOf(2, 3)),
            ),
            entries
        )
    }

    @Test
    fun `same group id interrupted by a standalone song produces two separate groups`() {
        val items = listOf(
            song("a1", queueGroupId = "album-a", queueGroupTitle = "Album A"),
            song("a2", queueGroupId = "album-a", queueGroupTitle = "Album A"),
            song("songX"),
            song("a3", queueGroupId = "album-a", queueGroupTitle = "Album A"),
        )

        val entries = buildQueueEntries(items)

        assertEquals(
            listOf(
                QueueEntry.Group(groupId = "album-a", title = "Album A", flatIndices = listOf(0, 1)),
                QueueEntry.SingleSong(2),
                QueueEntry.Group(groupId = "album-a", title = "Album A", flatIndices = listOf(3)),
            ),
            entries
        )
    }

    @Test
    fun `inconsistent group titles use the first member's title without crashing`() {
        val items = listOf(
            song("a1", queueGroupId = "album-a", queueGroupTitle = "Album A"),
            song("a2", queueGroupId = "album-a", queueGroupTitle = "Different Title"),
        )

        val entries = buildQueueEntries(items)

        assertEquals(
            listOf(QueueEntry.Group(groupId = "album-a", title = "Album A", flatIndices = listOf(0, 1))),
            entries
        )
    }

    @Test
    fun `group containing two songs`() {
        val items = listOf(
            song("a1", queueGroupId = "album-a", queueGroupTitle = "Album A"),
            song("a2", queueGroupId = "album-a", queueGroupTitle = "Album A"),
        )

        val entries = buildQueueEntries(items)

        assertEquals(1, entries.size)
        val group = entries.single() as QueueEntry.Group
        assertEquals(2, group.flatIndices.size)
    }

    @Test
    fun `every original flat index appears exactly once across the resulting entries`() {
        val items = listOf(
            song("s0"),
            song("a1", queueGroupId = "album-a", queueGroupTitle = "Album A"),
            song("a2", queueGroupId = "album-a", queueGroupTitle = "Album A"),
            song("s3"),
            song("b1", queueGroupId = "album-b", queueGroupTitle = "Album B"),
            song("s5"),
        )

        val entries = buildQueueEntries(items)

        val allIndices = entries.flatMap {
            when (it) {
                is QueueEntry.SingleSong -> listOf(it.flatIndex)
                is QueueEntry.Group -> it.flatIndices
            }
        }

        assertEquals(items.indices.toList(), allIndices.sorted())
        assertEquals(allIndices.size, allIndices.toSet().size)
    }

    @Test
    fun `standalone items remain in their original relative order`() {
        val items = listOf(
            song("s0"),
            song("a1", queueGroupId = "album-a", queueGroupTitle = "Album A"),
            song("a2", queueGroupId = "album-a", queueGroupTitle = "Album A"),
            song("s3"),
            song("s4"),
        )

        val entries = buildQueueEntries(items)

        val standaloneIndices = entries.filterIsInstance<QueueEntry.SingleSong>().map { it.flatIndex }

        assertEquals(listOf(0, 3, 4), standaloneIndices)
    }

    @Test
    fun `group members remain in their original flat-index order`() {
        val items = listOf(
            song("a1", queueGroupId = "album-a", queueGroupTitle = "Album A"),
            song("a2", queueGroupId = "album-a", queueGroupTitle = "Album A"),
            song("a3", queueGroupId = "album-a", queueGroupTitle = "Album A"),
        )

        val entries = buildQueueEntries(items)

        val group = entries.single() as QueueEntry.Group
        assertTrue(group.flatIndices == group.flatIndices.sorted())
        assertEquals(listOf(0, 1, 2), group.flatIndices)
    }
}
