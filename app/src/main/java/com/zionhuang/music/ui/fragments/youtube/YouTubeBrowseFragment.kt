package com.zionhuang.music.ui.fragments.youtube

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.music.R
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.requireAppCompatActivity
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.ui.adapters.YouTubeItemPagingAdapter
import com.zionhuang.music.ui.adapters.selection.YouTubeItemDetailsLookup
import com.zionhuang.music.ui.adapters.selection.YouTubeItemKeyProvider
import com.zionhuang.music.ui.fragments.base.PagingRecyclerViewFragment
import com.zionhuang.music.ui.listeners.YTItemBatchMenuListener
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.utils.addActionModeObserver
import com.zionhuang.music.viewmodels.YouTubeBrowseViewModel
import com.zionhuang.music.viewmodels.YouTubeBrowseViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class YouTubeBrowseFragment : PagingRecyclerViewFragment<YouTubeItemPagingAdapter>(), MenuProvider {
    private val args: YouTubeBrowseFragmentArgs by navArgs()
    private val viewModel by viewModels<YouTubeBrowseViewModel> { YouTubeBrowseViewModelFactory(requireActivity().application, args.endpoint) }

    override val adapter = YouTubeItemPagingAdapter(NavigationEndpointHandler(this))
    private var tracker: SelectionTracker<String>? = null
    private val menuListener = YTItemBatchMenuListener(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        tracker = SelectionTracker.Builder("selectionId", binding.recyclerView, YouTubeItemKeyProvider(adapter), YouTubeItemDetailsLookup(binding.recyclerView), StorageStrategy.createStringStorage())
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()
            .apply {
                adapter.tracker = this
                addObserver(object : SelectionTracker.SelectionObserver<String>() {
                    override fun onItemStateChanged(key: String, selected: Boolean) {
                        getSwipeRefreshLayout().isEnabled = !hasSelection()
                    }
                })
                addActionModeObserver(requireActivity(), R.menu.youtube_item_batch) { menuItem ->
                    val map = adapter.snapshot().items.associateBy { it.id }
                    val items = selection.toList().map { map[it] }.filterIsInstance<SongItem>()
                    when (menuItem.itemId) {
                        R.id.action_play_next -> menuListener.playNext(items)
                        R.id.action_add_to_queue -> menuListener.addToQueue(items)
                        R.id.action_add_to_library -> menuListener.addToLibrary(items)
                        R.id.action_add_to_playlist -> menuListener.addToPlaylist(items)
                        R.id.action_download -> menuListener.download(items)
                    }
                    true
                }
            }

        if (args.endpoint.isAlbumEndpoint) {
            adapter.onPlayAlbum = {
                viewModel.albumSongs?.let { songs ->
                    MediaSessionConnection.binder?.songPlayer?.playQueue(ListQueue(
                        title = viewModel.albumName,
                        items = songs.map { it.toMediaItem() }
                    ))
                }
            }
            adapter.onShuffleAlbum = {
                viewModel.albumSongs?.let { songs ->
                    MediaSessionConnection.binder?.songPlayer?.playQueue(ListQueue(
                        title = viewModel.albumName,
                        items = songs.shuffled().map { it.toMediaItem() }
                    ))
                }
            }
            binding.recyclerView.addOnClickListener { position, _ ->
                (adapter.getItemAt(position) as? SongItem)?.let { item ->
                    viewModel.albumSongs?.let { songs ->
                        MediaSessionConnection.binder?.songPlayer?.playQueue(ListQueue(
                            title = viewModel.albumName,
                            items = songs.map { it.toMediaItem() },
                            startIndex = songs.indexOfFirst { it.id == item.id }
                        ))
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewModel.pagingData.collectLatest {
                adapter.submitData(it)
            }
        }

        requireAppCompatActivity().title = ""
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_and_settings, menu)
        menu.findItem(R.id.action_search).actionView = null
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> findNavController().navigate(R.id.youtubeSuggestionFragment)
            R.id.action_settings -> findNavController().navigate(R.id.settingsActivity)
        }
        return true
    }
}