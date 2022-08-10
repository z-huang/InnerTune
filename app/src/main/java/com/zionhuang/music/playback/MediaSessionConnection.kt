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
import com.zionhuang.music.models.MediaSessionQueueData
import com.zionhuang.music.playback.MusicService.MusicBinder
import com.zionhuang.music.utils.livedata.SafeMutableLiveData

object MediaSessionConnection {
    var mediaController: MediaControllerCompat? = null
        private set
    val transportControls: MediaControllerCompat.TransportControls? get() = mediaController?.transportControls
    private val mediaControllerCallback = MediaControllerCallback()
    private var serviceConnection: ServiceConnection? = null

    val isConnected = SafeMutableLiveData(false)
    val playbackState = MutableLiveData<PlaybackStateCompat?>(null)
    val nowPlaying = MutableLiveData<MediaMetadataCompat?>(null)
    val queueData = MutableLiveData(MediaSessionQueueData())

    val binderLiveData = MutableLiveData<MusicBinder>()

    private var _binder: MusicBinder? = null
    val binder: MusicBinder?
        get() = _binder

    fun connect(context: Context) {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
                if (iBinder !is MusicBinder) return
                _binder = iBinder
                binderLiveData.postValue(iBinder)
                try {
                    mediaController = MediaControllerCompat(context, iBinder.sessionToken).apply {
                        registerCallback(mediaControllerCallback)
                    }
                    isConnected.postValue(true)
                } catch (e: RemoteException) {
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                _binder = null
                binderLiveData.postValue(null)
                mediaController?.unregisterCallback(mediaControllerCallback)
                isConnected.postValue(false)
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
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) = playbackState.postValue(state)
        override fun onMetadataChanged(metadata: MediaMetadataCompat) = nowPlaying.postValue(metadata)
        override fun onQueueChanged(queue: List<MediaSessionCompat.QueueItem>) = queueData.postValue(queueData.value!!.update(queue))
    }
}