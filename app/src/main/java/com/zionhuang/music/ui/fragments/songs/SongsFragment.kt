package com.zionhuang.music.ui.fragments.songs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.playback.queue.Queue
import com.zionhuang.music.ui.adapters.SongsAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SongsFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    private val playbackViewModel by activityViewModels<PlaybackViewModel>()
    private val songsViewModel by viewModels<SongsViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val songsPagingAdapter = SongsAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songsPagingAdapter
            addOnClickListener { pos, _ ->
                playbackViewModel.playMedia(SongParcel.fromSongEntity(songsPagingAdapter.getItemByPosition(pos)), Queue.QUEUE_ALL_SONG)
            }
        }
        lifecycleScope.launch {
            songsViewModel.allSongsFlow.collectLatest {
                songsPagingAdapter.submitData(it)
            }
        }
    }
}