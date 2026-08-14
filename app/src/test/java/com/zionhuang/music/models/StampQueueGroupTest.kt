package com.zionhuang.music.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StampQueueGroupTest {
    private fun song(id: String) = MediaMetadata(
        id = id,
        title = "Title $id",
        artists = listOf(MediaMetadata.Artist(id = "artist-$id", name = "Artist $id")),
        duration = 200,
        thumbnailUrl = "https://example.com/$id.jpg",
        album = MediaMetadata.Album(id = "album1", title = "Album X"),
        explicit = true,
    )

    @Test
    fun `all members receive one shared non-null groupId`() {
        val stamped = List(4) { song("t${it + 1}") }.stampQueueGroup("Album X")

        val groupIds = stamped.map { it.queueGroupId }.toSet()

        assertEquals(1, groupIds.size)
        assertNotNull(groupIds.single())
    }

    @Test
    fun `queueGroupTitle is populated for every member`() {
        val stamped = List(4) { song("t${it + 1}") }.stampQueueGroup("Album X")

        assertTrue(stamped.all { it.queueGroupTitle == "Album X" })
    }

    @Test
    fun `queueGroupIndex is assigned 0,1,2,3 in order`() {
        val stamped = List(4) { song("t${it + 1}") }.stampQueueGroup("Album X")

        assertEquals(listOf(0, 1, 2, 3), stamped.map { it.queueGroupIndex })
    }

    @Test
    fun `queueGroupSize is 4 for every member of a 4-song album`() {
        val stamped = List(4) { song("t${it + 1}") }.stampQueueGroup("Album X")

        assertTrue(stamped.all { it.queueGroupSize == 4 })
    }

    @Test
    fun `two separate queueing operations receive different groupIds`() {
        val songs = List(4) { song("t${it + 1}") }

        val firstAddition = songs.stampQueueGroup("Album X")
        val secondAddition = songs.stampQueueGroup("Album X")

        assertNotEquals(firstAddition.first().queueGroupId, secondAddition.first().queueGroupId)
    }

    @Test
    fun `song order is unchanged`() {
        val songs = listOf(song("t1"), song("t2"), song("t3"), song("t4"))

        val stamped = songs.stampQueueGroup("Album X")

        assertEquals(songs.map { it.id }, stamped.map { it.id })
    }

    @Test
    fun `empty list does not crash and produces no bogus group`() {
        val stamped = emptyList<MediaMetadata>().stampQueueGroup("Album X")

        assertEquals(emptyList<MediaMetadata>(), stamped)
    }

    @Test
    fun `metadata unrelated to grouping is preserved`() {
        val original = song("t1")

        val stamped = listOf(original).stampQueueGroup("Album X").single()

        assertEquals(original.id, stamped.id)
        assertEquals(original.title, stamped.title)
        assertEquals(original.artists, stamped.artists)
        assertEquals(original.duration, stamped.duration)
        assertEquals(original.thumbnailUrl, stamped.thumbnailUrl)
        assertEquals(original.album, stamped.album)
        assertEquals(original.explicit, stamped.explicit)
    }
}
