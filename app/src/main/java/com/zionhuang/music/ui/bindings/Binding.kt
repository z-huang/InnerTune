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
import com.zionhuang.music.utils.GlideApp
import com.zionhuang.music.utils.getMaxResThumbnailUrl
import com.zionhuang.music.utils.makeTimeString

@BindingAdapter("duration")
fun setDuration(view: TextView, duration: Int) {
    view.text = makeTimeString(duration.toLong())
}

@BindingAdapter("playState")
fun setPlayState(view: ImageView, state: Int) {
    if (state == PlaybackStateCompat.STATE_PAUSED || state == PlaybackStateCompat.STATE_NONE) {
        view.setImageResource(R.drawable.ic_baseline_play_arrow_48)
    } else {
        view.setImageResource(R.drawable.ic_baseline_pause_48)
    }
}

@BindingAdapter("coverId")
fun setCoverId(view: ImageView, id: String) {
    GlideApp.with(view)
            .load("https://i3.ytimg.com/vi/$id/maxresdefault.jpg")
            .placeholder(R.drawable.ic_round_music_note_24)
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
            .placeholder(R.drawable.ic_round_music_note_24)
            .transform(MultiTransformation(CenterCrop(), RoundedCorners(view.context.resources.getDimensionPixelSize(R.dimen.song_cover_radius))))
            .into(view)
}

@BindingAdapter("publishDate")
fun setPublishDate(view: TextView, date: DateTime) {
    view.text = date.toString()
}
