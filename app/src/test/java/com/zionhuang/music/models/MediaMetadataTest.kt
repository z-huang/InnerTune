package com.zionhuang.music.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class MediaMetadataTest {
    private fun sampleMetadata(
        queueGroupId: String? = null,
        queueGroupTitle: String? = null,
        queueGroupIndex: Int? = null,
        queueGroupSize: Int? = null,
    ) = MediaMetadata(
        id = "song1",
        title = "Test Song",
        artists = listOf(MediaMetadata.Artist(id = "artist1", name = "Test Artist")),
        duration = 180,
        thumbnailUrl = "https://example.com/thumb.jpg",
        album = MediaMetadata.Album(id = "album1", title = "Test Album"),
        explicit = false,
        queueGroupId = queueGroupId,
        queueGroupTitle = queueGroupTitle,
        queueGroupIndex = queueGroupIndex,
        queueGroupSize = queueGroupSize,
    )

    @Test
    fun `construction without group fields still works`() {
        val metadata = MediaMetadata(
            id = "song1",
            title = "Test Song",
            artists = listOf(MediaMetadata.Artist(id = "artist1", name = "Test Artist")),
            duration = 180,
            thumbnailUrl = "https://example.com/thumb.jpg",
            album = MediaMetadata.Album(id = "album1", title = "Test Album"),
            explicit = false
        )

        assertEquals("song1", metadata.id)
        assertEquals("Test Song", metadata.title)
    }

    @Test
    fun `group fields default to null`() {
        val metadata = sampleMetadata()

        assertNull(metadata.queueGroupId)
        assertNull(metadata.queueGroupTitle)
        assertNull(metadata.queueGroupIndex)
        assertNull(metadata.queueGroupSize)
    }

    @Test
    fun `group fields survive Java serialization round-trip`() {
        val original = sampleMetadata(
            queueGroupId = "test-group",
            queueGroupTitle = "Test Album",
            queueGroupIndex = 2,
            queueGroupSize = 10
        )

        val bytes = ByteArrayOutputStream().apply {
            ObjectOutputStream(this).use { it.writeObject(original) }
        }.toByteArray()

        val restored = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as MediaMetadata }

        assertEquals(original, restored)
        assertEquals("test-group", restored.queueGroupId)
        assertEquals("Test Album", restored.queueGroupTitle)
        assertEquals(2, restored.queueGroupIndex)
        assertEquals(10, restored.queueGroupSize)
    }
}
