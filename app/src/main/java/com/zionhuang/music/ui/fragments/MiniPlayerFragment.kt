package com.zionhuang.music.ui.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.provider.Settings
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.zionhuang.music.databinding.FragmentMiniPlayerBinding
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.viewmodels.PlaybackViewModel

class MiniPlayerFragment : Fragment() {
    private lateinit var binding: FragmentMiniPlayerBinding
    private val viewModel by activityViewModels<PlaybackViewModel>()
    private val mainActivity: MainActivity get() = requireActivity() as MainActivity
    private var sliderIsTracking: Boolean = false
    private var mediaController: MediaControllerCompat? = null
    private val mediaControllerCallback = ControllerCallback()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMiniPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        // Marquee
        binding.songTitle.isSelected = true
        binding.songArtist.isSelected = true

        viewModel.playbackState.observe(viewLifecycleOwner) { playbackState ->
            if (playbackState.state != STATE_NONE && playbackState.state != STATE_STOPPED) {
                if (mainActivity.bottomSheetBehavior.state == STATE_HIDDEN) {
                    mainActivity.bottomSheetBehavior.state = STATE_COLLAPSED
                }
            }
        }
        binding.root.setOnClickListener {
            mainActivity.bottomSheetBehavior.state = STATE_EXPANDED
        }
        viewModel.mediaController.observe(viewLifecycleOwner) {
            mediaController?.unregisterCallback(mediaControllerCallback)
            mediaController = it
            it?.registerCallback(mediaControllerCallback)
            if (it != null) {
                mediaControllerCallback.onMetadataChanged(it.metadata)
                mediaControllerCallback.onPlaybackStateChanged(it.playbackState)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaController?.unregisterCallback(mediaControllerCallback)
    }

    private var duration: Long = 0
    private var progressAnimator: ValueAnimator? = null
    private val durationScale: Float by lazy { Settings.Global.getFloat(requireContext().contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) }

    private inner class ControllerCallback : MediaControllerCompat.Callback(), ValueAnimator.AnimatorUpdateListener {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            state ?: return

            progressAnimator?.cancel()
            progressAnimator = null

            val progress = state.position.toInt()
            binding.progressBar.progress = progress
            if (state.state == STATE_PLAYING) {
                val timeToEnd = ((duration - progress) / state.playbackSpeed).toInt()
                if (timeToEnd > 0) {
                    progressAnimator?.cancel()
                    progressAnimator = ValueAnimator.ofInt(progress, duration.toInt()).apply {
                        duration = (timeToEnd / durationScale).toLong()
                        interpolator = LinearInterpolator()
                        addUpdateListener(this@ControllerCallback)
                        start()
                    }
                }
            }
            binding.progressBar.isIndeterminate = state.state == STATE_BUFFERING
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            super.onMetadataChanged(metadata)
            duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
            binding.progressBar.max = duration.toInt()
            mediaController?.let {
                onPlaybackStateChanged(it.playbackState)
            }
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            if (sliderIsTracking) {
                animation.cancel()
                return
            }
            val animatedValue = animation.animatedValue as Int
            binding.progressBar.progress = animatedValue
        }
    }
}