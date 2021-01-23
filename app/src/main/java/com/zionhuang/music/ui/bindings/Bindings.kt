package com.zionhuang.music.ui.bindings

import android.support.v4.media.session.PlaybackStateCompat
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.api.client.util.DateTime
import com.google.api.services.youtube.model.SearchResult
import com.zionhuang.music.R
import com.zionhuang.music.extensions.circle
import com.zionhuang.music.extensions.load
import com.zionhuang.music.ui.widgets.PlayPauseButton
import com.zionhuang.music.utils.GlideApp
import com.zionhuang.music.utils.getMaxResThumbnailUrl
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
fun setPlayState(view: PlayPauseButton, state: Int) {
    if (state == PlaybackStateCompat.STATE_PAUSED || state == PlaybackStateCompat.STATE_NONE) {
        view.animationPause()
    } else {
        view.animatePlay()
    }
}

@BindingAdapter("coverId")
fun setCoverId(view: ImageView, id: String?) {
    GlideApp.with(view)
            .load("https://i3.ytimg.com/vi/$id/maxresdefault.jpg")
            .placeholder(R.drawable.ic_music_note)
            .transform(MultiTransformation(CenterCrop(), RoundedCorners(view.context.resources.getDimensionPixelSize(R.dimen.song_cover_radius))))
            .into(view)
}

@BindingAdapter("thumbnails")
fun setThumbnails(view: ImageView, item: SearchResult) {
    val url = if (item.id.kind == "youtube#video") {
        "https://i3.ytimg.com/vi/" + item.id.videoId + "/maxresdefault.jpg"
    } else {
        getMaxResThumbnailUrl(item.snippet.thumbnails)
    }
    GlideApp.with(view)
            .load(url)
            .placeholder(R.drawable.ic_music_note)
            .transform(MultiTransformation(CenterCrop(), RoundedCorners(view.context.resources.getDimensionPixelSize(R.dimen.song_cover_radius))))
            .into(view)
}

@BindingAdapter("publishDate")
fun setPublishDate(view: TextView, date: DateTime) {
    view.text = date.toString()
}

@BindingAdapter("srcUrl", "circleCrop", requireAll = false)
fun setUrl(view: ImageView, url: String? = null, circleCrop: Boolean = false) {
    url?.let {
        view.load(it) {
            if (circleCrop) circle()
        }
    }
}