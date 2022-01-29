package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem
import com.zionhuang.music.constants.MediaConstants.EXTRA_ARTIST_ID
import com.zionhuang.music.constants.MediaConstants.QUEUE_ARTIST
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.extensions.getApplication
import com.zionhuang.music.extensions.toMediaItems
import com.zionhuang.music.models.QueueData

class ArtistQueue(
    override val items: List<MediaItem>,
) : Queue {
    override fun hasNextPage() = false
    override suspend fun nextPage() = emptyList<MediaItem>()

    companion object {
        const val TYPE = QUEUE_ARTIST
        val fromParcel: suspend (QueueData) -> Queue = {
            ArtistQueue(SongRepository().getArtistSongsList(
                it.extras.getInt(EXTRA_ARTIST_ID),
                it.sortInfo!!
            ).toMediaItems(getApplication()))
        }
    }
}