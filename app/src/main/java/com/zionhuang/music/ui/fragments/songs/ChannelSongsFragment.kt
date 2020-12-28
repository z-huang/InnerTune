package com.zionhuang.music.ui.fragments.songs

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialContainerTransform
import com.zionhuang.music.R
import com.zionhuang.music.databinding.LayoutChannelSongsBinding
import com.zionhuang.music.download.DownloadHandler
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.circle
import com.zionhuang.music.extensions.load
import com.zionhuang.music.extensions.themeColor
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.playback.queue.Queue
import com.zionhuang.music.ui.adapters.SongsAdapter
import com.zionhuang.music.ui.fragments.base.MainFragment
import com.zionhuang.music.utils.makeTimeString
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import com.zionhuang.music.youtube.YouTubeExtractor
import com.zionhuang.music.youtube.models.YouTubeChannel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChannelSongsFragment : MainFragment<LayoutChannelSongsBinding>() {
    private val args: ChannelSongsFragmentArgs by navArgs()
    private val channelId by lazy { args.channelId }

    private val playbackViewModel by activityViewModels<PlaybackViewModel>()
    private val songsViewModel by activityViewModels<SongsViewModel>()
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

        songsViewModel.addDownloadListener(downloadHandler.downloadListener)
        val songsAdapter = SongsAdapter(songsViewModel.songPopupMenuListener, downloadHandler)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songsAdapter
            addOnClickListener { pos, _ ->
                playbackViewModel.playMedia(SongParcel.fromSong(songsAdapter.getItemByPosition(pos)!!), Queue.QUEUE_ALL_SONG)
            }
        }

        songsViewModel.songRepository.channelSongsCount(channelId).observe(viewLifecycleOwner) { count ->
            binding.songsCount.text = resources.getQuantityString(R.plurals.channel_songs_count, count, count)
        }
        songsViewModel.songRepository.channelSongsDuration(channelId).observe(viewLifecycleOwner) { duration ->
            binding.totalDuration.text = makeTimeString(duration)
        }
        lifecycleScope.launch {
            songsViewModel.songRepository.getChannel(channelId)!!.name.let {
                activity.title = it
                binding.channelName.text = it
            }
            when (val channel = YouTubeExtractor.getInstance(requireContext()).getChannel(channelId)) {
                is YouTubeChannel.Success -> {
                    Log.d(TAG, channel.toString())
                    channel.bannerUrl?.let { binding.banner.load(it) }
                    channel.avatarUrl?.let { binding.avatar.load(it) { circle() } }
                }
                is YouTubeChannel.Error -> {
                    Log.d(TAG, "Get channel error ${channel.errorMessage}")
                }
            }
            songsViewModel.getChannelSongsAsFlow(channelId).collectLatest {
                songsAdapter.submitData(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        songsViewModel.removeDownloadListener(downloadHandler.downloadListener)
    }

    companion object {
        val TAG = "ChannelSongsFragment"
    }
}