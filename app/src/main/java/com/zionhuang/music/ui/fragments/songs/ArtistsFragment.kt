package com.zionhuang.music.ui.fragments.songs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.Hold
import com.zionhuang.music.R
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.ui.adapters.ArtistsAdapter
import com.zionhuang.music.ui.fragments.LibraryFragmentDirections
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArtistsFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    private val songsViewModel by activityViewModels<SongsViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val artistsAdapter = ArtistsAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = artistsAdapter
            addOnClickListener { position, view ->
                requireParentFragment().exitTransition = Hold()
                requireParentFragment().reenterTransition = Hold()
                val transitionName = getString(R.string.artist_songs_transition_name)
                val extras = FragmentNavigatorExtras(view to transitionName)
                val directions = LibraryFragmentDirections.actionLibraryFragmentToArtistSongsFragment(artistsAdapter.getItemByPosition(position)!!.id!!)
                findNavController().navigate(directions, extras)
            }
        }
        lifecycleScope.launch {
            songsViewModel.allArtistsFlow.collectLatest {
                artistsAdapter.submitData(it)
            }
        }
    }

    companion object {
        const val TAG = "ArtistsFragment"
    }
}