package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.NeoBottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.NeoBottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.slider.Slider
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.BrowseLocalArtistSongsEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_TOGGLE_LIKE
import com.zionhuang.music.databinding.BottomControlsSheetBinding
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.utils.makeTimeString
import com.zionhuang.music.viewmodels.PlaybackViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BottomControlsFragment : Fragment() {
    lateinit var binding: BottomControlsSheetBinding
    private val viewModel by activityViewModels<PlaybackViewModel>()
    private val mainActivity: MainActivity get() = requireActivity() as MainActivity
    private var sliderIsTracking: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomControlsSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        // Marquee
        binding.songTitle.isSelected = true
        binding.songArtist.isSelected = true

        binding.songArtist.setOnClickListener {
            val mediaMetadata = MediaSessionConnection.binder?.songPlayer?.currentMediaMetadata?.value ?: return@setOnClickListener
            if (mediaMetadata.artists.isNotEmpty()) {
                val artist = mediaMetadata.artists[0]
                NavigationEndpointHandler(mainActivity.currentFragment!!).handle(if (artist.id.startsWith("UC")) {
                    BrowseEndpoint.artistBrowseEndpoint(artist.id)
                } else {
                    BrowseLocalArtistSongsEndpoint(artist.id)
                })
                mainActivity.collapseBottomSheet()
            }
        }

        binding.btnFavorite.setOnClickListener {
            viewModel.transportControls?.sendCustomAction(ACTION_TOGGLE_LIKE, null)
        }

        binding.slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                sliderIsTracking = true
            }

            override fun onStopTrackingTouch(slider: Slider) {
                MediaSessionConnection.mediaController?.transportControls?.seekTo(slider.value.toLong())
                sliderIsTracking = false
            }
        })
        binding.slider.addOnChangeListener { _, value, _ ->
            binding.position.text = makeTimeString((value).toLong())
        }

        lifecycleScope.launch {
            viewModel.playbackState.collect { playbackState ->
                if (playbackState.state != PlaybackStateCompat.STATE_NONE && playbackState.state != PlaybackStateCompat.STATE_STOPPED) {
                    if (mainActivity.bottomSheetBehavior.state == STATE_HIDDEN) {
                        mainActivity.bottomSheetBehavior.state = STATE_COLLAPSED
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewModel.currentSong.collectLatest { song ->
                binding.btnFavorite.setImageResource(if (song?.song?.liked == true) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
            }
        }
        lifecycleScope.launch {
            viewModel.position.collect { position ->
                if (!sliderIsTracking && binding.slider.isEnabled) {
                    binding.slider.value = position.toFloat().coerceIn(binding.slider.valueFrom, binding.slider.valueTo)
                    binding.position.text = makeTimeString(position)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.duration.collect { duration ->
                if (duration <= 0) {
                    binding.slider.isEnabled = false
                    binding.duration.text = ""
                } else {
                    binding.slider.isEnabled = true
                    binding.slider.valueTo = duration.toFloat()
                    binding.duration.text = makeTimeString(duration)
                }
            }
        }
    }
}