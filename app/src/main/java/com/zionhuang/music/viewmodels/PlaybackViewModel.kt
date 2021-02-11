package com.zionhuang.music.viewmodels

import android.app.Application
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import com.google.android.exoplayer2.ui.PlayerView
import com.zionhuang.music.constants.MediaConstants.QUEUE_TYPE
import com.zionhuang.music.constants.MediaConstants.SONG_ID
import com.zionhuang.music.constants.MediaConstants.SONG_PARCEL
import com.zionhuang.music.models.MediaData
import com.zionhuang.music.models.PlaybackStateData
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.models.toMediaData
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queue.Queue.Companion.QueueType
import com.zionhuang.music.utils.livedata.SafeLiveData
import com.zionhuang.music.utils.livedata.SafeMutableLiveData

class PlaybackViewModel(application: Application) : AndroidViewModel(application) {
    private val _mediaData = MutableLiveData<MediaData?>(null)
    val mediaData: LiveData<MediaData?> get() = _mediaData

    private val _playbackState = SafeMutableLiveData(PlaybackStateData())
    val playbackState: SafeLiveData<PlaybackStateData> get() = _playbackState

    private val mediaMetadataObserver = Observer<MediaMetadataCompat?> { mediaMetadata ->
        if (mediaMetadata != null) {
            _mediaData.postValue(mediaData.value?.pullMediaMetadata(mediaMetadata)
                    ?: mediaMetadata.toMediaData())
        }
    }

    private val playbackStateObserver = Observer<PlaybackStateCompat?> { playbackState ->
        if (playbackState != null) {
            _playbackState.postValue(_playbackState.value.pullPlaybackState(playbackState))
        }
    }

    private val mediaSessionConnection = MediaSessionConnection(application).apply {
        connect()
        playbackState.observeForever(playbackStateObserver)
        nowPlaying.observeForever(mediaMetadataObserver)
    }

    val mediaController: LiveData<MediaControllerCompat?> = mediaSessionConnection.isConnected.map { isConnected ->
        if (isConnected) mediaSessionConnection.mediaController else null
    }

    val transportControls: MediaControllerCompat.TransportControls? get() = mediaSessionConnection.transportControls

    fun setPlayerView(playerView: PlayerView) = mediaSessionConnection.setPlayerView(playerView)

    fun togglePlayPause() {
        if (playbackState.value.state == PlaybackStateCompat.STATE_PLAYING) {
            mediaSessionConnection.transportControls?.pause()
        } else {
            mediaSessionConnection.transportControls?.play()
        }
    }

    fun playMedia(@QueueType queueType: Int, song: SongParcel) {
        mediaSessionConnection.transportControls?.playFromMediaId(song.id, bundleOf(
                QUEUE_TYPE to queueType,
                SONG_ID to song.id,
                SONG_PARCEL to song
        ))
    }

    override fun onCleared() {
        super.onCleared()
        mediaSessionConnection.apply {
            playbackState.removeObserver(playbackStateObserver)
            nowPlaying.removeObserver(mediaMetadataObserver)
            disconnect()
        }
    }
}