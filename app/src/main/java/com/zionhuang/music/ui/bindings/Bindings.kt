package com.zionhuang.music.ui.bindings

import android.net.Uri
import android.support.v4.media.session.PlaybackStateCompat.*
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.zionhuang.innertube.models.Thumbnail
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.ArtworkType
import com.zionhuang.music.constants.MediaConstants.TYPE_RECTANGLE
import com.zionhuang.music.constants.MediaConstants.TYPE_SQUARE
import com.zionhuang.music.extensions.circle
import com.zionhuang.music.extensions.fullResolution
import com.zionhuang.music.extensions.load
import com.zionhuang.music.extensions.roundCorner
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.widgets.PlayPauseButton
import com.zionhuang.music.ui.widgets.RepeatButton
import com.zionhuang.music.ui.widgets.ShuffleButton
import com.zionhuang.music.utils.makeTimeString

@BindingAdapter("enabled")
fun setEnabled(view: View, enabled: Boolean) {
    view.isEnabled = enabled
    view.alpha = if (enabled) 1f else 0.5f
}

@BindingAdapter("artworkType")
fun setArtworkType(view: ImageView, @ArtworkType source: Int) {
    view.scaleType = when (source) {
        TYPE_SQUARE -> ImageView.ScaleType.CENTER_CROP
        TYPE_RECTANGLE -> ImageView.ScaleType.FIT_CENTER
        else -> throw IllegalArgumentException("Unknown artwork type.")
    }
}

@BindingAdapter("duration")
fun setDuration(view: TextView, duration: Int) {
    view.text = makeTimeString(duration.toLong())
}

@BindingAdapter("duration")
fun setDuration(view: TextView, duration: Long) {
    view.text = makeTimeString(duration)
}

@BindingAdapter("playState")
fun setPlayState(view: PlayPauseButton, @State state: Int) {
    if (state == STATE_PAUSED || state == STATE_NONE) {
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

@BindingAdapter("artworkId")
fun setArtworkId(view: ImageView, id: String) {
    view.load(SongRepository.getSongArtworkFile(id)) {
        placeholder(R.drawable.ic_music_note)
        roundCorner(view.context.resources.getDimensionPixelSize(R.dimen.song_cover_radius))
    }
}

@BindingAdapter("artworkUri")
fun setArtworkUri(view: ImageView, uri: Uri) {
    view.load(uri) {
        placeholder(R.drawable.ic_music_note)
        roundCorner(view.context.resources.getDimensionPixelSize(R.dimen.song_cover_radius))
    }
}

@BindingAdapter("srcUrl", "circleCrop", "fullResolution", requireAll = false)
fun setUrl(
    view: ImageView,
    url: String? = null,
    circleCrop: Boolean = false,
    fullResolution: Boolean = false,
) {
    url?.let {
        view.load(it) {
            if (circleCrop) circle()
            if (fullResolution) fullResolution()
        }
    }
}

@BindingAdapter("thumbnails", "roundedCorner", "circleCrop", requireAll = false)
fun setThumbnails(
    view: ImageView,
    thumbnails: List<Thumbnail>?,
    roundedCorner: Boolean?,
    circleCrop: Boolean?,
) {
    thumbnails?.lastOrNull()?.let {
        view.load(it.url) {
            if (circleCrop == true) circle()
            if (roundedCorner == null || roundedCorner) {
                transform(MultiTransformation(FitCenter(), RoundedCorners(view.context.resources.getDimensionPixelSize(R.dimen.song_cover_radius))))
            }
        }
    }
}