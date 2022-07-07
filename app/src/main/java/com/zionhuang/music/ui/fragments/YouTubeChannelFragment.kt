package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_DATA
import com.zionhuang.music.constants.MediaConstants.QUEUE_YT_CHANNEL
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.id
import com.zionhuang.music.extensions.requireAppCompatActivity
import com.zionhuang.music.models.QueueData
import com.zionhuang.music.ui.adapters.InfoItemAdapter
import com.zionhuang.music.ui.fragments.base.PagingRecyclerViewFragment
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import com.zionhuang.music.viewmodels.YouTubeChannelViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class YouTubeChannelFragment : PagingRecyclerViewFragment<InfoItemAdapter>() {
    private val args: YouTubeChannelFragmentArgs by navArgs()
    private val channelId by lazy { args.channelId }

    private val viewModel by viewModels<YouTubeChannelViewModel>()
    private val songsViewModel by activityViewModels<SongsViewModel>()
    private val playbackViewModel by activityViewModels<PlaybackViewModel>()

    override val adapter = InfoItemAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.streamMenuListener = songsViewModel.streamPopupMenuListener

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addOnClickListener { pos, _ ->
                val item = this@YouTubeChannelFragment.adapter.getItemByPosition(pos)
                if (item is StreamInfoItem) {
                    playbackViewModel.playMedia(
                        requireActivity(), item.id, bundleOf(
                            EXTRA_QUEUE_DATA to QueueData(QUEUE_YT_CHANNEL, channelId)
                        )
                    )
                }
            }
        }

        lifecycleScope.launch {
            val channel = viewModel.getChannelInfo(channelId)
            requireAppCompatActivity().supportActionBar?.title = channel.name
            viewModel.getChannel(channelId).collectLatest {
                adapter.submitData(it)
            }
        }
    }
}