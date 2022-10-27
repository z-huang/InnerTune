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
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.requireAppCompatActivity
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.repos.SongRepository
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

class ArtistSongsFragment : RecyclerViewFragment<LocalItemAdapter>() {
    private val args: ArtistSongsFragmentArgs by navArgs()
    private lateinit var artist: ArtistEntity

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

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addOnClickListener { position, _ ->
            if (adapter.currentList[position] !is Song) return@addOnClickListener
            playbackViewModel.playQueue(requireActivity(),
                ListQueue(
                    title = artist.name,
                    items = adapter.currentList.drop(1).filterIsInstance<Song>().map { it.toMediaItem() },
                    startIndex = position - 1
                )
            )
        }
        adapter.onShuffle = {
            playbackViewModel.playQueue(requireActivity(),
                ListQueue(
                    title = artist.name,
                    items = adapter.currentList.drop(1).filterIsInstance<Song>().shuffled().map { it.toMediaItem() }
                )
            )
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
            artist = SongRepository(requireContext()).getArtistById(args.artistId)!!
            requireAppCompatActivity().title = artist.name
            songsViewModel.getArtistSongsAsFlow(args.artistId).collectLatest {
                adapter.submitList(it)
            }
        }
    }
}