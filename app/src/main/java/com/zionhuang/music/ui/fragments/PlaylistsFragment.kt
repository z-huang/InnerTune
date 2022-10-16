package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.DOWNLOADED_PLAYLIST_ID
import com.zionhuang.music.constants.Constants.LIKED_PLAYLIST_ID
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.extensions.addFastScroller
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.adapters.LocalItemAdapter
import com.zionhuang.music.ui.adapters.selection.LocalItemDetailsLookup
import com.zionhuang.music.ui.adapters.selection.LocalItemKeyProvider
import com.zionhuang.music.ui.fragments.base.RecyclerViewFragment
import com.zionhuang.music.ui.fragments.dialogs.CreatePlaylistDialog
import com.zionhuang.music.ui.listeners.DownloadedPlaylistMenuListener
import com.zionhuang.music.ui.listeners.LikedPlaylistMenuListener
import com.zionhuang.music.ui.listeners.PlaylistMenuListener
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.utils.addActionModeObserver
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlaylistsFragment : RecyclerViewFragment<LocalItemAdapter>(), MenuProvider {
    private val songsViewModel by activityViewModels<SongsViewModel>()
    private val menuListener = PlaylistMenuListener(this)
    override val adapter = LocalItemAdapter().apply {
        playlistMenuListener = menuListener
        likedPlaylistMenuListener = LikedPlaylistMenuListener(this@PlaylistsFragment)
        downloadedPlaylistMenuListener = DownloadedPlaylistMenuListener(this@PlaylistsFragment)
    }
    private var tracker: SelectionTracker<String>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).fab.setOnClickListener {
            CreatePlaylistDialog().show(childFragmentManager, null)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addFastScroller { useMd2Style() }
        }
        binding.recyclerView.addOnClickListener { position, _ ->
            val item = adapter.currentList[position]
            if (item is Playlist) {
                if (item.playlist.isYouTubePlaylist) {
                    NavigationEndpointHandler(this).handle(BrowseEndpoint.playlistBrowseEndpoint("VL" + item.id))
                } else {
                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
                    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
                    findNavController().navigate(PlaylistsFragmentDirections.actionPlaylistsToPlaylistSongs(item.id))
                }
            } else if (item.id == LIKED_PLAYLIST_ID || item.id == DOWNLOADED_PLAYLIST_ID) {
                exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
                reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
                findNavController().navigate(PlaylistsFragmentDirections.actionPlaylistsToCustomPlaylist(item.id))
            }
        }

        tracker = SelectionTracker.Builder("selectionId", binding.recyclerView, LocalItemKeyProvider(adapter), LocalItemDetailsLookup(binding.recyclerView), StorageStrategy.createStringStorage())
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()
            .apply {
                adapter.tracker = this
                addActionModeObserver(requireActivity(), R.menu.playlist_batch) { item ->
                    val map = adapter.currentList.associateBy { it.id }
                    val playlists = selection.toList().map { map[it] }.filterIsInstance<Playlist>()
                    when (item.itemId) {
                        R.id.action_play_next -> menuListener.playNext(playlists)
                        R.id.action_add_to_queue -> menuListener.addToQueue(playlists)
                        R.id.action_add_to_playlist -> menuListener.addToPlaylist(playlists)
                        R.id.action_download -> menuListener.download(playlists)
                        R.id.action_delete -> menuListener.delete(playlists)
                    }
                    true
                }
            }

        lifecycleScope.launch {
            songsViewModel.allPlaylistsFlow.collectLatest {
                adapter.submitList(it)
            }
        }

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_and_settings, menu)
        menu.findItem(R.id.action_search).actionView = null
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> findNavController().navigate(R.id.localSearchFragment)
            R.id.action_settings -> findNavController().navigate(R.id.settingsActivity)
        }
        return true
    }
}