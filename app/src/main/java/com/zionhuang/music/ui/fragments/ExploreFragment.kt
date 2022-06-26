package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.R
import com.zionhuang.music.databinding.FragmentExploreBinding
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.ExploreViewModel

class ExploreFragment : BindingFragment<FragmentExploreBinding>() {
    override fun getViewBinding() = FragmentExploreBinding.inflate(layoutInflater)

    private val exploreViewModel by viewModels<ExploreViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
        exitTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_and_settings, menu)
        menu.findItem(R.id.action_search).actionView = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> findNavController().navigate(R.id.action_explorationFragment_to_searchSuggestionFragment)
            R.id.action_settings -> findNavController().navigate(SettingsFragmentDirections.openSettingsFragment())
        }
        return true
    }
}