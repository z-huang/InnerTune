@file:OptIn(ExperimentalSerializationApi::class)

package com.zionhuang.innertube.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class SectionListRenderer(
    val header: Header?,
    val contents: List<Content>,
    val continuations: List<Continuation>?,
) {
    @Serializable
    data class Header(
        val chipCloudRenderer: ChipCloudRenderer,
    ) {
        @Serializable
        data class ChipCloudRenderer(
            val chips: List<Chip>,
        ) {
            @Serializable
            data class Chip(
                val chipCloudChipRenderer: ChipCloudChipRenderer,
            ) {
                @Serializable
                data class ChipCloudChipRenderer(
                    val isSelected: Boolean,
                    val navigationEndpoint: NavigationEndpoint,
                    // The close button doesn't have the following two fields
                    val text: Runs?,
                    val uniqueId: String?,
                )

                fun toFilter() = Filter(
                    text = chipCloudChipRenderer.text.toString(),
                    searchEndpoint = chipCloudChipRenderer.navigationEndpoint.searchEndpoint!!
                )
            }
        }
    }

    @Serializable
    data class Content(
        @JsonNames("musicImmersiveCarouselShelfRenderer")
        val musicCarouselShelfRenderer: MusicCarouselShelfRenderer?,
        val musicShelfRenderer: MusicShelfRenderer?,
        val musicPlaylistShelfRenderer: MusicPlaylistShelfRenderer?,
        val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer?,
        val gridRenderer: GridRenderer?,
    ) {
        fun toSection(): Section? = when {
            musicCarouselShelfRenderer != null -> CarouselSection(
                header = Section.Header(
                    title = musicCarouselShelfRenderer.header.musicCarouselShelfBasicHeaderRenderer.title.toString(),
                ),
                items = musicCarouselShelfRenderer.contents.map { it.toItem() },
                numItemsPerColumn = musicCarouselShelfRenderer.numItemsPerColumn ?: 1,
                itemViewType = musicCarouselShelfRenderer.getViewType()
            )
            musicShelfRenderer != null -> toItemSection()
            musicDescriptionShelfRenderer != null -> DescriptionSection(
                header = Section.Header(
                    title = musicDescriptionShelfRenderer.header.toString(),
                    subtitle = musicDescriptionShelfRenderer.subheader.toString(),
                ),
                description = musicDescriptionShelfRenderer.description.toString()
            )
            gridRenderer != null -> GridSection(
                header = gridRenderer.header?.gridHeaderRenderer?.title?.toString()?.let {
                    Section.Header(
                        title = it
                    )
                },
                items = gridRenderer.items.map { it.toItem() }
            )
            else -> null
        }

        fun toItemSection() = ListSection(
            header = Section.Header(
                title = musicShelfRenderer!!.title.toString(),
                moreNavigationEndpoint = musicShelfRenderer.bottomEndpoint,
            ),
            items = musicShelfRenderer.contents.map { it.toItem() },
            continuation = musicShelfRenderer.continuations?.getContinuation(),
            itemViewType = musicShelfRenderer.getViewType()
        )
    }
}