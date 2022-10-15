package com.zionhuang.music.db.daos

import androidx.room.*
import com.zionhuang.music.db.entities.FormatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FormatDao {
    @Query("SELECT * FROM format WHERE id = :id")
    suspend fun getSongFormat(id: String?): FormatEntity?

    @Query("SELECT * FROM format WHERE id = :id")
    fun getSongFormatAsFlow(id: String?): Flow<FormatEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(format: FormatEntity): Long

    @Update
    suspend fun update(format: FormatEntity)

    suspend fun upsert(format: FormatEntity) {
        if (insert(format) == -1L) {
            update(format)
        }
    }
}