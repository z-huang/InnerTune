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
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.themeColor
import com.zionhuang.music.ui.adapters.InfoItemAdapter
import com.zionhuang.music.ui.adapters.LoadStateAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.YouTubePlaylistViewModel
import com.zionhuang.music.youtube.extractors.YouTubeStreamExtractor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class YouTubePlaylistFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    private val args: YouTubePlaylistFragmentArgs by navArgs()
    private val url by lazy { args.url }

    private val viewModel by viewModels<YouTubePlaylistViewModel>()
    private val playbackViewModel by activityViewModels<PlaybackViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = 300L
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val infoItemAdapter = InfoItemAdapter().apply {
            addLoadStateListener { loadState ->
                binding.progressBar.isVisible = loadState.refresh is LoadState.Loading
                binding.btnRetry.isVisible = loadState.refresh is LoadState.Error
                binding.errorMsg.isVisible = loadState.refresh is LoadState.Error
                if (loadState.refresh is LoadState.Error) {
                    binding.errorMsg.text = (loadState.refresh as LoadState.Error).error.localizedMessage
                }
            }
        }

        binding.recyclerView.apply {
            transitionName = getString(R.string.youtube_playlist_transition_name)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = infoItemAdapter.withLoadStateFooter(LoadStateAdapter { infoItemAdapter.retry() })
            addOnClickListener { pos, _ ->
                val item = infoItemAdapter.getItemByPosition(pos)
                if (item is StreamInfoItem) {
                    playbackViewModel.playMedia(requireActivity(), YouTubeStreamExtractor.extractId(item.url)!!, bundleOf(

                    ))
                }
            }
        }

        lifecycleScope.launch {
            viewModel.getPlaylist(url).collectLatest {
                infoItemAdapter.submitData(it)
            }
        }
    }
}