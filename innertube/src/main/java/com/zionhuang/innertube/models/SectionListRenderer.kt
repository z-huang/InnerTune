@file:OptIn(ExperimentalSerializationApi::class)

package com.zionhuang.innertube.models

import com.zionhuang.innertube.utils.plus
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class SectionListRenderer(
    val header: Header?,
    val contents: List<Content>?,
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
        fun toBaseItems(): List<YTBaseItem> = when {
            musicCarouselShelfRenderer != null -> listOfNotNull(
                musicCarouselShelfRenderer.header?.toHeader(),
                CarouselSection(
                    id = musicCarouselShelfRenderer.header?.musicCarouselShelfBasicHeaderRenderer?.title.toString(),
                    items = musicCarouselShelfRenderer.contents.mapNotNull { it.toBaseItem() },
                    numItemsPerColumn = musicCarouselShelfRenderer.numItemsPerColumn ?: 1,
                    itemViewType = musicCarouselShelfRenderer.getViewType()
                )
            )
            musicShelfRenderer != null -> musicShelfRenderer.contents?.mapNotNull { it.toItem() }.orEmpty().let { items ->
                if (items.isNotEmpty()) musicShelfRenderer.toHeader() + items
                else items
            }
            musicPlaylistShelfRenderer != null -> musicPlaylistShelfRenderer.contents.mapNotNull { it.toItem() }
            musicDescriptionShelfRenderer != null -> listOfNotNull(
                musicDescriptionShelfRenderer.toSectionHeader(),
                DescriptionSection(
                    description = musicDescriptionShelfRenderer.description.toString()
                )
            )
            gridRenderer != null -> if (gridRenderer.items[0].toBaseItem().let { it is NavigationItem && it.stripeColor == null }) {
                // bring NavigationItems out to separate items
                gridRenderer.header?.toSectionHeader() + gridRenderer.items.map { it.toBaseItem() }
            } else {
                listOfNotNull(
                    gridRenderer.header?.toSectionHeader(),
                    GridSection(
                        id = gridRenderer.header?.gridHeaderRenderer?.title.toString(),
                        items = gridRenderer.items.map { it.toBaseItem() }
                    )
                )
            }
            else -> emptyList()
        }
    }
}