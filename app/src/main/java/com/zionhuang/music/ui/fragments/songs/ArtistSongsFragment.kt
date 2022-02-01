package com.zionhuang.music.ui.fragments.songs

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialContainerTransform
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_DATA
import com.zionhuang.music.constants.MediaConstants.QUEUE_ARTIST
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.themeColor
import com.zionhuang.music.models.QueueData
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.adapters.SongsAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArtistSongsFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    override fun getViewBinding() = LayoutRecyclerviewBinding.inflate(layoutInflater)

    private val args: ArtistSongsFragmentArgs by navArgs()
    private val artistId by lazy { args.artistId }

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
            sortInfo = songsViewModel.sortInfo
            downloadInfo = songsViewModel.downloadInfoLiveData
        }

        binding.recyclerView.apply {
            transitionName = getString(R.string.artist_songs_transition_name)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songsAdapter
            addOnClickListener { pos, _ ->
                if (pos == 0) return@addOnClickListener
                playbackViewModel.playMedia(
                    requireActivity(), songsAdapter.getItemByPosition(pos)!!.id, bundleOf(
                        EXTRA_QUEUE_DATA to QueueData(QUEUE_ARTIST, sortInfo = songsViewModel.sortInfo.parcelize())
                    )
                )
            }
        }

        lifecycleScope.launch {
            (requireActivity() as MainActivity).title = songsViewModel.songRepository.getArtistById(artistId)!!.name
            songsViewModel.getArtistSongsAsFlow(artistId).collectLatest {
                songsAdapter.submitData(it)
            }
        }

        songsViewModel.sortInfo.liveData.observe(viewLifecycleOwner) {
            songsAdapter.refresh()
        }

        songsViewModel.downloadInfoLiveData.observe(viewLifecycleOwner) { map ->
            map.forEach { (key, value) ->
                songsAdapter.setProgress(key, value)
            }
        }
    }

    companion object {
        val TAG = "ArtistSongsFragment"
    }
}