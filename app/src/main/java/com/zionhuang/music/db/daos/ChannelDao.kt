package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zionhuang.music.db.entities.ChannelEntity

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channel")
    fun getAllChannelsAsPagingSource(): PagingSource<Int, ChannelEntity>

    @Query("SELECT * FROM channel WHERE id = :channelId")
    fun getChannelById(channelId: String): ChannelEntity?

    @Query("SELECT * FROM channel WHERE name = :name")
    fun getChannelByName(name: String): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(channel: ChannelEntity): Long

    @Query("SELECT EXISTS (SELECT 1 FROM channel WHERE id=:channelId)")
    suspend fun contains(channelId: String): Boolean
}