package com.zionhuang.music.ui.fragments

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.zionhuang.innertube.YouTube.HOME_BROWSE_ID
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.ui.adapters.YouTubeItemPagingAdapter
import com.zionhuang.music.ui.fragments.base.PagingRecyclerViewFragment
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.viewmodels.YouTubeBrowseViewModel
import com.zionhuang.music.viewmodels.YouTubeBrowseViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : PagingRecyclerViewFragment<YouTubeItemPagingAdapter>(), MenuProvider {
    override fun getViewBinding() = LayoutRecyclerviewBinding.inflate(layoutInflater)
    override fun getToolbar(): Toolbar = binding.toolbar

    private val viewModel by viewModels<YouTubeBrowseViewModel> {
        YouTubeBrowseViewModelFactory(
            requireActivity().application,
            BrowseEndpoint(browseId = HOME_BROWSE_ID)
        )
    }
    override val adapter = YouTubeItemPagingAdapter(NavigationEndpointHandler(this))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        lifecycleScope.launch {
            viewModel.pagingData.collectLatest {
                adapter.submitData(it)
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
            R.id.action_search -> findNavController().navigate(R.id.youtubeSuggestionFragment)
            R.id.action_settings -> findNavController().navigate(R.id.settingsActivity)
        }
        return true
    }
}