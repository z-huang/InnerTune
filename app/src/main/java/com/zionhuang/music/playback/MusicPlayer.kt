package com.zionhuang.music.playback

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.ResolvingDataSource
import com.zionhuang.music.youtube.YouTubeExtractor
import com.zionhuang.music.youtube.models.YouTubeStream
import com.zionhuang.music.youtube.models.YtFormat
import kotlinx.coroutines.runBlocking

typealias OnDurationSet = (Long) -> Unit
typealias OnPlaybackStateChanged = (state: Int) -> Unit

/**
 * A wrapper around [SimpleExoPlayer]
 */

class MusicPlayer internal constructor(context: Context) : Player.EventListener {
    companion object {
        private const val TAG = "MusicPlayer"
    }

    private var durationSet = false
    private var onDurationSet: OnDurationSet = {}
    private var onPlaybackStateChanged: OnPlaybackStateChanged = {}

    private var player: SimpleExoPlayer = SimpleExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(
                    ResolvingDataSource.Factory(
                            DefaultDataSourceFactory(context)
                    ) { dataSpec ->
                        val stream: YtFormat? = runBlocking {
                            val res = YouTubeExtractor.getInstance(context).extractStream(dataSpec.uri.host!!)
                            return@runBlocking if (res is YouTubeStream.Success) {
                                res.formats.maxByOrNull {
                                    it.abr ?: 0
                                }!!
                            } else null
                        }
                        return@Factory if (stream != null) {
                            dataSpec.withUri(stream.url!!.toUri())
                        } else {
                            dataSpec
                        }
                    }
            ))
            .build().apply {
                addListener(this@MusicPlayer)
                playWhenReady = true
            }

    val exoPlayer: ExoPlayer get() = player

    val isPlaying: Boolean get() = player.playWhenReady
    val position: Long get() = player.currentPosition
    val duration: Long get() = player.duration
    var volume: Float
        get() = player.volume
        set(value) {
            player.volume = value
        }
    var playbackSpeed: Float
        get() = player.playbackParameters.speed
        set(playbackSpeed) = player.setPlaybackParameters(PlaybackParameters(playbackSpeed))

    fun play() {
        player.playWhenReady = true
    }

    fun pause() {
        player.playWhenReady = false
    }

    fun seekTo(pos: Long) = player.seekTo(pos)

    fun fastForward() = seekTo(minOf(position + 10000, duration))

    fun rewind() = seekTo(maxOf(position - 10000, 0))

    fun stop() = player.stop(true)

    var source: Uri?
        get() = player.currentMediaItem?.playbackProperties?.uri
        set(value) {
            if (value == null) {
                stop()
            } else {
                player.apply {
                    setMediaItem(MediaItem.fromUri(value))
                    prepare()
                    seekTo(0)
                }
            }
            durationSet = false
        }

    fun release() = player.release()

    fun setPlayerView(playerView: PlayerView?) = playerView?.let { it.player = player }

    override fun onPlaybackStateChanged(state: Int) {
        if (state == Player.STATE_READY) {
            if (!durationSet) {
                durationSet = true
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