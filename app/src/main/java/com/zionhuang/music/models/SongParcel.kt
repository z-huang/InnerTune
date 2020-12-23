package com.zionhuang.music.models

import android.os.Parcelable
import com.google.api.services.youtube.model.SearchResult
import com.google.api.services.youtube.model.Video
import com.zionhuang.music.db.entities.Song
import kotlinx.parcelize.Parcelize

@Parcelize
class SongParcel(val id: String, val title: String?, val artist: String?) : Parcelable {
    companion object {
        fun fromSong(song: Song): SongParcel {
            return SongParcel(song.id, song.title, song.artistName)
        }

        fun fromVideo(video: Video): SongParcel {
            return SongParcel(video.id, video.snippet.title, video.snippet.channelTitle)
        }

        fun fromSearchResult(item: SearchResult): SongParcel {
            require("youtube#video" == item.id.kind) { "Can't convert a " + item.id.kind + " item to SongParcel." }
            return SongParcel(item.id.videoId, item.snippet.title, item.snippet.channelTitle)
        }
    }
}