package com.zionhuang.music.models

import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*

data class PlaybackStateData(
    @State val state: Int = STATE_NONE,
    @ShuffleMode val shuffleMode: Int = SHUFFLE_MODE_NONE,
    @RepeatMode val repeatMode: Int = REPEAT_MODE_NONE,
    @Actions val actions: Long = 0,
    val errorCode: Int = 0,
    val errorMessage: String? = null,
) {
    companion object {
        fun from(mediaController: MediaControllerCompat, playbackState: PlaybackStateCompat) = PlaybackStateData(
            playbackState.state,
            mediaController.shuffleMode,
            mediaController.repeatMode,
            playbackState.actions,
            playbackState.errorCode,
            playbackState.errorMessage?.toString()
        )
    }
}
