package com.zionhuang.music.ui.fragments.youtube

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ALBUM
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ARTIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_COMMUNITY_PLAYLIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_FEATURED_PLAYLIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_VIDEO
import com.zionhuang.music.R
import com.zionhuang.music.databinding.FragmentSearchBinding
import com.zionhuang.music.extensions.requireAppCompatActivity
import com.zionhuang.music.ui.adapters.LoadStateAdapter
import com.zionhuang.music.ui.adapters.YouTubeItemPagingAdapter
import com.zionhuang.music.ui.fragments.base.NavigationFragment
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.utils.bindLoadStateLayout
import com.zionhuang.music.viewmodels.SearchViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class YouTubeSearchFragment : NavigationFragment<FragmentSearchBinding>() {
    override fun getViewBinding() = FragmentSearchBinding.inflate(layoutInflater)
    override fun getToolbar(): Toolbar = binding.toolbar

    private val args: YouTubeSearchFragmentArgs by navArgs()
    private val query by lazy { args.searchQuery }

    private val viewModel by viewModels<SearchViewModel>()

    private val navigationEndpointHandler = NavigationEndpointHandler(this)
    private val adapter = YouTubeItemPagingAdapter(navigationEndpointHandler)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enterTransition = null
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).setDuration(resources.getInteger(R.integer.motion_duration_large).toLong()).addTarget(R.id.fragment_content).addTarget(R.id.fragment_content)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        adapter.bindLoadStateLayout(binding.layoutLoadState)

        requireAppCompatActivity().supportActionBar?.title = query

        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@YouTubeSearchFragment.adapter.withLoadStateFooter(LoadStateAdapter { this@YouTubeSearchFragment.adapter.retry() })
        }

        binding.chipGroup.setOnCheckedStateChangeListener { group, _ ->
            viewModel.filter.postValue(when (group.checkedChipId) {
                R.id.chip_all -> null
                R.id.chip_songs -> FILTER_SONG
                R.id.chip_videos -> FILTER_VIDEO
                R.id.chip_albums -> FILTER_ALBUM
                R.id.chip_artists -> FILTER_ARTIST
                R.id.chip_community_playlists -> FILTER_COMMUNITY_PLAYLIST
                R.id.chip_featured_playlists -> FILTER_FEATURED_PLAYLIST
                else -> null
            })
        }

        viewModel.filter.observe(viewLifecycleOwner) { filter ->
            when (filter) {
                null -> binding.chipAll
                FILTER_SONG -> binding.chipSongs
                FILTER_VIDEO -> binding.chipVideos
                FILTER_ALBUM -> binding.chipAlbums
                FILTER_ARTIST -> binding.chipArtists
                FILTER_COMMUNITY_PLAYLIST -> binding.chipCommunityPlaylists
                FILTER_FEATURED_PLAYLIST -> binding.chipFeaturedPlaylists
                else -> null
            }?.apply {
                isChecked = true
            }
            adapter.refresh()
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
            viewModel.search(query).collectLatest {
                adapter.submitData(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_icon, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_search) {
            NavHostFragment.findNavController(this).navigate(R.id.action_searchResultFragment_to_searchSuggestionFragment)
        }
        return true
    }
}