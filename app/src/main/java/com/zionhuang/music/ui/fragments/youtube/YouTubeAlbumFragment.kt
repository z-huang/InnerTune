package com.zionhuang.music.ui.fragments.youtube

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.innertube.models.Section
import com.zionhuang.music.R
import com.zionhuang.music.databinding.FragmentYoutubeAlbumBinding
import com.zionhuang.music.ui.adapters.YouTubeItemAdapter
import com.zionhuang.music.ui.fragments.SettingsFragmentDirections
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.viewmodels.YouTubeViewModel
import kotlinx.coroutines.launch

class YouTubeAlbumFragment : BindingFragment<FragmentYoutubeAlbumBinding>() {
    private val args: YouTubeBrowseFragmentArgs by navArgs()

    override fun getViewBinding() = FragmentYoutubeAlbumBinding.inflate(layoutInflater)

    private val youTubeViewModel by activityViewModels<YouTubeViewModel>()
    private val navigationEndpointHandler = NavigationEndpointHandler(this)
    private val itemAdapter = YouTubeItemAdapter(Section.ViewType.LIST, false, navigationEndpointHandler)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
        exitTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }
        lifecycleScope.launch {
            val info = youTubeViewModel.getAlbumInfo(args.endpoint)
            binding.info = info
            binding.buttonBar.isVisible = true
            binding.layoutLoadState.progressBar.isVisible = false
            binding.btnPlay.setOnClickListener {
                info.menu.playEndpoint?.let { endpoint -> navigationEndpointHandler.handle(endpoint) }
            }
            binding.btnShuffle.setOnClickListener {
                info.menu.shuffleEndpoint?.let { endpoint -> navigationEndpointHandler.handle(endpoint) }
            }
            itemAdapter.submitList(info.items)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_and_settings, menu)
        menu.findItem(R.id.action_search).actionView = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> findNavController().navigate(R.id.action_exploreFragment_to_searchSuggestionFragment)
            R.id.action_settings -> findNavController().navigate(SettingsFragmentDirections.openSettingsFragment())
        }
        return true
    }
}