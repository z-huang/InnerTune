package com.zionhuang.music.viewmodels

import android.app.Application
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE
import androidx.lifecycle.*
import com.google.android.exoplayer2.ui.PlayerView
import com.zionhuang.music.models.MediaData
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.models.toMediaData
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queue.Queue.Companion.QueueType

class PlaybackViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentSong = MutableLiveData<MediaData?>(null)
    val currentSong: LiveData<MediaData?>
        get() = _currentSong

    private val _currentState = MutableLiveData(STATE_NONE)
    val currentState: LiveData<Int>
        get() = _currentState

    private val mediaMetadataObserver = Observer<MediaMetadataCompat?> { mediaMetadata ->
        if (mediaMetadata != null) {
            val newValue = currentSong.value?.pullMediaMetadata(mediaMetadata)
                    ?: mediaMetadata.toMediaData()
            _currentSong.postValue(newValue)
        }
    }

    private val playbackStateObserver = Observer<PlaybackStateCompat?> { playbackState ->
        if (playbackState != null) {
            _currentState.postValue(playbackState.state)
        }
    }

    private val mediaSessionConnection = MediaSessionConnection(application).apply {
        connect()
        playbackState.observeForever(playbackStateObserver)
        nowPlaying.observeForever(mediaMetadataObserver)
    }

    val mediaController: LiveData<MediaControllerCompat?> =
            mediaSessionConnection.isConnected.map { isConnected ->
                if (isConnected) mediaSessionConnection.mediaController else null
            }

    val transportControls: MediaControllerCompat.TransportControls?
        get() = mediaSessionConnection.transportControls

    fun setPlayerView(playerView: PlayerView) {
        mediaSessionConnection.setPlayerView(playerView)
    }

    fun togglePlayPause() {
        if (currentState.value == PlaybackStateCompat.STATE_PLAYING) {
            mediaSessionConnection.transportControls?.pause()
        } else {
            mediaSessionConnection.transportControls?.play()
        }
    }

    fun playMedia(song: SongParcel, @QueueType queueType: Int) {
        mediaSessionConnection.transportControls?.playFromMediaId(song.id, Bundle().apply {
            putParcelable("song", song)
            putInt("queueType", queueType)
        })
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