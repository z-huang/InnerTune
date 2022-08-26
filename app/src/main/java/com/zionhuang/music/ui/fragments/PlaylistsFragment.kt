package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.NavigationEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.adapters.LocalItemPagingAdapter
import com.zionhuang.music.ui.fragments.PlaylistsFragmentDirections.actionPlaylistsFragmentToPlaylistSongsFragment
import com.zionhuang.music.ui.fragments.base.PagingRecyclerViewFragment
import com.zionhuang.music.ui.fragments.dialogs.CreatePlaylistDialog
import com.zionhuang.music.ui.listeners.PlaylistMenuListener
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlaylistsFragment : PagingRecyclerViewFragment<LocalItemPagingAdapter>(), MenuProvider {
    override fun getViewBinding() = LayoutRecyclerviewBinding.inflate(layoutInflater)
    override fun getToolbar(): Toolbar = binding.toolbar

    private val songsViewModel by activityViewModels<SongsViewModel>()
    override val adapter = LocalItemPagingAdapter().apply {
        playlistMenuListener = PlaylistMenuListener(this@PlaylistsFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).fab.setOnClickListener {
            CreatePlaylistDialog().show(childFragmentManager, null)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addOnClickListener { position, _ ->
                (this@PlaylistsFragment.adapter.getItemAt(position) as? Playlist)?.let { playlist ->
                    if (playlist.playlist.isYouTubePlaylist) {
                        NavigationEndpointHandler(this@PlaylistsFragment).handle(NavigationEndpoint(
                            browseEndpoint = BrowseEndpoint(browseId = "VL" + playlist.id)
                        ))
                    } else {
                        val directions = actionPlaylistsFragmentToPlaylistSongsFragment(playlist.id)
                        findNavController().navigate(directions)
                    }
                }
            }
        }

        lifecycleScope.launch {
            songsViewModel.allPlaylistsFlow.collectLatest {
                adapter.submitData(it)
            }
        }

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> findNavController().navigate(R.id.settingsActivity)
        }
        return true
    }
}