package com.zionhuang.music.db.daos

import androidx.room.*
import com.zionhuang.music.db.entities.LyricsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricsDao {
    @Query("SELECT * FROM lyrics WHERE id = :id")
    suspend fun getLyrics(id: String?): LyricsEntity?

    @Query("SELECT * FROM lyrics WHERE id = :id")
    fun getLyricsAsFlow(id: String?): Flow<LyricsEntity?>

    @Query("SELECT EXISTS (SELECT 1 FROM lyrics WHERE id = :id)")
    suspend fun hasLyrics(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(lyrics: LyricsEntity): Long

    @Update
    suspend fun update(lyrics: LyricsEntity)

    suspend fun upsert(lyrics: LyricsEntity) {
        if (insert(lyrics) == -1L) {
            update(lyrics)
        }
    }
}