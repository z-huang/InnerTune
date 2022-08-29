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
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ARTIST
import com.zionhuang.innertube.models.NavigationEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.Artist
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.ui.adapters.LocalItemAdapter
import com.zionhuang.music.ui.fragments.ArtistsFragmentDirections.actionArtistsFragmentToArtistSongsFragment
import com.zionhuang.music.ui.fragments.base.RecyclerViewFragment
import com.zionhuang.music.ui.listeners.ArtistMenuListener
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArtistsFragment : RecyclerViewFragment<LocalItemAdapter>(), MenuProvider {
    private val songsViewModel by activityViewModels<SongsViewModel>()
    override val adapter = LocalItemAdapter().apply {
        artistMenuListener = ArtistMenuListener(this@ArtistsFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addOnClickListener { position, _ ->
                (this@ArtistsFragment.adapter.currentList[position] as? Artist)?.let { artist ->
                    if (artist.artist.isYouTubeArtist) {
                        NavigationEndpointHandler(this@ArtistsFragment).handle(NavigationEndpoint(
                            browseEndpoint = BrowseEndpoint(
                                browseId = artist.id,
                                browseEndpointContextSupportedConfigs = BrowseEndpointContextSupportedConfigs(
                                    browseEndpointContextMusicConfig = BrowseEndpointContextMusicConfig(
                                        pageType = MUSIC_PAGE_TYPE_ARTIST
                                    )
                                )
                            )
                        ))
                    } else {
                        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
                        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
                        findNavController().navigate(actionArtistsFragmentToArtistSongsFragment(artist.id))
                    }
                }
            }
        }

        lifecycleScope.launch {
            songsViewModel.allArtistsFlow.collectLatest {
                adapter.submitList(it)
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