package com.zionhuang.music.ui.fragments

import android.app.SearchManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.content.getSystemService
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.LocalItem
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.*
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.ui.adapters.LocalItemAdapter
import com.zionhuang.music.ui.adapters.selection.LocalItemDetailsLookup
import com.zionhuang.music.ui.adapters.selection.LocalItemKeyProvider
import com.zionhuang.music.ui.fragments.base.RecyclerViewFragment
import com.zionhuang.music.ui.listeners.SongMenuListener
import com.zionhuang.music.utils.addActionModeObserver
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SongsFragment : RecyclerViewFragment<LocalItemAdapter>(), MenuProvider {
    private val playbackViewModel by activityViewModels<PlaybackViewModel>()
    private val songsViewModel by activityViewModels<SongsViewModel>()
    private val menuListener = SongMenuListener(this)
    override val adapter = LocalItemAdapter().apply {
        songMenuListener = menuListener
    }
    private var tracker: SelectionTracker<String>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            addOnClickListener { position, _ ->
                if (this@SongsFragment.adapter.currentList[position] !is LocalItem) {
                    return@addOnClickListener
                }
                playbackViewModel.playQueue(requireActivity(), ListQueue(
                    items = this@SongsFragment.adapter.currentList.filterIsInstance<Song>().map { it.toMediaItem() },
                    startIndex = position - 1
                ))
            }
            addFastScroller { useMd2Style() }
        }

        tracker = SelectionTracker.Builder("selectionId", binding.recyclerView, LocalItemKeyProvider(adapter), LocalItemDetailsLookup(binding.recyclerView), StorageStrategy.createStringStorage())
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()
            .apply {
                adapter.tracker = this
                addActionModeObserver(requireActivity(), R.menu.song_batch) { item ->
                    val map = adapter.currentList.associateBy { it.id }
                    val songs = selection.toList().map { map[it] }.filterIsInstance<Song>()
                    when (item.itemId) {
                        R.id.action_play_next -> menuListener.playNext(songs)
                        R.id.action_add_to_queue -> menuListener.addToQueue(songs)
                        R.id.action_add_to_playlist -> menuListener.addToPlaylist(songs)
                        R.id.action_download -> menuListener.download(songs)
                        R.id.action_remove_download -> menuListener.removeDownload(songs)
                        R.id.action_refetch -> menuListener.refetch(songs)
                        R.id.action_delete -> menuListener.delete(songs)
                    }
                    true
                }
            }

        lifecycleScope.launch {
            songsViewModel.allSongsFlow.collectLatest {
                adapter.submitList(it)
            }
        }

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    @OptIn(FlowPreview::class)
    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_and_settings, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.apply {
            findViewById<EditText>(androidx.appcompat.R.id.search_src_text)?.apply {
                setPadding(0, 2, 0, 2)
                setTextColor(requireContext().resolveColor(R.attr.colorOnSurface))
                setHintTextColor(requireContext().resolveColor(R.attr.colorOnSurfaceVariant))
            }
            setSearchableInfo(requireContext().getSystemService<SearchManager>()?.getSearchableInfo(requireActivity().componentName))
            viewLifecycleOwner.lifecycleScope.launch {
                getQueryTextChangeFlow()
                    .debounce(100.toDuration(DurationUnit.MILLISECONDS))
                    .collect { e ->
                        songsViewModel.query = e.query
//                        adapter.refresh()
                    }
            }
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> findNavController().navigate(R.id.settingsActivity)
        }
        return true
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        tracker?.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker?.onSaveInstanceState(outState)
    }
}