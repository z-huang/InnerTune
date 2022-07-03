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
        fun toSections(): List<Section> = when {
            musicCarouselShelfRenderer != null -> listOf(
                Header(
                    title = musicCarouselShelfRenderer.header.musicCarouselShelfBasicHeaderRenderer.title.toString()
                ),
                CarouselSection(
                    id = musicCarouselShelfRenderer.header.musicCarouselShelfBasicHeaderRenderer.title.toString(),
                    items = musicCarouselShelfRenderer.contents.map { it.toItem() },
                    numItemsPerColumn = musicCarouselShelfRenderer.numItemsPerColumn ?: 1,
                    itemViewType = musicCarouselShelfRenderer.getViewType()
                ))
            musicShelfRenderer != null -> listOfNotNull(
                musicShelfRenderer.toSectionHeader(),
                ListSection(
                    id = musicShelfRenderer.title.toString(),
                    items = musicShelfRenderer.contents.map { it.toItem() },
                    continuation = musicShelfRenderer.continuations?.getContinuation(),
                    itemViewType = musicShelfRenderer.getViewType()
                ))
            musicDescriptionShelfRenderer != null -> listOfNotNull(
                musicDescriptionShelfRenderer.toSectionHeader(),
                DescriptionSection(
                    description = musicDescriptionShelfRenderer.description.toString()
                ))
            gridRenderer != null -> listOfNotNull(
                gridRenderer.header?.toSectionHeader(),
                GridSection(
                    id = gridRenderer.header?.gridHeaderRenderer?.title.toString(),
                    items = gridRenderer.items.map { it.toItem() }
                )
            )
            else -> emptyList()
        }
    }
}