package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.NavigationEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.Album
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.ui.adapters.LocalItemAdapter
import com.zionhuang.music.ui.fragments.base.PagingRecyclerViewFragment
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlbumsFragment : PagingRecyclerViewFragment<LocalItemAdapter>(), MenuProvider {
    private val songsViewModel by activityViewModels<SongsViewModel>()
    override val adapter = LocalItemAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addOnClickListener { position, _ ->
                (this@AlbumsFragment.adapter.getItemAt(position) as? Album)?.let { album ->
                    NavigationEndpointHandler(this@AlbumsFragment).handle(NavigationEndpoint(
                        browseEndpoint = BrowseEndpoint(browseId = album.id)
                    ))
                }
            }
        }

        lifecycleScope.launch {
            songsViewModel.allAlbumsFlow.collectLatest {
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