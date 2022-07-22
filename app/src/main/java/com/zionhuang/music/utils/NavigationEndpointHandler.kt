package com.zionhuang.music.utils

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.models.*
import com.zionhuang.music.R
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.fragments.youtube.YouTubeBrowseFragmentDirections.openYouTubeBrowseFragment

open class NavigationEndpointHandler(private val fragment: Fragment) {
    open fun handle(navigationEndpoint: NavigationEndpoint?, item: Item? = null) = when (val endpoint = navigationEndpoint?.endpoint) {
        is WatchEndpoint -> {
            MediaSessionConnection.binder?.playQueue(YouTubeQueue(endpoint, item))
            (fragment.requireActivity() as? MainActivity)?.showBottomSheet()
        }
        is WatchPlaylistEndpoint -> {
            MediaSessionConnection.binder?.playQueue(YouTubeQueue(endpoint.toWatchEndpoint(), item))
            (fragment.requireActivity() as? MainActivity)?.showBottomSheet()
        }
        is BrowseEndpoint -> {
            fragment.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
            fragment.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
            fragment.findNavController().navigate(openYouTubeBrowseFragment(endpoint))
        }
        is SearchEndpoint -> {}
        is QueueAddEndpoint -> MediaSessionConnection.binder?.handleQueueAddEndpoint(endpoint, item!!)
        is ShareEntityEndpoint -> {}
        null -> {}
    }

    fun share(item: Item) {
        val intent = Intent().apply {
            action = ACTION_SEND
            type = "text/plain"
            putExtra(EXTRA_TEXT, item.shareLink)
        }
        fragment.startActivity(Intent.createChooser(intent, null))
    }
}