package com.zionhuang.music.ui.fragments.songs

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialContainerTransform
import com.zionhuang.music.R
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.download.DownloadHandler
import com.zionhuang.music.extensions.themeColor
import com.zionhuang.music.ui.adapters.ChannelSongsAdapter
import com.zionhuang.music.ui.fragments.base.MainFragment
import com.zionhuang.music.viewmodels.ChannelViewModel
import com.zionhuang.music.viewmodels.ChannelViewModelFactory
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChannelSongsFragment : MainFragment<LayoutRecyclerviewBinding>() {
    private val args: ChannelSongsFragmentArgs by navArgs()
    private val channelId by lazy { args.channelId }

    private val playbackViewModel by activityViewModels<PlaybackViewModel>()
    private val songsViewModel by activityViewModels<SongsViewModel>()
    private val channelViewModel by viewModels<ChannelViewModel> { ChannelViewModelFactory(requireActivity().application, channelId) }
    private val downloadHandler = DownloadHandler()

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
        postponeEnterTransition()
        binding.recyclerView.doOnPreDraw { startPostponedEnterTransition() }

        songsViewModel.downloadServiceConnection.addDownloadListener(downloadHandler.downloadListener)
        val channelSongsAdapter = ChannelSongsAdapter(songsViewModel.songPopupMenuListener, downloadHandler, channelViewModel, viewLifecycleOwner)
        binding.recyclerView.apply {
            transitionName = getString(R.string.channel_songs_transition_name)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = channelSongsAdapter
        }

        channelViewModel.channel.observe(viewLifecycleOwner) { channel ->
            activity.title = channel.name
        }

        lifecycleScope.launch {
            songsViewModel.getChannelSongsAsFlow(channelId).collectLatest {
                channelSongsAdapter.submitData(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        songsViewModel.downloadServiceConnection.removeDownloadListener(downloadHandler.downloadListener)
    }

    companion object {
        const val TAG = "ChannelSongsFragment"
    }
}