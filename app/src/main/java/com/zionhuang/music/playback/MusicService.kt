package com.zionhuang.music.playback

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ui.PlayerView

class MusicService : LifecycleService() {
    companion object {
        private const val TAG = "MusicService"
    }

    private val binder = LocalBinder()
    private lateinit var songPlayer: SongPlayer

    override fun onCreate() {
        super.onCreate()
        songPlayer = SongPlayer(this, lifecycleScope)
        songPlayer.onNotificationPosted { notificationId, notification, ongoing ->
            if (ongoing) {
                startForeground(notificationId, notification)
            } else {
                stopForeground(false)
            }
        }
    }

    override fun onDestroy() {
        songPlayer.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    val sessionToken: MediaSessionCompat.Token
        get() = songPlayer.mediaSession.sessionToken

    fun setPlayerView(playerView: PlayerView?) {
        songPlayer.setPlayerView(playerView)
    }

    internal inner class LocalBinder : Binder() {
        val service: MusicService
            get() = this@MusicService
    }
}