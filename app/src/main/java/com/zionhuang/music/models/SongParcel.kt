package com.zionhuang.music.models

import android.os.Parcelable
import com.google.api.services.youtube.model.SearchResult
import com.google.api.services.youtube.model.Video
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.maxResUrl
import com.zionhuang.music.youtube.extractors.YouTubeStreamExtractor
import com.zionhuang.music.youtube.models.YouTubeStream
import kotlinx.parcelize.Parcelize
import org.schabi.newpipe.extractor.stream.StreamInfoItem

@Parcelize
data class SongParcel(
        val id: String,
        val title: String?,
        val artist: String?,
        val channelId: String,
        val channelName: String,
        val artworkUrl: String?,
) : Parcelable {
    companion object {
        fun fromSong(song: Song): SongParcel =
                SongParcel(song.songId, song.title, song.artistName, song.channelId, song.channelName, null)

        fun fromStream(stream: YouTubeStream.Success): SongParcel =
                SongParcel(stream.id, stream.title, null, stream.channelId, stream.channelTitle, stream.thumbnailUrl)

        fun fromVideo(video: Video): SongParcel =
                SongParcel(video.id, video.snippet.title, null, video.snippet.channelId, video.snippet.channelTitle, video.snippet.thumbnails.maxResUrl)

        fun fromSearchResult(item: SearchResult): SongParcel {
            require("youtube#video" == item.id.kind) { "Can't convert a " + item.id.kind + " item to SongParcel." }
            return SongParcel(item.id.videoId, item.snippet.title, null, item.snippet.channelId, item.snippet.channelTitle, item.snippet.thumbnails.maxResUrl)
        }

        fun fromStreamInfoItem(item: StreamInfoItem): SongParcel =
                SongParcel(YouTubeStreamExtractor.extractId(item.url)!!, item.name, "", "", "", item.thumbnailUrl)
    }
}