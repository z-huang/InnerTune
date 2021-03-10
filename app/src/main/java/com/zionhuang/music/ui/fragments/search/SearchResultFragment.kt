package com.zionhuang.music.ui.fragments.search

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.LoadState
import androidx.paging.LoadState.Loading
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_SONG_ID
import com.zionhuang.music.databinding.FragmentSearchResultBinding
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.ui.adapters.LoadStateAdapter
import com.zionhuang.music.ui.adapters.NewPipeSearchResultAdapter
import com.zionhuang.music.ui.fragments.base.MainFragment
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SearchViewModel
import com.zionhuang.music.youtube.extractors.YouTubeStreamExtractor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class SearchResultFragment : MainFragment<FragmentSearchResultBinding>() {
    companion object {
        private const val TAG = "SearchResultFragment"
    }

    private val viewModel by viewModels<SearchViewModel>()
    private val playbackViewModel by activityViewModels<PlaybackViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply { duration = 300L }
        exitTransition = MaterialFadeThrough().apply { duration = 300L }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = SearchResultFragmentArgs.fromBundle(requireArguments()).searchQuery
        activity.supportActionBar?.title = query

        val searchResultAdapter = NewPipeSearchResultAdapter().apply {
            addLoadStateListener { loadState ->
                binding.progressBar.isVisible = loadState.refresh is Loading
                binding.btnRetry.isVisible = loadState.refresh is LoadState.Error
                binding.tvErrorMsg.isVisible = loadState.refresh is LoadState.Error
                if (loadState.refresh is LoadState.Error) {
                    binding.tvErrorMsg.text = (loadState.refresh as LoadState.Error).error.localizedMessage
                }
            }
        }
        binding.btnRetry.setOnClickListener { searchResultAdapter.retry() }
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchResultAdapter.withLoadStateFooter(LoadStateAdapter { searchResultAdapter.retry() })
            addOnClickListener { pos, _ ->
                val item = searchResultAdapter.getItemByPosition(pos)!!
                if (item is StreamInfoItem) {
                    playbackViewModel.playFromSearch(requireActivity(), query, bundleOf(
                            EXTRA_SONG_ID to YouTubeStreamExtractor.extractId(item.url)
                    ))
                }
//                if ("youtube#video" != item.id.kind) {
//                    return@addOnClickListener
//                }
//                playbackViewModel.playMedia(QUEUE_SINGLE, SongParcel.fromSearchResult(item))
            }
        }

        lifecycleScope.launch {
            viewModel.search(query).collectLatest {
                searchResultAdapter.submitData(it)
            }
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
}