@file:OptIn(ExperimentalSerializationApi::class)

package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoints.NavigationEndpoint
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
        fun toSection(): Section? {
            return when {
                musicCarouselShelfRenderer != null -> ItemSection(
                    title = musicCarouselShelfRenderer.header.musicCarouselShelfBasicHeaderRenderer.title.toString(),
                    items = musicCarouselShelfRenderer.contents.map { it.toItem() }
                )
                musicShelfRenderer != null -> toItemSection()
                musicDescriptionShelfRenderer != null -> DescriptionSection(
                    title = musicDescriptionShelfRenderer.header.toString(),
                    subtitle = musicDescriptionShelfRenderer.subheader.toString(),
                    description = musicDescriptionShelfRenderer.description.toString()
                )
                gridRenderer != null -> ItemSection(
                    items = gridRenderer.items.map { it.toItem() }
                )
                else -> null
            }
        }

        fun toItemSection() = ItemSection(
            title = musicShelfRenderer!!.title.toString(),
            items = musicShelfRenderer.contents.map { it.toItem() },
            continuation = musicShelfRenderer.continuations?.getContinuation(),
            bottomEndpoint = musicShelfRenderer.bottomEndpoint?.searchEndpoint
        )
    }
}