package com.zionhuang.music.ui.widgets

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.provider.Settings
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.slider.Slider
import com.zionhuang.music.utils.makeTimeString

class MediaWidgetsController(
    context: Context,
    private val progressBar: ProgressBar,
    private val slider: Slider,
    private val progressTextView: TextView,
) {
    private var sliderIsTracking = false
    private var mediaController: MediaControllerCompat? = null

    private var controllerCallback: ControllerCallback? = null
    private var progressAnimator: ValueAnimator? = null
    private var duration: Long = 0
    private val durationScale: Float = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

    init {
        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                sliderIsTracking = true
            }

            override fun onStopTrackingTouch(slider: Slider) {
                mediaController?.transportControls?.seekTo(slider.value.toLong())
                sliderIsTracking = false
            }

        })
        slider.addOnChangeListener { _, value, _ ->
            progressTextView.text = makeTimeString((value).toLong())
        }
    }

    fun setMediaController(newController: MediaControllerCompat?) {
        if (newController != null) {
            controllerCallback = ControllerCallback().also {
                newController.registerCallback(it)
                it.onMetadataChanged(newController.metadata)
                it.onPlaybackStateChanged(newController.playbackState)
            }
        } else if (mediaController != null) {
            if (controllerCallback != null) {
                mediaController!!.unregisterCallback(controllerCallback!!)
                controllerCallback = null
            }
        }
        mediaController = newController
    }

    fun disconnectController() {
        if (mediaController != null) {
            mediaController!!.unregisterCallback(controllerCallback!!)
            controllerCallback = null
            mediaController = null
        }
    }

    private inner class ControllerCallback : MediaControllerCompat.Callback(), AnimatorUpdateListener {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            state ?: return

            progressAnimator?.cancel()
            progressAnimator = null

            val progress = state.position.toInt()
            progressBar.progress = progress
            if (slider.isEnabled) {
                slider.value = progress.toFloat().coerceIn(slider.valueFrom, slider.valueTo)
            }
            progressTextView.text = makeTimeString(progress.toLong())
            if (state.state == PlaybackStateCompat.STATE_PLAYING) {
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
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            super.onMetadataChanged(metadata)
            duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
            progressBar.max = duration.toInt()
            if (duration <= 0L) {
                slider.isEnabled = false
            } else {
                slider.isEnabled = true
                slider.valueTo = duration.toFloat()
            }
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
            progressBar.progress = animatedValue
            slider.value = animatedValue.toFloat().coerceIn(slider.valueFrom, slider.valueTo)
            progressTextView.text = makeTimeString(animatedValue.toLong())
        }
    }
}