package com.zionhuang.music.utils

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zionhuang.innertube.models.*
import com.zionhuang.music.ui.fragments.youtube.YouTubeBrowseFragmentDirections.openYouTubeBrowseFragment

open class NavigationEndpointHandler(private val fragment: Fragment) {
    open fun handle(navigationEndpoint: NavigationEndpoint) = when (val endpoint = navigationEndpoint.endpoint) {
        is WatchEndpoint -> {}
        is BrowseEndpoint -> fragment.findNavController().navigate(openYouTubeBrowseFragment(endpoint))
        is SearchEndpoint -> {}
        is ShareEntityEndpoint -> {}
        is WatchPlaylistEndpoint -> {}
        null -> {}
    }
}