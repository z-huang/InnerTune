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
import com.zionhuang.music.constants.MediaConstants.EXTRA_PLAYLIST_ID
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_DESC
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_ORDER
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_TYPE
import com.zionhuang.music.constants.MediaConstants.QUEUE_PLAYLIST
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.themeColor
import com.zionhuang.music.ui.adapters.SongsAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.ui.listeners.SortMenuListener
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlaylistSongsFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    override fun getViewBinding() = LayoutRecyclerviewBinding.inflate(layoutInflater)

    private val args: PlaylistSongsFragmentArgs by navArgs()
    private val playlistId by lazy { args.playlistId }

    private val playbackViewModel by activityViewModels<PlaybackViewModel>()
    private val songsViewModel by activityViewModels<SongsViewModel>()
    private val songsAdapter = SongsAdapter()

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

        songsAdapter.apply {
            popupMenuListener = songsViewModel.songPopupMenuListener
            sortMenuListener = this@PlaylistSongsFragment.sortMenuListener
        }

        binding.recyclerView.apply {
            transitionName = getString(R.string.playlist_songs_transition_name)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songsAdapter
            addOnClickListener { pos, _ ->
                if (pos == 0) return@addOnClickListener
                playbackViewModel.playMedia(
                    requireActivity(), songsAdapter.getItemByPosition(pos)!!.songId, bundleOf(
                        EXTRA_PLAYLIST_ID to playlistId,
                        EXTRA_QUEUE_TYPE to QUEUE_ARTIST,
                        EXTRA_QUEUE_ORDER to sortMenuListener.sortType(),
                        EXTRA_QUEUE_DESC to sortMenuListener.sortDescending()
                    )
                )
            }
        }

        lifecycleScope.launch {
            //activity.title = songsViewModel.songRepository.getArtist(artistId)!!.name
            songsViewModel.getPlaylistSongsAsFlow(playlistId).collectLatest {
                songsAdapter.submitData(it)
            }
            //val items = songsViewModel.songRepository.getPlaylistSongsList(playlistId)
            //Log.d(TAG, items.toString())
        }
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