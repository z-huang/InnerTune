@file:OptIn(ExperimentalSerializationApi::class)

package com.zionhuang.innertube.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class ThumbnailRenderer(
    @JsonNames("croppedSquareThumbnailRenderer")
    val musicThumbnailRenderer: MusicThumbnailRenderer?,
    val musicAnimatedThumbnailRenderer: MusicAnimatedThumbnailRenderer?,
) {
    val isSquare: Boolean
        get() = when {
            musicThumbnailRenderer != null -> musicThumbnailRenderer.thumbnail.thumbnails[0].isSquare
            musicAnimatedThumbnailRenderer != null -> musicAnimatedThumbnailRenderer.backupRenderer.thumbnail.thumbnails[0].isSquare
            else -> throw UnsupportedOperationException("Unknown thumbnail type")
        }

    fun getThumbnails(): List<Thumbnail> = when {
        musicThumbnailRenderer != null -> musicThumbnailRenderer.thumbnail.thumbnails
        musicAnimatedThumbnailRenderer != null -> musicAnimatedThumbnailRenderer.backupRenderer.thumbnail.thumbnails
        else -> throw UnsupportedOperationException("Unknown thumbnail type")
    }

    @Serializable
    data class MusicThumbnailRenderer(
        val thumbnail: Thumbnails,
        val thumbnailCrop: String?,
        val thumbnailScale: String?,
    ) {
        companion object {
            const val MUSIC_THUMBNAIL_CROP_UNSPECIFIED = "MUSIC_THUMBNAIL_CROP_UNSPECIFIED"
            const val MUSIC_THUMBNAIL_SCALE_ASPECT_FIT = "MUSIC_THUMBNAIL_SCALE_ASPECT_FIT"
        }
    }

    @Serializable
    data class MusicAnimatedThumbnailRenderer(
        val animatedThumbnail: Thumbnails,
        val backupRenderer: MusicThumbnailRenderer,
    )
}