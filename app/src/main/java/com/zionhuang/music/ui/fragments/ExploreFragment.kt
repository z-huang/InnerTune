package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.innertube.YouTube.EXPLORE_BROWSE_ID
import com.zionhuang.music.R
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.ui.adapters.SectionAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.ExploreViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExploreFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    override fun getViewBinding() = LayoutRecyclerviewBinding.inflate(layoutInflater)

    private val exploreViewModel by viewModels<ExploreViewModel>()
    private val sectionAdapter = SectionAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
        exitTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sectionAdapter
        }
        lifecycleScope.launch {
            exploreViewModel.browse(EXPLORE_BROWSE_ID).collectLatest {
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