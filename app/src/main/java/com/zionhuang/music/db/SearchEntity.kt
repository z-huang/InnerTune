package com.zionhuang.music.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.api.services.youtube.model.SearchResult
import java.util.*

@Entity(tableName = "search_items")
data class SearchEntity(
        @PrimaryKey val queryId: Long = -1,
        val id: String,
        val title: String,
        val channelTitle: String,
        val publishedDate: Date
)

fun SearchResult.toSearchEntity(queryId: Long): SearchEntity = SearchEntity(
        queryId,
        when (this.id.kind) {
            "youtube#video" -> this.id.videoId
            "youtube#channel" -> this.id.channelId
            "youtube#playlist" -> this.id.playlistId
            else -> throw IllegalArgumentException("Unexpected value in id.kind")
        },
        this.snippet.title,
        this.snippet.channelTitle,
        Date(this.snippet.publishedAt.value)
)