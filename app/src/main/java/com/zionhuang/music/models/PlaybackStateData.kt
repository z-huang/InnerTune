package com.zionhuang.music.models

import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.zionhuang.music.constants.MediaSessionConstants.REPEAT_MODE
import com.zionhuang.music.constants.MediaSessionConstants.SHUFFLE_MODE

data class PlaybackStateData(
        @ShuffleMode var shuffleMode: Int = SHUFFLE_MODE_NONE,
        @RepeatMode var repeatMode: Int = REPEAT_MODE_NONE,
        @State var state: Int = STATE_NONE,
) {
    fun pullPlaybackState(playbackState: PlaybackStateCompat): PlaybackStateData = apply {
        state = playbackState.state
        playbackState.extras?.let {
            shuffleMode = it.getInt(SHUFFLE_MODE)
            repeatMode = it.getInt(REPEAT_MODE)
        }
    }
}
