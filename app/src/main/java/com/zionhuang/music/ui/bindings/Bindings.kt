package com.zionhuang.music.ui.bindings

import android.support.v4.media.session.PlaybackStateCompat.*
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.api.client.util.DateTime
import com.google.api.services.youtube.model.ThumbnailDetails
import com.zionhuang.music.R
import com.zionhuang.music.extensions.*
import com.zionhuang.music.ui.widgets.PlayPauseButton
import com.zionhuang.music.ui.widgets.ShuffleButton
import com.zionhuang.music.utils.makeTimeString

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
        SHUFFLE_MODE_INVALID -> view.disable()
        SHUFFLE_MODE_NONE -> view.disable()
        SHUFFLE_MODE_ALL -> view.enable()
        SHUFFLE_MODE_GROUP -> view.enable()
    }
}

@BindingAdapter("artwork")
fun setArtwork(view: ImageView, id: String) {
    view.load(view.context.getArtworkFile(id)) {
        placeholder(R.drawable.ic_music_note)
        roundCorner(view.context.resources.getDimensionPixelSize(R.dimen.song_cover_radius))
    }
}

@BindingAdapter("thumbnail")
fun setThumbnail(view: ImageView, thumbnail: ThumbnailDetails) {
    view.load(thumbnail.maxResUrl) {
        placeholder(R.drawable.ic_music_note)
        roundCorner(view.context.resources.getDimensionPixelSize(R.dimen.song_cover_radius))
    }
}

@BindingAdapter("publishDate")
fun setPublishDate(view: TextView, date: DateTime) {
    view.text = date.toString()
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