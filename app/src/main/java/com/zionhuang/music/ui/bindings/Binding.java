package com.zionhuang.music.ui.bindings;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.api.client.util.DateTime;
import com.zionhuang.music.R;
import com.zionhuang.music.utils.GlideApp;

import static android.support.v4.media.session.PlaybackStateCompat.STATE_NONE;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED;
import static com.zionhuang.music.utils.Utils.makeTimeString;

public class Binding {
    @BindingAdapter("duration")
    public static void setDuration(TextView v, int duration) {
        v.setText(makeTimeString(duration));
    }

    @BindingAdapter("playState")
    public static void setPlayState(ImageView v, int state) {
        if (state == STATE_PAUSED || state == STATE_NONE) {
            v.setImageResource(R.drawable.ic_baseline_play_arrow_48);
        } else {
            v.setImageResource(R.drawable.ic_baseline_pause_48);
        }
    }

    @BindingAdapter("coverId")
    public static void setCoverId(ImageView v, String id) {
        GlideApp.with(v)
                .load("https://i3.ytimg.com/vi/" + id + "/maxresdefault.jpg")
                .placeholder(R.drawable.ic_round_music_note_24)
                .transform(new MultiTransformation<>(new CenterCrop(), new RoundedCorners((int) (v.getContext().getResources().getDimensionPixelSize(R.dimen.song_cover_radius)))))
                .into(v);
    }

    @BindingAdapter("publishDate")
    public static void setPublishDate(TextView v, DateTime date) {
        v.setText(date.toString());
    }
}
