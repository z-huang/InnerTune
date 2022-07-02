package com.zionhuang.music.utils

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zionhuang.innertube.models.NavigationEndpoint
import com.zionhuang.innertube.models.endpoint.*
import com.zionhuang.innertube.models.endpoint.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ALBUM
import com.zionhuang.music.ui.fragments.youtube.YouTubeAlbumFragmentDirections.openYouTubeAlbumFragment
import com.zionhuang.music.ui.fragments.youtube.YouTubeBrowseFragmentDirections.openYouTubeBrowseFragment

class NavigationEndpointHandler(private val fragment: Fragment) {
    fun handle(navigationEndpoint: NavigationEndpoint) = when (val endpoint = navigationEndpoint.endpoint) {
        is WatchEndpoint -> {}
        is BrowseEndpoint -> when (endpoint.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType) {
            MUSIC_PAGE_TYPE_ALBUM -> fragment.findNavController().navigate(openYouTubeAlbumFragment(endpoint))
            else -> fragment.findNavController().navigate(openYouTubeBrowseFragment(endpoint))
        }
        is SearchEndpoint -> {}
        is ShareEntityEndpoint -> {}
        is WatchPlaylistEndpoint -> {}
        null -> {}
    }
}