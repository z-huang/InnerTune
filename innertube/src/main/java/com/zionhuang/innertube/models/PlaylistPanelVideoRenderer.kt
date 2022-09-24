package com.zionhuang.innertube.models

import com.zionhuang.innertube.utils.TimeParser
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistPanelVideoRenderer(
    val title: Runs?,
    val lengthText: Runs?,
    val longBylineText: Runs?,
    val shortBylineText: Runs?,
    val videoId: String?,
    val playlistSetVideoId: String?,
    val selected: Boolean,
    val thumbnail: Thumbnails,
    val unplayableText: Runs?,
    val menu: Menu?,
    val navigationEndpoint: NavigationEndpoint,
) {
    // Best way to get the most detailed song information
    fun toSongItem(): SongItem? {
        if (videoId == null || title == null || lengthText == null || longBylineText == null || shortBylineText == null || menu == null) return null
        val longByLineRuns = longBylineText.runs.splitBySeparator()
        return SongItem(
            id = videoId,
            title = title.toString(),
            subtitle = longBylineText.toString(),
            artists = longByLineRuns[0].oddElements(),
            album = longBylineText.runs
                .find { it.navigationEndpoint?.getEndpointType() == ITEM_ALBUM }
                ?.toLink(),
            albumYear = longByLineRuns.getOrNull(2)?.getOrNull(0)?.text?.toIntOrNull(),
            duration = TimeParser.parse(lengthText.runs[0].text),
            thumbnails = thumbnail.thumbnails,
            menu = menu.toItemMenu(),
            navigationEndpoint = navigationEndpoint.copy(
                // remove watchEndpoint params so that we can get queue items
                watchEndpoint = navigationEndpoint.watchEndpoint?.copy(params = null)
            )
        )
    }
}