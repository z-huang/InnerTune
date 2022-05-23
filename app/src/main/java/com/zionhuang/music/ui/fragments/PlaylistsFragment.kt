package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.adapters.PlaylistsAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.ui.fragments.dialogs.CreatePlaylistDialog
import com.zionhuang.music.viewmodels.PlaylistsViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlaylistsFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    override fun getViewBinding() = LayoutRecyclerviewBinding.inflate(layoutInflater)

    private val songsViewModel by activityViewModels<SongsViewModel>()
    private val playlistsViewModel by viewModels<PlaylistsViewModel>()
    private val playlistsAdapter = PlaylistsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply { duration = 300L }
        exitTransition = MaterialFadeThrough().apply { duration = 300L }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        playlistsAdapter.popupMenuListener = playlistsViewModel.popupMenuListener

        (requireActivity() as MainActivity).fab.setOnClickListener {
            CreatePlaylistDialog().show(childFragmentManager, null)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = playlistsAdapter
            addOnClickListener { position, view ->
                exitTransition = MaterialElevationScale(false).apply { duration = 300L }
                reenterTransition = MaterialElevationScale(true).apply { duration = 300L }
                val transitionName = getString(R.string.playlist_songs_transition_name)
                val extras = FragmentNavigatorExtras(view to transitionName)
                val directions = PlaylistsFragmentDirections.actionPlaylistsFragmentToPlaylistSongsFragment(playlistsAdapter.getItemByPosition(position)!!.playlistId)
                findNavController().navigate(directions, extras)
            }
        }

        lifecycleScope.launch {
            songsViewModel.allPlaylistsFlow.collectLatest {
                playlistsAdapter.submitData(it)
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
        inflater.inflate(R.menu.settings, menu)
    }

    companion object {
        const val TAG = "PlaylistsFragment"
    }
}