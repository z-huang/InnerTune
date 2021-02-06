package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.zionhuang.music.constants.ORDER_ARTIST
import com.zionhuang.music.constants.ORDER_CREATE_DATE
import com.zionhuang.music.constants.ORDER_NAME
import com.zionhuang.music.constants.SongSortType
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.ChannelEntity
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.extensions.toSQLiteQuery
import com.zionhuang.music.playback.queue.AllSongsQueue
import com.zionhuang.music.ui.fragments.songs.ChannelSongsFragment
import kotlinx.coroutines.flow.Flow

const val QUERY_ALL_SONG = "SELECT * FROM song ORDER BY %s %s"

@Dao
interface SongDao {
    /**
     * Methods for the UI
     */

    /**
     * All Songs [PagingSource] with order [ORDER_CREATE_DATE], [ORDER_NAME], and [ORDER_ARTIST]
     */
    fun getAllSongsAsPagingSource(@SongSortType order: Int, descending: Boolean): PagingSource<Int, Song> =
            getAllSongsAsPagingSource(QUERY_ALL_SONG.format(
                    when (order) {
                        ORDER_CREATE_DATE -> "create_date"
                        ORDER_NAME -> "title"
                        ORDER_ARTIST -> "artistId"
                        else -> throw IllegalArgumentException("Unexpected song sort type.")
                    },
                    if (descending) "DESC" else "ASC"
            ).toSQLiteQuery())

    @Transaction
    @RawQuery(observedEntities = [SongEntity::class, ArtistEntity::class, ChannelEntity::class])
    fun getAllSongsAsPagingSource(query: SupportSQLiteQuery): PagingSource<Int, Song>

    /**
     * Artist Songs [PagingSource]
     */
    @Transaction
    @Query("SELECT * FROM song WHERE artistId = :artistId")
    fun getArtistSongsAsPagingSource(artistId: Int): PagingSource<Int, Song>

    /**
     * Channel Songs [PagingSource]
     */
    @Transaction
    @Query("SELECT * FROM song WHERE channelId = :channelId")
    fun getChannelSongsAsPagingSource(channelId: String): PagingSource<Int, Song>

    /**
     * Methods for [ChannelSongsFragment]
     */
    @Query("SELECT COUNT(id) FROM song WHERE channelId = :channelId")
    fun channelSongsCount(channelId: String): Flow<Int>

    @Query("SELECT SUM(duration) FROM song WHERE channelId = :channelId")
    fun channelSongsDuration(channelId: String): Flow<Long?>

    /**
     * All Songs [MutableList] with order [ORDER_CREATE_DATE], [ORDER_NAME], and [ORDER_ARTIST]
     * Used by [AllSongsQueue]
     */
    @Transaction
    @Query("SELECT * FROM song ORDER BY create_date DESC")
    fun getAllSongsByCreateDateAsMutableList(): MutableList<Song>

    @Transaction
    @Query("SELECT * FROM song ORDER BY title DESC")
    fun getAllSongsByNameAsMutableList(): MutableList<Song>

    @Transaction
    @Query("SELECT * FROM song ORDER BY artistId DESC")
    fun getAllSongsByArtistAsMutableList(): MutableList<Song>

    /**
     * Internal methods
     */
    @Query("SELECT * FROM song WHERE id = :songId")
    suspend fun getSongEntityById(songId: String): SongEntity?

    @Transaction
    @Query("SELECT * FROM song WHERE id = :songId")
    suspend fun getSongById(songId: String): Song?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg songs: SongEntity)

    @Update
    suspend fun update(vararg songs: SongEntity)

    @Delete
    suspend fun delete(vararg songs: SongEntity)

    @Query("DELETE FROM song WHERE id = :songId")
    suspend fun delete(songId: String)

    @Query("SELECT EXISTS (SELECT 1 FROM song WHERE id=:songId)")
    suspend fun contains(songId: String): Boolean
}