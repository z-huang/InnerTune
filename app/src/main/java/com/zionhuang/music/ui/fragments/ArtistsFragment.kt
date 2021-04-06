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
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.R
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.ui.adapters.ArtistsAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArtistsFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    private val songsViewModel by activityViewModels<SongsViewModel>()
    private val artistsAdapter = ArtistsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply { duration = 300L }
        exitTransition = MaterialFadeThrough().apply { duration = 300L }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = artistsAdapter
            addOnClickListener { position, view ->
                exitTransition = MaterialElevationScale(false).apply { duration = 300L }
                reenterTransition = MaterialElevationScale(true).apply { duration = 300L }
                val transitionName = getString(R.string.artist_songs_transition_name)
                val extras = FragmentNavigatorExtras(view to transitionName)
                val directions = ArtistsFragmentDirections.actionArtistsFragmentToArtistSongsFragment(artistsAdapter.getItemByPosition(position)!!.id!!)
                findNavController().navigate(directions, extras)
            }
        }

        lifecycleScope.launch {
            songsViewModel.allArtistsFlow.collectLatest {
                artistsAdapter.submitData(it)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> findNavController().navigate(SettingsFragmentDirections.openSettingsFragment())
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_and_settings, menu)
    }

    companion object {
        const val TAG = "ArtistsFragment"
    }
}