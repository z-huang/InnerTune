package com.zionhuang.music.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.zionhuang.music.db.entities.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download")
    fun getAllDownloadEntities(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM download WHERE id = :downloadId")
    suspend fun getDownloadEntity(downloadId: Long): DownloadEntity?

    @Insert
    suspend fun insert(entity: DownloadEntity)

    @Query("DELETE FROM download WHERE id = :downloadId")
    suspend fun delete(downloadId: Long)
}