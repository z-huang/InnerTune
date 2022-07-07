package com.zionhuang.music.ui.fragments.search

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_DATA
import com.zionhuang.music.constants.MediaConstants.EXTRA_SEARCH_FILTER
import com.zionhuang.music.constants.MediaConstants.QUEUE_YT_SEARCH
import com.zionhuang.music.databinding.FragmentSearchBinding
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.id
import com.zionhuang.music.extensions.requireAppCompatActivity
import com.zionhuang.music.models.QueueData
import com.zionhuang.music.ui.adapters.InfoItemAdapter
import com.zionhuang.music.ui.adapters.LoadStateAdapter
import com.zionhuang.music.ui.fragments.base.NavigationFragment
import com.zionhuang.music.utils.bindLoadStateLayout
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SearchViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import com.zionhuang.music.youtube.NewPipeYouTubeHelper.extractChannelId
import com.zionhuang.music.youtube.NewPipeYouTubeHelper.extractPlaylistId
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.*
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class YouTubeSearchFragment : NavigationFragment<FragmentSearchBinding>() {
    override fun getViewBinding() = FragmentSearchBinding.inflate(layoutInflater)
    override fun getToolbar(): Toolbar = binding.toolbar

    private val args: YouTubeSearchFragmentArgs by navArgs()
    private val query by lazy { args.searchQuery }

    private val viewModel by viewModels<SearchViewModel>()
    private val songsViewModel by activityViewModels<SongsViewModel>()
    private val playbackViewModel by activityViewModels<PlaybackViewModel>()

    private val searchResultAdapter = InfoItemAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enterTransition = MaterialFadeThrough().addTarget(binding.content).setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
        exitTransition = MaterialFadeThrough().addTarget(binding.content).setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        requireAppCompatActivity().supportActionBar?.title = query

        searchResultAdapter.apply {
            streamMenuListener = songsViewModel.streamPopupMenuListener
            bindLoadStateLayout(binding.layoutLoadState)
        }
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchResultAdapter.withLoadStateFooter(LoadStateAdapter { searchResultAdapter.retry() })
            addOnClickListener { pos, view ->
                when (val item: InfoItem = searchResultAdapter.getItemByPosition(pos)!!) {
                    is StreamInfoItem -> {
                        playbackViewModel.playMedia(
                            requireActivity(), item.id, bundleOf(
                                EXTRA_QUEUE_DATA to QueueData(QUEUE_YT_SEARCH, query, extras = bundleOf(
                                    EXTRA_SEARCH_FILTER to viewModel.searchFilter.value
                                ))
                            )
                        )
                    }
                    is PlaylistInfoItem -> {
                        val directions = YouTubeSearchFragmentDirections.actionSearchResultFragmentToYouTubePlaylistFragment(extractPlaylistId(item.url)!!)
                        findNavController().navigate(directions)
                    }
                    is ChannelInfoItem -> {
                        val directions = YouTubeSearchFragmentDirections.actionSearchResultFragmentToYouTubeChannelFragment(extractChannelId(item.url)!!)
                        findNavController().navigate(directions)
                    }
                }
            }
        }

        binding.chipAll.isVisible = false
        binding.chipArtists.isVisible = false
        binding.chipGroup.setOnCheckedStateChangeListener { _, _ ->
            val filter = when (binding.chipGroup.checkedChipId) {
                R.id.chip_all -> ALL
                R.id.chip_songs -> MUSIC_SONGS
                R.id.chip_videos -> MUSIC_VIDEOS
                R.id.chip_albums -> MUSIC_ALBUMS
                R.id.chip_artists -> MUSIC_ARTISTS
                R.id.chip_playlists -> PLAYLISTS
                R.id.chip_channels -> CHANNELS
                else -> throw IllegalArgumentException("Unexpected filter type.")
            }
            viewModel.searchFilter.postValue(filter)
        }

        viewModel.searchFilter.observe(viewLifecycleOwner) { filter ->
            when (filter) {
                ALL -> binding.chipAll
                MUSIC_SONGS -> binding.chipSongs
                MUSIC_VIDEOS -> binding.chipVideos
                MUSIC_ALBUMS -> binding.chipAlbums
                MUSIC_ARTISTS -> binding.chipArtists
                PLAYLISTS -> binding.chipPlaylists
                CHANNELS -> binding.chipChannels
                else -> null
            }?.isChecked = true

            searchResultAdapter.refresh()
        }

        lifecycleScope.launch {
            // Always showing the first item when switching filters
            searchResultAdapter.loadStateFlow
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .collectLatest {
                    binding.recyclerView.scrollToPosition(0)
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
        inflater.inflate(R.menu.search_icon, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_search) {
            NavHostFragment.findNavController(this).navigate(R.id.action_searchResultFragment_to_searchSuggestionFragment)
        }
        return true
    }
}