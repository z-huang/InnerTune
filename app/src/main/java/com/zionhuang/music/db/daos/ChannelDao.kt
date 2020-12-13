package com.zionhuang.music.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zionhuang.music.db.entities.ChannelEntity

@Dao
interface ChannelDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(channel: ChannelEntity): Long

    @Query("SELECT EXISTS (SELECT 1 FROM channel WHERE id=:channelId)")
    suspend fun contains(channelId: String): Boolean
}