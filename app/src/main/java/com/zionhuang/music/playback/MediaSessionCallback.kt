package com.zionhuang.music.playback

import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.playback.MediaSessionConstants.ACTION_ADD_TO_LIBRARY
import com.zionhuang.music.playback.MediaSessionConstants.ACTION_TOGGLE_LIKE
import com.zionhuang.music.playback.queue.Queue

class MediaSessionCallback internal constructor(private val mediaSession: MediaSessionCompat, private val songPlayer: SongPlayer) : MediaSessionCompat.Callback() {
    override fun onPlayFromMediaId(mediaId: String, extras: Bundle) {
        val songParcel: SongParcel? = extras.getParcelable("song")
        val queueType = extras.getInt("queueType")
        require(queueType != Queue.QUEUE_NONE) { "Unidentified queue type" }
        songPlayer.setQueue(queueType, mediaId)
        songParcel?.let {
            songPlayer.updateSongMeta(it.id, it)
        }
        songPlayer.playSong()
    }

    override fun onCustomAction(action: String, extras: Bundle) {
        when (action) {
            ACTION_ADD_TO_LIBRARY -> songPlayer.addToLibrary()
            ACTION_TOGGLE_LIKE -> songPlayer.toggleLike()
        }
    }

    override fun onPlay() = songPlayer.play()
    override fun onPause() = songPlayer.pause()
    override fun onSeekTo(pos: Long) = songPlayer.seekTo(pos)
    override fun onStop() = songPlayer.stop()
    override fun onSkipToNext() = songPlayer.playNext()
    override fun onSkipToPrevious() = songPlayer.playPrevious()
}