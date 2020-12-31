package com.zionhuang.music.playback

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_ADD_TO_LIBRARY
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_DOWNLOAD
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_TOGGLE_LIKE
import com.zionhuang.music.playback.queue.Queue.Companion.QUEUE_NONE
import com.zionhuang.music.playback.queue.Queue.Companion.QUEUE_SINGLE
import com.zionhuang.music.youtube.YouTubeExtractor
import com.zionhuang.music.youtube.models.YouTubeSearch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MediaSessionCallback internal constructor(
        context: Context,
        private val songPlayer: SongPlayer,
        private val scope: CoroutineScope,
) : MediaSessionCompat.Callback() {
    private val youTubeExtractor = YouTubeExtractor.getInstance(context)

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle) {
        val songParcel: SongParcel? = extras.getParcelable("song")
        val queueType = extras.getInt("queueType").let { if (it != QUEUE_NONE) it else QUEUE_SINGLE }
        songPlayer.setQueue(queueType, mediaId)
        songParcel?.let {
            songPlayer.updateSongMeta(it.id, it)
        }
        songPlayer.playSong()
    }

    override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
        val id = youTubeExtractor.extractId(uri.toString())
        if (id == null) {
            Log.d(TAG, "Can't extract video id from the url.")
            return
        }
        songPlayer.setQueue(QUEUE_SINGLE, id)
        songPlayer.playSong()
    }

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
        if (query == null) return
        scope.launch {
            when (val res = youTubeExtractor.search(query)) {
                is YouTubeSearch.Success -> {
                    songPlayer.setQueue(QUEUE_SINGLE, res.items[0].id)
                    songPlayer.playSong()
                }
                is YouTubeSearch.Error -> {
                    Log.d(TAG, "Failed to search: $query")
                }
            }
        }
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

    companion object {
        const val TAG = "MediaSessionCallback"
    }
}