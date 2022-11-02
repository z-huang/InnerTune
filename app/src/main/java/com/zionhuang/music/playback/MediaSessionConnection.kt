package com.zionhuang.music.playback

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat.QueueItem
import android.support.v4.media.session.PlaybackStateCompat
import com.zionhuang.music.playback.MusicService.MusicBinder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object MediaSessionConnection {
    var mediaController: MediaControllerCompat? = null
        private set
    val transportControls: MediaControllerCompat.TransportControls? get() = mediaController?.transportControls
    private val mediaControllerCallback = MediaControllerCallback()

    private val _isConnected = MutableStateFlow(false)
    private val _playbackState = MutableStateFlow<PlaybackStateCompat?>(null)
    private val _mediaMetadata = MutableStateFlow<MediaMetadataCompat?>(null)
    private val _queueTitle = MutableStateFlow<String?>(null)
    private val _queueItems = MutableStateFlow<List<QueueItem>>(emptyList())

    val isConnected: StateFlow<Boolean> = _isConnected
    val playbackState: StateFlow<PlaybackStateCompat?> = _playbackState
    val mediaMetadata: StateFlow<MediaMetadataCompat?> = _mediaMetadata
    val queueTitle: StateFlow<String?> = _queueTitle
    val queueItems: StateFlow<List<QueueItem>> = _queueItems

    private var _binder: MusicBinder? = null
    val binder: MusicBinder? get() = _binder

    private var serviceConnection: ServiceConnection? = null

    fun connect(context: Context) {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
                if (iBinder !is MusicBinder) return
                _binder = iBinder
                try {
                    mediaController = MediaControllerCompat(context, iBinder.sessionToken).apply {
                        registerCallback(mediaControllerCallback)
                    }
                    _isConnected.value = true
                } catch (_: RemoteException) {
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                _binder = null
                mediaController?.unregisterCallback(mediaControllerCallback)
                _isConnected.value = false
            }
        }.also {
            val intent = Intent(context, MusicService::class.java)
            context.bindService(intent, it, Context.BIND_AUTO_CREATE)
        }
    }

    fun disconnect(context: Context) {
        if (serviceConnection != null) {
            context.unbindService(serviceConnection!!)
        }
    }

    private class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            _playbackState.value = state
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _mediaMetadata.value = metadata
            // force update playback state
            mediaController?.let {
                _playbackState.value = it.playbackState
            }
        }

        override fun onQueueChanged(queue: List<QueueItem>) {
            _queueItems.value = queue
        }

        override fun onQueueTitleChanged(title: CharSequence?) {
            _queueTitle.value = title?.toString()
        }
    }
}