package com.zionhuang.innertube.models.response

import com.zionhuang.innertube.models.*
import kotlinx.serialization.Serializable

@Serializable
data class BrowseResponse(
    val contents: Contents?,
    val continuationContents: ContinuationContents?,
    val header: Header?,
    val microformat: Microformat?,
) {
    fun toBrowseResult() = when {
        contents != null -> BrowseResult(
            sections = contents.singleColumnBrowseResultsRenderer!!.tabs[0].tabRenderer.content!!.sectionListRenderer!!.contents.flatMap { it.toSections() },
            continuation = contents.singleColumnBrowseResultsRenderer.tabs[0].tabRenderer.content!!.sectionListRenderer!!.continuations?.getContinuation()
        )
        continuationContents != null -> when {
            continuationContents.sectionListContinuation != null -> BrowseResult(
                sections = continuationContents.sectionListContinuation.contents.flatMap { it.toSections() },
                continuation = continuationContents.sectionListContinuation.continuations?.getContinuation()
            )
            continuationContents.musicPlaylistShelfContinuation != null -> BrowseResult(
                sections = listOf(
                    ListSection(
                        id = continuationContents.musicPlaylistShelfContinuation.continuation.getContinuation(),
                        items = continuationContents.musicPlaylistShelfContinuation.contents.map { it.toItem() },
                        continuation = continuationContents.musicPlaylistShelfContinuation.continuation.getContinuation(),
                        itemViewType = Section.ViewType.LIST
                    )
                ),
                continuation = continuationContents.musicPlaylistShelfContinuation.continuation.getContinuation()
            )
            else -> throw UnsupportedOperationException("Unknown continuation type")
        }
        else -> throw UnsupportedOperationException("Unknown response")
    }

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
            val continuation: List<Continuation>,
        )
    }

    @Serializable
    data class Header(
        val musicImmersiveHeaderRenderer: MusicImmersiveHeaderRenderer?,
        val musicDetailHeaderRenderer: MusicDetailHeaderRenderer?,
    ) {
        @Serializable
        data class MusicImmersiveHeaderRenderer(
            val title: Runs,
            val description: Runs,
            val thumbnail: ThumbnailRenderer,
            val playButton: Button,
            val startRadioButton: Button,
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
