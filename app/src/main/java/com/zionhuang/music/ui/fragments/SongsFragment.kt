package com.zionhuang.music.ui.fragments

import android.app.DownloadManager.*
import android.os.Bundle
import android.view.*
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_DESC
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_ORDER
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_TYPE
import com.zionhuang.music.constants.MediaConstants.QUEUE_ALL_SONG
import com.zionhuang.music.constants.ORDER_ARTIST
import com.zionhuang.music.constants.ORDER_CREATE_DATE
import com.zionhuang.music.constants.ORDER_NAME
import com.zionhuang.music.constants.SongSortType
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.extensions.addFastScroller
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.ui.adapters.SongsAdapter
import com.zionhuang.music.ui.adapters.selection.SongItemDetailsLookup
import com.zionhuang.music.ui.adapters.selection.SongItemKeyProvider
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.ui.listeners.SortMenuListener
import com.zionhuang.music.utils.addActionModeObserver
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SongsFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    private val playbackViewModel by activityViewModels<PlaybackViewModel>()
    private val songsViewModel by activityViewModels<SongsViewModel>()
    private lateinit var songsAdapter: SongsAdapter
    private lateinit var tracker: SelectionTracker<String>
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply { duration = 300L }
        exitTransition = MaterialFadeThrough().apply { duration = 300L }
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()
        binding.recyclerView.doOnPreDraw { startPostponedEnterTransition() }

        songsAdapter = SongsAdapter(songsViewModel.songPopupMenuListener).apply {
            sortMenuListener = this@SongsFragment.sortMenuListener
            downloadInfo = songsViewModel.downloadInfoLiveData
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songsAdapter
            addOnClickListener { pos, _ ->
                if (pos == 0) return@addOnClickListener
                playbackViewModel.playMedia(
                    requireActivity(), songsAdapter.getItemByPosition(pos)!!.songId, bundleOf(
                        EXTRA_QUEUE_TYPE to QUEUE_ALL_SONG,
                        EXTRA_QUEUE_ORDER to sortMenuListener.sortType(),
                        EXTRA_QUEUE_DESC to sortMenuListener.sortDescending()
                    )
                )
            }
            addFastScroller { useMd2Style() }
        }

        tracker = SelectionTracker.Builder(
            "selectionId",
            binding.recyclerView,
            SongItemKeyProvider(songsAdapter),
            SongItemDetailsLookup(binding.recyclerView),
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()
        songsAdapter.tracker = tracker
        tracker.addActionModeObserver(requireActivity(), tracker, R.menu.song_contextual_action_bar) { item ->
            val selectedMap = songsAdapter.snapshot().items
                .filter { tracker.selection.contains(it.songId) }
                .associateBy { it.songId }
            val songs = tracker.selection.toList().mapNotNull { selectedMap[it] }
            when (item.itemId) {
                R.id.action_play_next -> songsViewModel.songPopupMenuListener.playNext(songs, requireContext())
                R.id.action_add_to_queue -> songsViewModel.songPopupMenuListener.addToQueue(songs, requireContext())
                R.id.action_add_to_playlist -> songsViewModel.songPopupMenuListener.addToPlaylist(songs, requireContext())
                R.id.action_download -> songsViewModel.songPopupMenuListener.downloadSongs(tracker.selection.toList(), requireContext())
                R.id.action_delete -> songsViewModel.songPopupMenuListener.deleteSongs(songs)
            }
            true
        }

        lifecycleScope.launch {
            songsViewModel.allSongsFlow.collectLatest {
                songsAdapter.submitData(it)
            }
        }

        songsViewModel.downloadInfoLiveData.observe(viewLifecycleOwner) { map ->
            map.forEach { (key, value) ->
                songsAdapter.setProgress(key, value)
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

    private val sortMenuListener = object : SortMenuListener {
        @IdRes
        override fun sortType(): Int = songsViewModel.sortType
        override fun sortDescending(): Boolean = songsViewModel.sortDescending
        override fun sortByCreateDate() = updateSortType(ORDER_CREATE_DATE)
        override fun sortByName() = updateSortType(ORDER_NAME)
        override fun sortByArtist() = updateSortType(ORDER_ARTIST)
        override fun toggleSortOrder() {
            songsViewModel.sortDescending = !songsViewModel.sortDescending
            songsAdapter.refresh()
        }
    }

    private fun updateSortType(@SongSortType sortType: Int) {
        songsViewModel.sortType = sortType
        songsAdapter.refresh()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        tracker.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker.onSaveInstanceState(outState)
    }

    companion object {
        private const val TAG = "SongsFragment"
    }
}