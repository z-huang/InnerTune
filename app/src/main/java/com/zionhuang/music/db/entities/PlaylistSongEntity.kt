package com.zionhuang.music.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_song",
        indices = [Index("playlistId"), Index("songId")],
        foreignKeys = [
            ForeignKey(
                    entity = PlaylistEntity::class,
                    parentColumns = ["playlistId"],
                    childColumns = ["playlistId"],
                    onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                    entity = SongEntity::class,
                    parentColumns = ["songId"],
                    childColumns = ["songId"],
                    onDelete = ForeignKey.CASCADE)])
data class PlaylistSongEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val playlistId: Int,
        val songId: String,
        val idInPlaylist: Int = 0,
)
