package com.zionhuang.innertube.models.response

import com.zionhuang.innertube.models.*
import kotlinx.serialization.Serializable

@Serializable
data class BrowseResponse(
    val contents: Contents,
    val continuationContents: ContinuationContents?,
    val header: Header?,
    val microformat: Microformat?,
) {
    fun toSectionList() = contents.singleColumnBrowseResultsRenderer!!.tabs[0].tabRenderer.content!!.sectionListRenderer!!.contents.mapNotNull {
        it.toSection()
    }

    fun toBrowseResult() =
        if (continuationContents == null)
            BrowseResult(
                sections = toSectionList(),
                continuation = contents.singleColumnBrowseResultsRenderer!!.tabs[0].tabRenderer.content!!.sectionListRenderer!!.continuations?.getContinuation()
            )
        else
            BrowseResult(
                sections = continuationContents.sectionListContinuation.contents.mapNotNull { it.toSection() },
                continuation = continuationContents.sectionListContinuation.continuations?.getContinuation()
            )

    @Serializable
    data class Contents(
        val singleColumnBrowseResultsRenderer: Tabs?,
        val sectionListRenderer: SectionListRenderer?,
    )

    @Serializable
    data class ContinuationContents(
        val sectionListContinuation: SectionListContinuation,
    ) {
        @Serializable
        data class SectionListContinuation(
            val contents: List<SectionListRenderer.Content>,
            val continuations: List<Continuation>?,
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
