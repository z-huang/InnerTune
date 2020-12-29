package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.R
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.ui.adapters.ChannelsAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChannelsFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    private val songsViewModel by activityViewModels<SongsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply { duration = 300L }
        exitTransition = MaterialFadeThrough().apply { duration = 300L }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()
        binding.recyclerView.doOnPreDraw { startPostponedEnterTransition() }

        val channelsAdapter = ChannelsAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = channelsAdapter
            addOnClickListener { position, view ->
                exitTransition = MaterialElevationScale(false).apply {
                    duration = 300L
                }
                reenterTransition = MaterialElevationScale(true).apply {
                    duration = 300L
                }
                val transitionName = getString(R.string.channel_songs_transition_name)
                val extras = FragmentNavigatorExtras(view to transitionName)
                val directions = ChannelsFragmentDirections.actionChannelsFragmentToChannelSongsFragment(channelsAdapter.getItemByPosition(position)!!.id)
                findNavController().navigate(directions, extras)
            }
        }
        lifecycleScope.launch {
            songsViewModel.allChannelsFlow.collectLatest {
                channelsAdapter.submitData(it)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                findNavController().navigate(SettingsFragmentDirections.openSettingsFragment())
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search_and_settings, menu)
    }
}