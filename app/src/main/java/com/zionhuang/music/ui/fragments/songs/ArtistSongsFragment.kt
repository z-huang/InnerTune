package com.zionhuang.music.ui.fragments.songs

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialContainerTransform
import com.zionhuang.music.R
import com.zionhuang.music.constants.*
import com.zionhuang.music.constants.MediaConstants.EXTRA_ARTIST_ID
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_DESC
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_ORDER
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_TYPE
import com.zionhuang.music.constants.MediaConstants.QUEUE_ARTIST
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.download.DownloadHandler
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.themeColor
import com.zionhuang.music.ui.adapters.SongsAdapter
import com.zionhuang.music.ui.fragments.base.MainFragment
import com.zionhuang.music.ui.listeners.SortMenuListener
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArtistSongsFragment : MainFragment<LayoutRecyclerviewBinding>() {
    private val args: ArtistSongsFragmentArgs by navArgs()
    private val artistId by lazy { args.artistId }

    private val playbackViewModel by activityViewModels<PlaybackViewModel>()
    private val songsViewModel by activityViewModels<SongsViewModel>()
    private lateinit var songsAdapter: SongsAdapter
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
        view.doOnPreDraw { startPostponedEnterTransition() }

        songsViewModel.downloadServiceConnection.addDownloadListener(downloadHandler.downloadListener)

        songsAdapter = SongsAdapter(songsViewModel.songPopupMenuListener, downloadHandler).apply {
            sortMenuListener = this@ArtistSongsFragment.sortMenuListener
        }

        binding.recyclerView.apply {
            transitionName = getString(R.string.artist_songs_transition_name)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songsAdapter
            addOnClickListener { pos, _ ->
                if (pos == 0) return@addOnClickListener
                playbackViewModel.playMedia(requireActivity(), songsAdapter.getItemByPosition(pos)!!.id, bundleOf(
                        EXTRA_ARTIST_ID to artistId,
                        EXTRA_QUEUE_TYPE to QUEUE_ARTIST,
                        EXTRA_QUEUE_ORDER to sortMenuListener.sortType(),
                        EXTRA_QUEUE_DESC to sortMenuListener.sortDescending()
                ))
            }
        }

        lifecycleScope.launch {
            activity.title = songsViewModel.songRepository.getArtist(artistId)!!.name
            songsViewModel.getArtistSongsAsFlow(artistId).collectLatest {
                songsAdapter.submitData(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        songsViewModel.downloadServiceConnection.removeDownloadListener(downloadHandler.downloadListener)
    }

    private val sortMenuListener = object : SortMenuListener {
        @IdRes
        override fun sortType(): Int = songsViewModel.sortType
        override fun sortDescending(): Boolean = songsViewModel.sortDescending
        override fun sortByCreateDate() = updateSortType(ORDER_CREATE_DATE)
        override fun sortByName() = updateSortType(ORDER_NAME)
        override fun sortByArtist() = updateSortType(ORDER_ARTIST)
        override fun toggleSortOrder() {
            songsViewModel.sortDescending = !songsViewModel.sortDescending
            songsAdapter.refresh()
        }
    }

    private fun updateSortType(@SongSortType sortType: Int) {
        songsViewModel.sortType = sortType
        songsAdapter.refresh()
    }

    companion object {
        val TAG = "ArtistSongsFragment"
    }
}