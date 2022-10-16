package com.zionhuang.music.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_PREVIOUS
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.models.BrowseEndpoint.Companion.albumBrowseEndpoint
import com.zionhuang.innertube.models.BrowseEndpoint.Companion.artistBrowseEndpoint
import com.zionhuang.innertube.models.BrowseEndpoint.Companion.playlistBrowseEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.databinding.FragmentSearchLocalBinding
import com.zionhuang.music.db.entities.Album
import com.zionhuang.music.db.entities.Artist
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.getTextChangeFlow
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.ui.adapters.LocalItemAdapter
import com.zionhuang.music.ui.fragments.base.AbsRecyclerViewFragment
import com.zionhuang.music.ui.fragments.songs.ArtistSongsFragmentArgs
import com.zionhuang.music.ui.fragments.songs.PlaylistSongsFragmentArgs
import com.zionhuang.music.utils.KeyboardUtil.hideKeyboard
import com.zionhuang.music.utils.KeyboardUtil.showKeyboard
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.viewmodels.LocalSearchViewModel
import com.zionhuang.music.viewmodels.LocalSearchViewModel.Filter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class LocalSearchFragment : AbsRecyclerViewFragment<FragmentSearchLocalBinding, LocalItemAdapter>() {
    override fun getViewBinding() = FragmentSearchLocalBinding.inflate(layoutInflater)
    override fun getToolbar() = binding.toolbar
    override fun getRecyclerView() = binding.recyclerView

    private val viewModel by viewModels<LocalSearchViewModel>()
    override val adapter = LocalItemAdapter()

    private val voiceResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val spokenText = it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (spokenText != null) {
                binding.searchView.setText(spokenText)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LocalSearchFragment.adapter
        }
        binding.recyclerView.addOnClickListener { position, _ ->
            when (val item = adapter.currentList[position]) {
                is Song -> {
                    val songs = adapter.currentList.filterIsInstance<Song>()
                    MediaSessionConnection.binder?.songPlayer?.playQueue(ListQueue(
                        title = getString(R.string.queue_searched_songs),
                        items = songs.map { it.toMediaItem() },
                        startIndex = songs.indexOfFirst { it.id == item.id }
                    ))
                }
                is Artist -> if (item.artist.isYouTubeArtist) {
                    NavigationEndpointHandler(this).handle(artistBrowseEndpoint(item.id))
                } else {
                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
                    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
                    findNavController().navigate(R.id.artistSongsFragment, ArtistSongsFragmentArgs.Builder(item.id).build().toBundle())
                }
                is Album -> NavigationEndpointHandler(this).handle(albumBrowseEndpoint(item.id))
                is Playlist -> if (item.playlist.isYouTubePlaylist) {
                    NavigationEndpointHandler(this).handle(playlistBrowseEndpoint("VL" + item.id))
                } else {
                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
                    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
                    findNavController().navigate(R.id.playlistSongsFragment, PlaylistSongsFragmentArgs.Builder(item.id).build().toBundle())
                }
                else -> {}
            }
        }

        binding.btnVoice.setOnClickListener {
            voiceResultLauncher.launch(Intent(ACTION_RECOGNIZE_SPEECH))
        }
        setupSearchView()
        showKeyboard()
        when (viewModel.filter.value) {
            Filter.ALL -> binding.chipAll
            Filter.SONG -> binding.chipSongs
            Filter.ALBUM -> binding.chipAlbums
            Filter.ARTIST -> binding.chipArtists
            Filter.PLAYLIST -> binding.chipPlaylists
        }.isChecked = true

        binding.chipGroup.setOnCheckedStateChangeListener { group, _ ->
            viewModel.filter.value = when (group.checkedChipId) {
                R.id.chip_all -> Filter.ALL
                R.id.chip_songs -> Filter.SONG
                R.id.chip_albums -> Filter.ALBUM
                R.id.chip_artists -> Filter.ARTIST
                R.id.chip_playlists -> Filter.PLAYLIST
                else -> Filter.ALL
            }
        }

        lifecycleScope.launch {
            viewModel.result.collectLatest { list ->
                adapter.submitList(list)
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchView() {
        lifecycleScope.launch {
            binding.searchView
                .getTextChangeFlow()
                .debounce(100L)
                .collectLatest {
                    viewModel.query.postValue(it)
                    binding.btnClear.isVisible = it.isNotEmpty()
                }
        }
        binding.searchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_PREVIOUS) {
                hideKeyboard()
                true
            } else {
                false
            }
        }
        binding.btnClear.setOnClickListener {
            binding.searchView.text.clear()
        }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    private fun showKeyboard() = showKeyboard(requireActivity(), binding.searchView)
    private fun hideKeyboard() = hideKeyboard(requireActivity(), binding.searchView)
}