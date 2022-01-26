package com.zionhuang.music.models

import android.os.Bundle
import android.os.Parcelable
import com.zionhuang.music.playback.queues.*
import kotlinx.parcelize.Parcelize

@Parcelize
data class QueueData(
    val type: Int,
    val queueId: String = "",
    val extras: Bundle = Bundle(),
    val songId: String = "",
) : Parcelable {
    suspend fun toQueue() = if (type in map) map[type]!!.invoke(this) else throw IllegalArgumentException("Unknown queue type")

    companion object {
        val map = mapOf(
            YouTubeSingleSongQueue.TYPE to YouTubeSingleSongQueue.fromParcel,
            AllSongQueue.TYPE to AllSongQueue.fromParcel,
            ArtistQueue.TYPE to ArtistQueue.fromParcel,
            PlaylistQueue.TYPE to PlaylistQueue.fromParcel,
            YouTubeSearchQueue.TYPE to YouTubeSearchQueue.fromParcel,
            YouTubePlaylistQueue.TYPE to YouTubePlaylistQueue.fromParcel,
            YouTubeChannelQueue.TYPE to YouTubeChannelQueue.fromParcel
        )
    }
}
