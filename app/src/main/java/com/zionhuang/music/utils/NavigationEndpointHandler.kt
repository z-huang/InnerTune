package com.zionhuang.music.utils

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zionhuang.innertube.models.endpoint.*
import com.zionhuang.music.ui.fragments.youtube.YouTubeBrowseFragmentDirections

class NavigationEndpointHandler(private val fragment: Fragment) {
    fun handle(navigationEndpoint: NavigationEndpoint) {
        when (val endpoint = navigationEndpoint.endpoint) {
            is WatchEndpoint -> {}
            is BrowseEndpoint -> {
                fragment.findNavController().navigate(YouTubeBrowseFragmentDirections.openYouTubeBrowseFragment(endpoint))
            }
            is SearchEndpoint -> {}
            is ShareEntityEndpoint -> {}
            is WatchPlaylistEndpoint -> {}
            null -> {}
        }
    }
}