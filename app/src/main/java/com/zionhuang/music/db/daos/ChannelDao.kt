package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.*
import com.zionhuang.music.db.entities.ChannelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channel")
    fun getAllChannelsAsPagingSource(): PagingSource<Int, ChannelEntity>

    @Query("SELECT * FROM channel WHERE id = :channelId")
    fun getChannelById(channelId: String): ChannelEntity?

    // TODO nullable
    @Query("SELECT * FROM channel WHERE id = :channelId")
    fun getChannelFlowById(channelId: String): Flow<ChannelEntity>

    @Query("SELECT * FROM channel WHERE name = :name")
    fun getChannelByName(name: String): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(channel: ChannelEntity)

    @Query("SELECT EXISTS (SELECT 1 FROM channel WHERE id=:channelId)")
    suspend fun contains(channelId: String): Boolean

    @Delete
    suspend fun delete(channel: ChannelEntity)
}