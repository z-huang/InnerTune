package com.zionhuang.music.playback

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ui.PlayerView
import com.zionhuang.music.models.QueueData
import com.zionhuang.music.playback.MusicService.LocalBinder

class MediaSessionConnection(private val context: Context) {
    var mediaController: MediaControllerCompat? = null
        private set
    val transportControls: MediaControllerCompat.TransportControls?
        get() = mediaController?.transportControls
    private val mediaControllerCallback = MediaControllerCallback()
    private var serviceConnection: MediaServiceConnection? = null

    val isConnected = MutableLiveData(false)
    val playbackState = MutableLiveData<PlaybackStateCompat?>(null)
    val nowPlaying = MutableLiveData<MediaMetadataCompat?>(null)
    val queueData = MutableLiveData(QueueData())

    private var playerView: PlayerView? = null

    fun connect() {
        serviceConnection = MediaServiceConnection().also {
            val intent = Intent(context, MusicService::class.java)
            context.bindService(intent, it, Context.BIND_AUTO_CREATE)
        }
    }

    fun disconnect() {
        if (serviceConnection != null) {
            context.unbindService(serviceConnection!!)
        }
    }

    fun setPlayerView(playerView: PlayerView?) {
        this.playerView = playerView
    }

    private inner class MediaServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
            val service = (iBinder as LocalBinder).service.apply {
                setPlayerView(playerView)
            }
            try {
                mediaController = MediaControllerCompat(context, service.sessionToken).apply {
                    registerCallback(mediaControllerCallback)
                }
                isConnected.postValue(true)
            } catch (e: RemoteException) {
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mediaController?.unregisterCallback(mediaControllerCallback)
            isConnected.postValue(false)
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) = playbackState.postValue(state)
        override fun onMetadataChanged(metadata: MediaMetadataCompat) = nowPlaying.postValue(metadata)
        override fun onQueueChanged(queue: List<MediaSessionCompat.QueueItem>) = queueData.postValue(queueData.value!!.update(queue))
    }
}