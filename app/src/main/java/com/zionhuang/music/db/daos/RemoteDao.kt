package com.zionhuang.music.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zionhuang.music.db.entities.RemoteKey
import com.zionhuang.music.db.entities.SearchEntity

@Dao
interface RemoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(remoteKey: RemoteKey)

    @Query("SELECT * FROM remote_keys WHERE `query` = :query")
    suspend fun getRemoteKey(query: String): RemoteKey

    @Query("SELECT * FROM search_items WHERE queryId = :queryId")
    suspend fun getSearchEntities(queryId: Long): List<SearchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchEntities(list: List<SearchEntity>)
}