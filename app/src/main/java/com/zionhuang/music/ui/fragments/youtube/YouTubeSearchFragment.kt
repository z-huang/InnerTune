package com.zionhuang.music.ui.fragments.youtube

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ALBUM
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ARTIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_COMMUNITY_PLAYLIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_FEATURED_PLAYLIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_VIDEO
import com.zionhuang.innertube.models.YTItem
import com.zionhuang.music.R
import com.zionhuang.music.databinding.FragmentSearchBinding
import com.zionhuang.music.extensions.requireAppCompatActivity
import com.zionhuang.music.ui.adapters.YouTubeItemPagingAdapter
import com.zionhuang.music.ui.adapters.selection.YouTubeItemDetailsLookup
import com.zionhuang.music.ui.adapters.selection.YouTubeItemKeyProvider
import com.zionhuang.music.ui.fragments.base.AbsPagingRecyclerViewFragment
import com.zionhuang.music.ui.listeners.YTItemBatchMenuListener
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.utils.addActionModeObserver
import com.zionhuang.music.viewmodels.YouTubeSearchViewModel
import com.zionhuang.music.viewmodels.YouTubeSearchViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class YouTubeSearchFragment : AbsPagingRecyclerViewFragment<FragmentSearchBinding, YouTubeItemPagingAdapter>(), MenuProvider {
    override fun getViewBinding() = FragmentSearchBinding.inflate(layoutInflater)
    override fun getToolbar(): Toolbar = binding.toolbar
    override fun getRecyclerView(): RecyclerView = binding.recyclerView
    override fun getLayoutLoadState() = binding.layoutLoadState
    override fun getSwipeRefreshLayout() = binding.swipeRefresh

    private val args: YouTubeSearchFragmentArgs by navArgs()

    private val viewModel by viewModels<YouTubeSearchViewModel> { YouTubeSearchViewModelFactory(requireActivity().application, args.query) }

    private val navigationEndpointHandler = NavigationEndpointHandler(this)
    override val adapter = YouTubeItemPagingAdapter(navigationEndpointHandler)
    private var tracker: SelectionTracker<String>? = null
    private val menuListener = YTItemBatchMenuListener(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enterTransition = null
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).setDuration(resources.getInteger(R.integer.motion_duration_large).toLong()).addTarget(R.id.fragment_content).addTarget(R.id.fragment_content)

        requireAppCompatActivity().title = args.query

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        when (viewModel.filter.value) {
            null -> binding.chipAll
            FILTER_SONG -> binding.chipSongs
            FILTER_VIDEO -> binding.chipVideos
            FILTER_ALBUM -> binding.chipAlbums
            FILTER_ARTIST -> binding.chipArtists
            FILTER_COMMUNITY_PLAYLIST -> binding.chipCommunityPlaylists
            FILTER_FEATURED_PLAYLIST -> binding.chipFeaturedPlaylists
            else -> null
        }?.isChecked = true

        binding.chipGroup.setOnCheckedStateChangeListener { group, _ ->
            val newFilter = when (group.checkedChipId) {
                R.id.chip_all -> null
                R.id.chip_songs -> FILTER_SONG
                R.id.chip_videos -> FILTER_VIDEO
                R.id.chip_albums -> FILTER_ALBUM
                R.id.chip_artists -> FILTER_ARTIST
                R.id.chip_community_playlists -> FILTER_COMMUNITY_PLAYLIST
                R.id.chip_featured_playlists -> FILTER_FEATURED_PLAYLIST
                else -> null
            }
            if (viewModel.filter.value == newFilter) {
                binding.recyclerView.smoothScrollToPosition(0)
            } else {
                tracker?.clearSelection()
                viewModel.filter.value = newFilter
                adapter.refresh()
            }
        }

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
                    val items = selection.toList().map { map[it] }.filterIsInstance<YTItem>()
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

        lifecycleScope.launch {
            // Always show the first item when switching filters
            adapter.loadStateFlow
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .collectLatest {
                    binding.recyclerView.scrollToPosition(0)
                }
        }

        lifecycleScope.launch {
            viewModel.pagingData.collectLatest {
                adapter.submitData(it)
            }
        }

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_icon, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_search) {
            findNavController().navigate(YouTubeSearchFragmentDirections.actionSearchResultToSearchSuggestion(args.query))
        }
        return true
    }
}