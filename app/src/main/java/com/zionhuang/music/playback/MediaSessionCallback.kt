package com.zionhuang.music.playback

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.os.bundleOf
import com.zionhuang.music.constants.MediaConstants.QUEUE_TYPE
import com.zionhuang.music.constants.MediaConstants.SONG_ID
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_ADD_TO_LIBRARY
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_DOWNLOAD
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_TOGGLE_LIKE
import com.zionhuang.music.playback.queue.Queue.Companion.QUEUE_SINGLE
import com.zionhuang.music.youtube.YouTubeExtractor

class MediaSessionCallback internal constructor(
        context: Context,
        val mediaSession: MediaSessionCompat,
        private val songPlayer: SongPlayer,
) : MediaSessionCompat.Callback() {
    private val youTubeExtractor = YouTubeExtractor.getInstance(context)

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle) {
        if (extras.isEmpty) {
            extras.apply {
                putInt(QUEUE_TYPE, QUEUE_SINGLE)
                putString(SONG_ID, mediaId)
            }
        }
        songPlayer.setQueue(extras)
    }

    override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
        val id = youTubeExtractor.extractId(uri.toString())
        if (id == null) {
            Log.d(TAG, "Can't extract video id from the url.")
            return
        }
        songPlayer.setQueue(bundleOf(
                QUEUE_TYPE to QUEUE_SINGLE,
                SONG_ID to id
        ))
    }

    override fun onPlayFromSearch(query: String, extras: Bundle?) {

    }

    override fun onCustomAction(action: String, extras: Bundle) {
        when (action) {
            ACTION_ADD_TO_LIBRARY -> songPlayer.addToLibrary()
            ACTION_TOGGLE_LIKE -> songPlayer.toggleLike()
            ACTION_DOWNLOAD -> songPlayer.downloadCurrentSong()
        }
    }

    override fun onPlay() = songPlayer.play()
    override fun onPause() = songPlayer.pause()
    override fun onSeekTo(pos: Long) = songPlayer.seekTo(pos)
    override fun onStop() = songPlayer.stop()
    override fun onSkipToNext() = songPlayer.playNext()
    override fun onSkipToPrevious() = songPlayer.playPrevious()
    override fun onFastForward() = songPlayer.fastForward()
    override fun onRewind() = songPlayer.rewind()

    override fun onSetRepeatMode(repeatMode: Int) {
    }

    companion object {
        const val TAG = "MediaSessionCallback"
    }
}