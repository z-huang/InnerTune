package com.zionhuang.innertube.models.response

import com.zionhuang.innertube.models.*
import kotlinx.serialization.Serializable

@Serializable
data class BrowseResponse(
    val contents: Contents?,
    val continuationContents: ContinuationContents?,
    val header: Header?,
    val microformat: Microformat?,
    val responseContext: ResponseContext,
) {
    fun toBrowseResult(): BrowseResult = when {
        continuationContents != null -> when {
            continuationContents.sectionListContinuation != null -> BrowseResult(
                items = continuationContents.sectionListContinuation.contents.flatMap { it.toBaseItems() },
                continuations = continuationContents.sectionListContinuation.continuations?.getContinuations()
            )
            continuationContents.musicPlaylistShelfContinuation != null -> BrowseResult(
                items = continuationContents.musicPlaylistShelfContinuation.contents.mapNotNull { it.toItem() },
                continuations = continuationContents.musicPlaylistShelfContinuation.continuations?.getContinuations()
            )
            else -> throw UnsupportedOperationException("Unknown continuation type")
        }
        contents != null -> BrowseResult(
            items = contents.singleColumnBrowseResultsRenderer!!.tabs[0].tabRenderer.content!!.sectionListRenderer!!.contents.flatMap { it.toBaseItems() },
            continuations = contents.singleColumnBrowseResultsRenderer.tabs[0].tabRenderer.content!!.sectionListRenderer!!.contents[0].musicPlaylistShelfRenderer?.continuations?.getContinuations().orEmpty()
                    + contents.singleColumnBrowseResultsRenderer.tabs[0].tabRenderer.content!!.sectionListRenderer!!.continuations?.getContinuations().orEmpty()
        )
        else -> BrowseResult(
            items = emptyList(),
            continuations = null
        )
    }.addHeader(header?.toHeader())

    @Serializable
    data class Contents(
        val singleColumnBrowseResultsRenderer: Tabs?,
        val sectionListRenderer: SectionListRenderer?,
    )

    @Serializable
    data class ContinuationContents(
        val sectionListContinuation: SectionListContinuation?,
        val musicPlaylistShelfContinuation: MusicPlaylistShelfContinuation?,
    ) {
        @Serializable
        data class SectionListContinuation(
            val contents: List<SectionListRenderer.Content>,
            val continuations: List<Continuation>?,
        )

        @Serializable
        data class MusicPlaylistShelfContinuation(
            val contents: List<MusicShelfRenderer.Content>,
            val continuations: List<Continuation>?,
        )
    }

    @Serializable
    data class Header(
        val musicImmersiveHeaderRenderer: MusicImmersiveHeaderRenderer?,
        val musicDetailHeaderRenderer: MusicDetailHeaderRenderer?,
    ) {
        fun toHeader(): YTBaseItem? = when {
            musicImmersiveHeaderRenderer != null -> ArtistHeader(
                id = musicImmersiveHeaderRenderer.title.toString(),
                name = musicImmersiveHeaderRenderer.title.toString(),
                description = musicImmersiveHeaderRenderer.description?.toString(),
                bannerThumbnails = musicImmersiveHeaderRenderer.thumbnail.getThumbnails(),
                shuffleEndpoint = musicImmersiveHeaderRenderer.playButton?.buttonRenderer?.navigationEndpoint,
                radioEndpoint = musicImmersiveHeaderRenderer.startRadioButton?.buttonRenderer?.navigationEndpoint,
            )
            musicDetailHeaderRenderer != null -> {
                val subtitle = musicDetailHeaderRenderer.subtitle.runs.splitBySeparator()
                AlbumOrPlaylistHeader(
                    id = musicDetailHeaderRenderer.title.toString(),
                    name = musicDetailHeaderRenderer.title.toString(),
                    subtitle = musicDetailHeaderRenderer.subtitle.toString(),
                    secondSubtitle = musicDetailHeaderRenderer.secondSubtitle.toString(),
                    description = musicDetailHeaderRenderer.description?.toString(),
                    artists = subtitle.getOrNull(1)?.oddElements(),
                    year = subtitle.getOrNull(2)?.firstOrNull()?.text?.toIntOrNull(),
                    thumbnails = musicDetailHeaderRenderer.thumbnail.getThumbnails(),
                    menu = musicDetailHeaderRenderer.menu.toItemMenu()
                )
            }
            else -> null
        }

        @Serializable
        data class MusicImmersiveHeaderRenderer(
            val title: Runs,
            val description: Runs?,
            val thumbnail: ThumbnailRenderer,
            val playButton: Button?,
            val startRadioButton: Button?,
            val menu: Menu,
        )

        @Serializable
        data class MusicDetailHeaderRenderer(
            val title: Runs,
            val subtitle: Runs,
            val secondSubtitle: Runs,
            val description: Runs?,
            val thumbnail: ThumbnailRenderer,
            val menu: Menu,
        )
    }

    @Serializable
    data class Microformat(
        val microformatDataRenderer: MicroformatDataRenderer,
    ) {
        @Serializable
        data class MicroformatDataRenderer(
            val urlCanonical: String,
        )
    }
}
