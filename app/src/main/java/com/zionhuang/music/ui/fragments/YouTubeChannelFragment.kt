package com.zionhuang.music.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialContainerTransform
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_DATA
import com.zionhuang.music.constants.MediaConstants.QUEUE_YT_CHANNEL
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.requireAppCompatActivity
import com.zionhuang.music.extensions.resolveColor
import com.zionhuang.music.models.QueueData
import com.zionhuang.music.ui.adapters.InfoItemAdapter
import com.zionhuang.music.ui.adapters.LoadStateAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import com.zionhuang.music.viewmodels.YouTubeChannelViewModel
import com.zionhuang.music.youtube.NewPipeYouTubeHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class YouTubeChannelFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    override fun getViewBinding() = LayoutRecyclerviewBinding.inflate(layoutInflater)

    private val args: YouTubeChannelFragmentArgs by navArgs()
    private val channelId by lazy { args.channelId }

    private val viewModel by viewModels<YouTubeChannelViewModel>()
    private val songsViewModel by activityViewModels<SongsViewModel>()
    private val playbackViewModel by activityViewModels<PlaybackViewModel>()

    private val infoItemAdapter = InfoItemAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = 300L
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().resolveColor(R.attr.colorSurface))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        infoItemAdapter.apply {
            streamMenuListener = songsViewModel.streamPopupMenuListener
            addLoadStateListener { loadState ->
                binding.progressBar.isVisible = loadState.refresh is LoadState.Loading
                binding.btnRetry.isVisible = loadState.refresh is LoadState.Error
                binding.errorMsg.isVisible = loadState.refresh is LoadState.Error
                if (loadState.refresh is LoadState.Error) {
                    binding.errorMsg.text =
                        (loadState.refresh as LoadState.Error).error.localizedMessage
                }
            }
        }

        binding.recyclerView.apply {
            transitionName = getString(R.string.youtube_channel_transition_name)
            layoutManager = LinearLayoutManager(requireContext())
            adapter =
                infoItemAdapter.withLoadStateFooter(LoadStateAdapter { infoItemAdapter.retry() })
            addOnClickListener { pos, _ ->
                val item = infoItemAdapter.getItemByPosition(pos)
                if (item is StreamInfoItem) {
                    playbackViewModel.playMedia(
                        requireActivity(), NewPipeYouTubeHelper.extractVideoId(item.url)!!, bundleOf(
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
                infoItemAdapter.submitData(it)
            }
        }
    }
}