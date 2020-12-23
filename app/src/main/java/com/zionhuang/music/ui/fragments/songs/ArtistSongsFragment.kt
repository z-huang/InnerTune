package com.zionhuang.music.ui.fragments.songs

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialContainerTransform
import com.zionhuang.music.R
import com.zionhuang.music.databinding.LayoutArtistSongsBinding
import com.zionhuang.music.download.DownloadHandler
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.themeColor
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.playback.queue.Queue
import com.zionhuang.music.ui.adapters.SongsAdapter
import com.zionhuang.music.ui.fragments.base.MainFragment
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArtistSongsFragment : MainFragment<LayoutArtistSongsBinding>() {
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
        binding.recyclerView.doOnPreDraw {
            startPostponedEnterTransition()
        }
        val artistId = ArtistSongsFragmentArgs.fromBundle(requireArguments()).artistId
        songsViewModel.addDownloadListener(downloadHandler.downloadListener)
        val songsAdapter = SongsAdapter(songsViewModel.songPopupMenuListener, downloadHandler)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songsAdapter
            addOnClickListener { pos, _ ->
                playbackViewModel.playMedia(SongParcel.fromSong(songsAdapter.getItemByPosition(pos)!!), Queue.QUEUE_ALL_SONG)
            }
        }
        lifecycleScope.launch {
            activity.title = songsViewModel.songRepository.getArtistById(artistId)!!.name
            songsViewModel.getArtistSongsAsFlow(artistId).collectLatest {
                songsAdapter.submitData(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        songsViewModel.removeDownloadListener(downloadHandler.downloadListener)
    }

    companion object {
        val TAG = "ArtistSongsFragment"
    }
}