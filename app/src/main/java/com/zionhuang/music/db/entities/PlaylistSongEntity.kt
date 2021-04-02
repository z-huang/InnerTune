package com.zionhuang.music.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_song",
        indices = [
            Index(value = ["playlistId"]),
            Index(value = ["songId"])],
        foreignKeys = [
            ForeignKey(
                    entity = PlaylistEntity::class,
                    parentColumns = ["id"],
                    childColumns = ["playlistId"]),
            ForeignKey(
                    entity = SongEntity::class,
                    parentColumns = ["id"],
                    childColumns = ["songId"])])
data class PlaylistSongEntity(
        @PrimaryKey(autoGenerate = true) val id: Int? = 0,
        val playlistId: Int,
        val songId: Int,
)
