package com.zionhuang.music.ui.fragments.youtube

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.R
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.ui.adapters.LoadStateAdapter
import com.zionhuang.music.ui.adapters.SectionAdapter
import com.zionhuang.music.ui.fragments.SettingsFragmentDirections
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.utils.bindLoadStateLayout
import com.zionhuang.music.viewmodels.YouTubeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class YouTubeBrowseFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    private val args: YouTubeBrowseFragmentArgs by navArgs()

    override fun getViewBinding() = LayoutRecyclerviewBinding.inflate(layoutInflater)

    private val youTubeViewModel by activityViewModels<YouTubeViewModel>()
    private val sectionAdapter = SectionAdapter(NavigationEndpointHandler(this))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
        exitTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sectionAdapter.bindLoadStateLayout(binding.layoutLoadState)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sectionAdapter.withLoadStateFooter(LoadStateAdapter { sectionAdapter.retry() })
        }
        lifecycleScope.launch {
            youTubeViewModel.browse(args.endpoint).collectLatest {
                sectionAdapter.submitData(it)
            }
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