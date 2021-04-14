package com.zionhuang.music.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "artist",
        indices = [Index(value = ["id"], unique = true)])
data class ArtistEntity(
        @PrimaryKey(autoGenerate = true) val id: Int? = null,
        val name: String,
) : Parcelable {
    override fun toString(): String = name
}
