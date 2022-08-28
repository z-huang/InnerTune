package com.zionhuang.music.ui.fragments.songs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.requireAppCompatActivity
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.adapters.LocalItemAdapter
import com.zionhuang.music.ui.fragments.base.RecyclerViewFragment
import com.zionhuang.music.ui.listeners.SongMenuListener
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArtistSongsFragment : RecyclerViewFragment<LocalItemAdapter>() {
    private val args: ArtistSongsFragmentArgs by navArgs()

    private val playbackViewModel by activityViewModels<PlaybackViewModel>()
    private val songsViewModel by activityViewModels<SongsViewModel>()
    override val adapter = LocalItemAdapter().apply {
        songMenuListener = SongMenuListener(this@ArtistSongsFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addOnClickListener { position, _ ->
                if (this@ArtistSongsFragment.adapter.currentList[position] !is Song) return@addOnClickListener
                playbackViewModel.playQueue(requireActivity(),
                    ListQueue(
                        items = this@ArtistSongsFragment.adapter.currentList.drop(1).filterIsInstance<Song>().map { it.toMediaItem() },
                        startIndex = position - 1
                    )
                )
            }
        }

        lifecycleScope.launch {
            requireAppCompatActivity().title = SongRepository.getArtistById(args.artistId)!!.name
            songsViewModel.getArtistSongsAsFlow(args.artistId).collectLatest {
                adapter.submitList(it)
            }
        }
    }
}