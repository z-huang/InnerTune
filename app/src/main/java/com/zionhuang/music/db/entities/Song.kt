package com.zionhuang.music.db.entities

import androidx.room.ColumnInfo
import androidx.room.Relation
import java.util.*

data class Song(
        val id: String,
        var title: String?,
        var artistId: Int,
        @Relation(entity = ArtistEntity::class, parentColumn = "artistId", entityColumn = "id", projection = ["name"])
        val artistName: String,
        var channelId: String?,
        @Relation(entity = ChannelEntity::class, parentColumn = "channelId", entityColumn = "id", projection = ["name"])
        val channelName: String,
        var duration: Int, // in seconds
        var liked: Boolean,
        @ColumnInfo(name = "download_state") var downloadState: Int,
        @ColumnInfo(name = "create_date") var createDate: Date,
        @ColumnInfo(name = "modify_date") var modifyDate: Date
)