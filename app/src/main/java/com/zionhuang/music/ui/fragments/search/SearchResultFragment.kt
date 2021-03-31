package com.zionhuang.music.ui.fragments.search

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.paging.LoadState.Loading
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_LINK_HANDLER
import com.zionhuang.music.constants.MediaConstants.EXTRA_SONG_ID
import com.zionhuang.music.databinding.FragmentSearchResultBinding
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.ui.adapters.LoadStateAdapter
import com.zionhuang.music.ui.adapters.NewPipeSearchResultAdapter
import com.zionhuang.music.ui.fragments.base.MainFragment
import com.zionhuang.music.ui.listeners.SearchFilterListener
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SearchViewModel
import com.zionhuang.music.youtube.extractors.YouTubeStreamExtractor
import com.zionhuang.music.youtube.newpipe.ExtractorHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class SearchResultFragment : MainFragment<FragmentSearchResultBinding>() {
    private val args: SearchResultFragmentArgs by navArgs()
    private val query by lazy { args.searchQuery }

    private val viewModel by viewModels<SearchViewModel>()
    private val playbackViewModel by activityViewModels<PlaybackViewModel>()

    private lateinit var searchResultAdapter: NewPipeSearchResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply { duration = 300L }
        exitTransition = MaterialFadeThrough().apply { duration = 300L }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity.supportActionBar?.title = query

        searchResultAdapter = NewPipeSearchResultAdapter(searchFilterListener).apply {
            addLoadStateListener { loadState ->
                binding.progressBar.isVisible = loadState.refresh is Loading
                binding.btnRetry.isVisible = loadState.refresh is LoadState.Error
                binding.errorMsg.isVisible = loadState.refresh is LoadState.Error
                if (loadState.refresh is LoadState.Error) {
                    binding.errorMsg.text = (loadState.refresh as LoadState.Error).error.localizedMessage
                }
            }
        }
        binding.btnRetry.setOnClickListener { searchResultAdapter.retry() }
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchResultAdapter.withLoadStateFooter(LoadStateAdapter { searchResultAdapter.retry() })
            addOnClickListener { pos, view ->
                if (pos == 0) return@addOnClickListener
                when (val item = searchResultAdapter.getItemByPosition(pos)!!) {
                    is StreamInfoItem -> {
                        playbackViewModel.playFromSearch(requireActivity(), query, bundleOf(
                                EXTRA_SONG_ID to YouTubeStreamExtractor.extractId(item.url),
                                EXTRA_LINK_HANDLER to ExtractorHelper.getSearchQueryHandler(query, listOf(searchFilterListener.filter))
                        ))
                    }
                    is PlaylistInfoItem -> {
                        exitTransition = Hold()
                        reenterTransition = MaterialElevationScale(true).apply { duration = 300 }
                        val transitionName = getString(R.string.youtube_playlist_transition_name)
                        val extras = FragmentNavigatorExtras(view to transitionName)
                        val directions = SearchResultFragmentDirections.actionSearchResultFragmentToYouTubePlaylistFragment(item.url)
                        findNavController().navigate(directions, extras)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.search(query).collectLatest {
                searchResultAdapter.submitData(it)
            }
        }
    }

    private val searchFilterListener = object : SearchFilterListener {
        override var filter: String
            get() = viewModel.filter
            set(value) {
                viewModel.filter = value
                searchResultAdapter.refresh()
            }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_search_icon, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_search) {
            NavHostFragment.findNavController(this).navigate(R.id.action_searchResultFragment_to_searchSuggestionFragment)
        }
        return true
    }

    companion object {
        private const val TAG = "SearchResultFragment"
    }
}