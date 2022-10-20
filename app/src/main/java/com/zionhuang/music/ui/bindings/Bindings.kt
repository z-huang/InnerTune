package com.zionhuang.music.ui.bindings

import android.graphics.drawable.Drawable
import android.support.v4.media.session.PlaybackStateCompat.*
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.zionhuang.innertube.models.Thumbnail
import com.zionhuang.music.extensions.getDensity
import com.zionhuang.music.ui.widgets.PlayPauseButton
import com.zionhuang.music.ui.widgets.RepeatButton
import com.zionhuang.music.ui.widgets.ShuffleButton
import com.zionhuang.music.utils.makeTimeString
import kotlin.math.roundToInt

@BindingAdapter("enabled")
fun setEnabled(view: View, enabled: Boolean) {
    view.isEnabled = enabled
    view.alpha = if (enabled) 1f else 0.5f
}

@BindingAdapter("duration")
fun setDuration(view: TextView, duration: Long) {
    view.text = makeTimeString(duration)
}

@BindingAdapter("playState")
fun setPlayState(view: PlayPauseButton, @State state: Int) {
    if (state == STATE_PAUSED || state == STATE_ERROR || state == STATE_NONE) {
        view.animationPause()
    } else if (state == STATE_PLAYING) {
        view.animatePlay()
    }
}

@BindingAdapter("shuffleMode")
fun setShuffleMode(view: ShuffleButton, @ShuffleMode mode: Int) {
    when (mode) {
        SHUFFLE_MODE_NONE, SHUFFLE_MODE_INVALID -> view.disable()
        SHUFFLE_MODE_ALL, SHUFFLE_MODE_GROUP -> view.enable()
    }
}

@BindingAdapter("repeatMode")
fun setRepeatMode(view: RepeatButton, @RepeatMode state: Int) {
    view.setState(state)
}

@BindingAdapter("srcUrl", "cornerRadius", "circleCrop", "placeholder", "thumbnailWidth", "thumbnailHeight", requireAll = false)
fun setImageUrl(
    view: ImageView,
    url: String?,
    cornerRadius: Float?,
    circleCrop: Boolean?,
    placeholder: Drawable?,
    thumbnailWidth: Float?,
    thumbnailHeight: Float?,
) {
    val density = view.context.getDensity()
    val resizedUrl = if (url != null) resizeThumbnailUrl(url, thumbnailWidth?.let { (it * density).roundToInt() }, thumbnailHeight?.let { (it * density).roundToInt() }) else null
    view.load(resizedUrl) {
        crossfade(true)
        scale(Scale.FIT)
        // the order of the following two lines is important. If circleCrop, ignore cornerRadius
        if (cornerRadius != null) transformations(RoundedCornersTransformation(cornerRadius))
        if (circleCrop == true) transformations(CircleCropTransformation())
        if (placeholder != null) {
            placeholder(placeholder)
            error(placeholder)
        }
    }
}

@BindingAdapter("thumbnails", "cornerRadius", "circleCrop", "placeholder", "thumbnailWidth", "thumbnailHeight", requireAll = false)
fun setThumbnails(
    view: ImageView,
    thumbnails: List<Thumbnail>?,
    cornerRadius: Float?,
    circleCrop: Boolean?,
    placeholder: Drawable?,
    thumbnailWidth: Float?,
    thumbnailHeight: Float?,
) = setImageUrl(view, thumbnails?.lastOrNull()?.url, cornerRadius, circleCrop, placeholder, thumbnailWidth, thumbnailHeight)

fun resizeThumbnailUrl(url: String, width: Int?, height: Int?): String {
    if (width == null && height == null) return url
    "https://lh3\\.googleusercontent\\.com/.*=w(\\d+)-h(\\d+).*".toRegex().matchEntire(url)?.groupValues?.let { group ->
        val (W, H) = group.drop(1).map { it.toInt() }
        var w = width
        var h = height
        if (w != null && h == null) h = (w / W) * H
        if (w == null && h != null) w = (h / H) * W
        return "$url-w$w-h$h"
    }
    if (url matches "https://yt3\\.ggpht\\.com/.*=s(\\d+)".toRegex()) {
        return "$url-s${width ?: height}"
    }
    return url
}