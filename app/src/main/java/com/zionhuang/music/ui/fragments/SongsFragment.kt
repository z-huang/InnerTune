package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.db.SongEntity
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.playback.queue.Queue
import com.zionhuang.music.ui.adapters.SongsAdapter
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel

class SongsFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    private val playbackViewModel by activityViewModels<PlaybackViewModel>()
    private val songsViewModel by viewModels<SongsViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val songsAdapter = SongsAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songsAdapter
            addOnClickListener { pos, _ ->
                playbackViewModel.playMedia(SongParcel.fromSongEntity(songsAdapter.getSongFromPosition(pos)), Queue.QUEUE_ALL_SONG)

            }
        }
        songsViewModel.allSongs.observe(viewLifecycleOwner, { list: List<SongEntity?>? -> songsAdapter.setDataSet(list) })
    }
}