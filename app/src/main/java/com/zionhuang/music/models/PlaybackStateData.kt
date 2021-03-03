package com.zionhuang.music.models

import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*

data class PlaybackStateData(
        @State var state: Int = STATE_NONE,
        @ShuffleMode var shuffleMode: Int = SHUFFLE_MODE_NONE,
        @RepeatMode var repeatMode: Int = REPEAT_MODE_NONE,
        @Actions var actions: Long = 0,
) {
    fun pullPlaybackState(playbackState: PlaybackStateCompat, mediaController: MediaControllerCompat?): PlaybackStateData = apply {
        state = playbackState.state
        if (mediaController != null) {
            shuffleMode = mediaController.shuffleMode
            repeatMode = mediaController.repeatMode
        }
        actions = playbackState.actions
    }
}
