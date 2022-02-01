package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem
import com.zionhuang.music.constants.MediaConstants.EXTRA_PLAYLIST_ID
import com.zionhuang.music.constants.MediaConstants.QUEUE_PLAYLIST
import com.zionhuang.music.extensions.getApplication
import com.zionhuang.music.extensions.toMediaItems
import com.zionhuang.music.models.QueueData
import com.zionhuang.music.repos.SongRepository

class PlaylistQueue(
    override val items: List<MediaItem>,
) : Queue {
    override fun hasNextPage() = false
    override suspend fun nextPage() = emptyList<MediaItem>()

    companion object {
        const val TYPE = QUEUE_PLAYLIST
        val fromParcel: suspend (QueueData) -> Queue = { it ->
            PlaylistQueue(SongRepository.getPlaylistSongs(it.extras.getInt(EXTRA_PLAYLIST_ID), it.sortInfo!!).getList().toMediaItems(getApplication()))
        }
    }
}