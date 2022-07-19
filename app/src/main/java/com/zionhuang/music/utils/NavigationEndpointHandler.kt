package com.zionhuang.music.utils

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zionhuang.innertube.models.*
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.ui.fragments.youtube.YouTubeBrowseFragmentDirections.openYouTubeBrowseFragment

open class NavigationEndpointHandler(private val fragment: Fragment) {
    open fun handle(navigationEndpoint: NavigationEndpoint?, item: Item? = null) = when (val endpoint = navigationEndpoint?.endpoint) {
        is WatchEndpoint -> MediaSessionConnection.binder?.playQueue(YouTubeQueue(endpoint, item))
        is WatchPlaylistEndpoint -> MediaSessionConnection.binder?.playQueue(YouTubeQueue(endpoint.toWatchEndpoint(), item))
        is BrowseEndpoint -> fragment.findNavController().navigate(openYouTubeBrowseFragment(endpoint))
        is SearchEndpoint -> {}
        is QueueAddEndpoint -> MediaSessionConnection.binder?.handleQueueAddEndpoint(endpoint, item!!)
        is ShareEntityEndpoint -> {}
        null -> {}
    }
}