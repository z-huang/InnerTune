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
import com.zionhuang.innertube.models.BrowseEndpoint.Companion.albumBrowseEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.Album
import com.zionhuang.music.extensions.addFastScroller
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.ui.adapters.LocalItemAdapter
import com.zionhuang.music.ui.adapters.selection.LocalItemDetailsLookup
import com.zionhuang.music.ui.adapters.selection.LocalItemKeyProvider
import com.zionhuang.music.ui.fragments.base.RecyclerViewFragment
import com.zionhuang.music.ui.listeners.AlbumMenuListener
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.utils.addActionModeObserver
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlbumsFragment : RecyclerViewFragment<LocalItemAdapter>(), MenuProvider {
    private val songsViewModel by activityViewModels<SongsViewModel>()
    private val menuListener = AlbumMenuListener(this)
    override val adapter = LocalItemAdapter().apply {
        albumMenuListener = menuListener
    }
    private var tracker: SelectionTracker<String>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addFastScroller { useMd2Style() }
            addOnClickListener { position, _ ->
                (this@AlbumsFragment.adapter.currentList[position] as? Album)?.let { album ->
                    NavigationEndpointHandler(this@AlbumsFragment).handle(albumBrowseEndpoint(album.id))
                }
            }
        }

        tracker = SelectionTracker.Builder("selectionId", binding.recyclerView, LocalItemKeyProvider(adapter), LocalItemDetailsLookup(binding.recyclerView), StorageStrategy.createStringStorage())
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()
            .apply {
                adapter.tracker = this
                addActionModeObserver(requireActivity(), R.menu.album_batch) { item ->
                    val map = adapter.currentList.associateBy { it.id }
                    val albums = selection.toList().map { map[it] }.filterIsInstance<Album>()
                    when (item.itemId) {
                        R.id.action_play_next -> menuListener.playNext(albums)
                        R.id.action_add_to_queue -> menuListener.addToQueue(albums)
                        R.id.action_add_to_playlist -> menuListener.addToPlaylist(albums)
                        R.id.action_refetch -> menuListener.refetch(albums)
                        R.id.action_delete -> menuListener.delete(albums)
                    }
                    true
                }
            }

        lifecycleScope.launch {
            songsViewModel.allAlbumsFlow.collectLatest {
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