package com.zionhuang.music.viewmodels

import android.app.Activity
import android.app.Application
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaControllerCompat.TransportControls
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.R
import com.zionhuang.music.extensions.preferenceLiveData
import com.zionhuang.music.models.PlaybackStateData
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queues.Queue
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.activities.MainActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository(application)
    val transportControls: TransportControls? get() = MediaSessionConnection.transportControls

    val mediaMetadata = MediaSessionConnection.mediaMetadata
    val playbackState = MediaSessionConnection.playbackState.map { playbackState ->
        if (MediaSessionConnection.mediaController != null && playbackState != null) PlaybackStateData.from(MediaSessionConnection.mediaController!!, playbackState)
        else PlaybackStateData()
    }.stateIn(viewModelScope, SharingStarted.Lazily, PlaybackStateData())
    val queueTitle = MediaSessionConnection.queueTitle
    val queueItems = MediaSessionConnection.queueItems

    val playerVolume: Flow<Float> = MediaSessionConnection.isConnected.flatMapLatest {
        MediaSessionConnection.binder?.songPlayer?.playerVolume ?: emptyFlow()
    }
    val currentSong = mediaMetadata.flatMapLatest { mediaMetadata ->
        songRepository.getSongById(mediaMetadata?.getString(METADATA_KEY_MEDIA_ID)).flow
    }
    val currentSongFormat = mediaMetadata.flatMapLatest { mediaMetadata ->
        songRepository.getSongFormat(mediaMetadata?.getString(METADATA_KEY_MEDIA_ID)).getFlow()
    }
    val currentLyrics = mediaMetadata.flatMapLatest { mediaMetadata ->
        songRepository.getLyrics(mediaMetadata?.getString(METADATA_KEY_MEDIA_ID))
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val showLyrics = preferenceLiveData(R.string.pref_show_lyrics, false)

    val position = MutableStateFlow(0L)
    val duration = MutableStateFlow(0L)
    private var lastPositionJob: Job? = null

    val mediaController: MediaControllerCompat? get() = MediaSessionConnection.mediaController

    init {
        viewModelScope.launch {
            MediaSessionConnection.playbackState.collectLatest { state ->
                lastPositionJob?.cancel()
                position.value = state?.position ?: 0
                if (state?.state == STATE_PLAYING) {
                    lastPositionJob = viewModelScope.launch {
                        while (true) {
                            MediaSessionConnection.binder?.songPlayer?.player?.currentPosition?.let {
                                position.value = it
                            }
                            delay(100)
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            MediaSessionConnection.mediaMetadata.collectLatest { metadata ->
                duration.value = metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) ?: -1
            }
        }
    }

    fun togglePlayPause() {
        if (MediaSessionConnection.playbackState.value?.state == STATE_PLAYING) {
            MediaSessionConnection.transportControls?.pause()
        } else {
            MediaSessionConnection.transportControls?.play()
        }
    }

    fun toggleShuffleMode() {
        MediaSessionConnection.mediaController?.let { mediaController ->
            when (mediaController.shuffleMode) {
                SHUFFLE_MODE_NONE -> mediaController.transportControls.setShuffleMode(SHUFFLE_MODE_ALL)
                SHUFFLE_MODE_ALL -> mediaController.transportControls.setShuffleMode(SHUFFLE_MODE_NONE)
            }
        }
    }

    fun toggleRepeatMode() {
        MediaSessionConnection.mediaController?.let { mediaController ->
            when (mediaController.repeatMode) {
                REPEAT_MODE_NONE, REPEAT_MODE_INVALID -> mediaController.transportControls.setRepeatMode(REPEAT_MODE_ALL)
                REPEAT_MODE_ALL, REPEAT_MODE_GROUP -> mediaController.transportControls.setRepeatMode(REPEAT_MODE_ONE)
                REPEAT_MODE_ONE -> mediaController.transportControls.setRepeatMode(REPEAT_MODE_NONE)
            }
        }
    }

    fun playQueue(activity: Activity, queue: Queue) {
        MediaSessionConnection.binder?.songPlayer?.playQueue(queue)
        (activity as? MainActivity)?.showBottomSheet()
    }
}