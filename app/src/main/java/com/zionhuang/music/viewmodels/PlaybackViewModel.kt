package com.zionhuang.music.viewmodels

import android.app.Activity
import android.app.Application
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import com.zionhuang.music.models.MediaSessionQueueData
import com.zionhuang.music.models.PlaybackStateData
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queues.Queue
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.utils.livedata.SafeLiveData
import com.zionhuang.music.utils.livedata.SafeMutableLiveData

class PlaybackViewModel(application: Application) : AndroidViewModel(application) {
    val mediaMetadata: LiveData<MediaMetadataCompat?> get() = MediaSessionConnection.nowPlaying

    private val _playbackState = SafeMutableLiveData(PlaybackStateData())
    val playbackState: SafeLiveData<PlaybackStateData> get() = _playbackState

    val queueData: LiveData<MediaSessionQueueData> get() = MediaSessionConnection.queueData

    private val playbackStateObserver = Observer<PlaybackStateCompat?> { playbackState ->
        if (playbackState != null) {
            _playbackState.postValue(_playbackState.value.pullPlaybackState(playbackState, mediaController.value))
        }
    }

    private val mediaSessionConnection = MediaSessionConnection.apply {
        if (!isConnected.value) connect(application)
        playbackState.observeForever(playbackStateObserver)
    }

    val mediaController: LiveData<MediaControllerCompat?> = mediaSessionConnection.isConnected.map { isConnected ->
        if (isConnected) mediaSessionConnection.mediaController else null
    }

    val transportControls: MediaControllerCompat.TransportControls? get() = mediaSessionConnection.transportControls

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

    fun playQueue(activity: Activity, queue: Queue) {
        mediaSessionConnection.binder?.playQueue(queue)
        (activity as? MainActivity)?.showBottomSheet()
    }

    override fun onCleared() {
        super.onCleared()
        mediaSessionConnection.apply {
            playbackState.removeObserver(playbackStateObserver)
            disconnect(getApplication())
        }
    }
}