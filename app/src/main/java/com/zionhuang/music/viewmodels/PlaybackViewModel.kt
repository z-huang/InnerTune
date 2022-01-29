package com.zionhuang.music.viewmodels

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.lifecycle.*
import com.google.android.exoplayer2.ui.PlayerView
import com.zionhuang.music.R
import com.zionhuang.music.extensions.preference
import com.zionhuang.music.models.MediaData
import com.zionhuang.music.models.MediaSessionQueueData
import com.zionhuang.music.models.PlaybackStateData
import com.zionhuang.music.models.toMediaData
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.utils.livedata.SafeLiveData
import com.zionhuang.music.utils.livedata.SafeMutableLiveData

class PlaybackViewModel(application: Application) : AndroidViewModel(application) {
    private val _mediaData = MutableLiveData<MediaData?>(null)
    val mediaData: LiveData<MediaData?> get() = _mediaData

    private val _playbackState = SafeMutableLiveData(PlaybackStateData())
    val playbackState: SafeLiveData<PlaybackStateData> get() = _playbackState

    private val _queueData = SafeMutableLiveData(MediaSessionQueueData())
    val queueData: SafeLiveData<MediaSessionQueueData> get() = _queueData

    private val mediaMetadataObserver = Observer<MediaMetadataCompat?> { mediaMetadata ->
        if (mediaMetadata != null) {
            _mediaData.postValue(mediaData.value?.pullMediaMetadata(mediaMetadata)
                    ?: mediaMetadata.toMediaData())
        }
    }

    private val playbackStateObserver = Observer<PlaybackStateCompat?> { playbackState ->
        if (playbackState != null) {
            _playbackState.postValue(_playbackState.value.pullPlaybackState(playbackState, mediaController?.value))
        }
    }

    private val queueDataObserver = Observer<MediaSessionQueueData> { queueData ->
        _queueData.postValue(queueData)
    }

    private val mediaSessionConnection = MediaSessionConnection.apply {
        connect(application)
        playbackState.observeForever(playbackStateObserver)
        nowPlaying.observeForever(mediaMetadataObserver)
        queueData.observeForever(queueDataObserver)
    }

    val mediaController: LiveData<MediaControllerCompat?> = mediaSessionConnection.isConnected.map { isConnected ->
        if (isConnected) mediaSessionConnection.mediaController else null
    }

    val transportControls: MediaControllerCompat.TransportControls? get() = mediaSessionConnection.transportControls

    val expandOnPlay by preference(R.string.pref_expand_on_play, false)

    fun setPlayerView(playerView: PlayerView) {
        mediaSessionConnection.playerView = playerView
    }

    fun togglePlayPause() {
        if (playbackState.value.state == STATE_PLAYING) {
            mediaSessionConnection.transportControls?.pause()
        } else {
            mediaSessionConnection.transportControls?.play()
        }
    }

    fun toggleShuffleMode() {
        mediaSessionConnection.mediaController?.let { mediaController ->
            when (mediaController.shuffleMode) {
                SHUFFLE_MODE_NONE -> mediaController.transportControls.setShuffleMode(SHUFFLE_MODE_ALL)
                SHUFFLE_MODE_ALL -> mediaController.transportControls.setShuffleMode(SHUFFLE_MODE_NONE)
            }
        }
    }

    fun toggleRepeatMode() {
        mediaSessionConnection.mediaController?.let { mediaController ->
            when (mediaController.repeatMode) {
                REPEAT_MODE_NONE, REPEAT_MODE_INVALID -> mediaController.transportControls.setRepeatMode(REPEAT_MODE_ALL)
                REPEAT_MODE_ALL, REPEAT_MODE_GROUP -> mediaController.transportControls.setRepeatMode(REPEAT_MODE_ONE)
                REPEAT_MODE_ONE -> mediaController.transportControls.setRepeatMode(REPEAT_MODE_NONE)
            }
        }
    }

    fun playMedia(activity: Activity, mediaId: String, extras: Bundle) {
        transportControls?.playFromMediaId(mediaId, extras)
        if (expandOnPlay) {
            (activity as? MainActivity)?.expandBottomSheet()
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaSessionConnection.apply {
            playbackState.removeObserver(playbackStateObserver)
            nowPlaying.removeObserver(mediaMetadataObserver)
            queueData.removeObserver(queueDataObserver)
            disconnect(getApplication())
        }
    }
}