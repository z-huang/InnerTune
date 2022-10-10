package com.zionhuang.music.ui.fragments.songs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.DOWNLOADED_PLAYLIST_ID
import com.zionhuang.music.constants.Constants.LIKED_PLAYLIST_ID
import com.zionhuang.music.db.entities.LocalItem
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.requireAppCompatActivity
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.ui.adapters.LocalItemAdapter
import com.zionhuang.music.ui.adapters.selection.LocalItemDetailsLookup
import com.zionhuang.music.ui.adapters.selection.LocalItemKeyProvider
import com.zionhuang.music.ui.fragments.base.RecyclerViewFragment
import com.zionhuang.music.ui.listeners.SongMenuListener
import com.zionhuang.music.utils.addActionModeObserver
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CustomPlaylistFragment : RecyclerViewFragment<LocalItemAdapter>() {
    private val args: PlaylistSongsFragmentArgs by navArgs()
    private val playlistId by lazy { args.playlistId }

    private val playbackViewModel by activityViewModels<PlaybackViewModel>()
    private val songsViewModel by activityViewModels<SongsViewModel>()
    private val menuListener = SongMenuListener(this)
    override val adapter = LocalItemAdapter().apply {
        songMenuListener = menuListener
    }
    private var tracker: SelectionTracker<String>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
        binding.recyclerView.addOnClickListener { position, _ ->
            if (adapter.currentList[position] !is LocalItem) return@addOnClickListener

            playbackViewModel.playQueue(requireActivity(), ListQueue(
                title = when (playlistId) {
                    LIKED_PLAYLIST_ID -> getString(R.string.liked_songs)
                    DOWNLOADED_PLAYLIST_ID -> getString(R.string.downloaded_songs)
                    else -> throw IllegalArgumentException("Unknown custom playlist")
                },
                items = adapter.currentList.filterIsInstance<Song>().map { it.toMediaItem() },
                startIndex = position - 1
            ))
        }
        adapter.onShuffle = {
            playbackViewModel.playQueue(requireActivity(), ListQueue(
                title = when (playlistId) {
                    LIKED_PLAYLIST_ID -> getString(R.string.liked_songs)
                    DOWNLOADED_PLAYLIST_ID -> getString(R.string.downloaded_songs)
                    else -> throw IllegalArgumentException("Unknown custom playlist")
                },
                items = adapter.currentList.filterIsInstance<Song>().shuffled().map { it.toMediaItem() }
            ))
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
            requireAppCompatActivity().title = when (playlistId) {
                LIKED_PLAYLIST_ID -> getString(R.string.liked_songs)
                DOWNLOADED_PLAYLIST_ID -> getString(R.string.downloaded_songs)
                else -> throw IllegalArgumentException("Unknown custom playlist")
            }
            when (playlistId) {
                LIKED_PLAYLIST_ID -> songsViewModel.getLikedSongsAsFlow()
                DOWNLOADED_PLAYLIST_ID -> songsViewModel.getDownloadedSongsAsFlow()
                else -> throw IllegalArgumentException("Unknown custom playlist")
            }.collectLatest {
                adapter.submitList(it)
            }
        }
    }
}