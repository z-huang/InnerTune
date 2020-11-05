package com.zionhuang.music.playback

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.PlayerView

typealias OnDurationSet = (Long) -> Unit
typealias OnPlaybackStateChanged = (state: Int) -> Unit

/**
 * A wrapper around [SimpleExoPlayer]
 */

class MusicPlayer internal constructor(context: Context) : Player.EventListener {
    companion object {
        private const val TAG = "MusicPlayer"
    }

    private var player: SimpleExoPlayer = SimpleExoPlayer.Builder(context).build().apply {
        addListener(this@MusicPlayer)
        playWhenReady = true
    }

    private var mDurationSet = false
    private var onDurationSet: OnDurationSet = {}
    private var onPlaybackStateChanged: OnPlaybackStateChanged = {}

    fun play() {
        player.playWhenReady = true
    }

    fun pause() {
        player.playWhenReady = false
    }

    fun seekTo(pos: Long) {
        player.seekTo(pos)
    }

    val isPlaying: Boolean
        get() = player.playWhenReady

    fun stop() {
        player.stop(true)
    }

    fun setSource(uri: Uri) {
        player.apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            seekTo(0)
        }
        mDurationSet = false
    }

    val position: Long
        get() = player.currentPosition
    val duration: Long
        get() = player.duration
    var playbackSpeed: Float
        get() = player.playbackParameters.speed
        set(playbackSpeed) = player.setPlaybackParameters(PlaybackParameters(playbackSpeed))

    val exoPlayer: ExoPlayer
        get() = player

    fun release() {
        player.release()
    }

    fun setPlayerView(playerView: PlayerView?) {
        if (playerView != null) {
            playerView.player = player
        }
    }

    override fun onPlaybackStateChanged(state: Int) {
        if (state == Player.STATE_READY) {
            if (!mDurationSet) {
                mDurationSet = true
                onDurationSet(duration)
            }
        }
        onPlaybackStateChanged.invoke(state)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (player.playbackState == Player.STATE_READY) {
            onPlaybackStateChanged.invoke(Player.STATE_READY)
        }
    }

    fun onDurationSet(cb: OnDurationSet) {
        onDurationSet = cb
    }

    fun onPlaybackStateChanged(cb: OnPlaybackStateChanged) {
        onPlaybackStateChanged = cb
    }
}