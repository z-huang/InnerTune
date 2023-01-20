package com.zionhuang.music.db

import androidx.room.*
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.pages.AlbumPage
import com.zionhuang.innertube.pages.ArtistPage
import com.zionhuang.music.constants.*
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.db.entities.SongEntity.Companion.STATE_DOWNLOADED
import com.zionhuang.music.extensions.reversed
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.ui.utils.resize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

@Dao
interface DatabaseDao {
    @Query("SELECT id FROM song")
    fun allSongId(): Flow<List<String>>

    @Query("SELECT id FROM song WHERE liked")
    fun allLikedSongId(): Flow<List<String>>

    @Query("SELECT id FROM album")
    fun allAlbumId(): Flow<List<String>>

    @Query("SELECT id FROM playlist")
    fun allPlaylistId(): Flow<List<String>>

    @Transaction
    @Query("SELECT * FROM song ORDER BY rowId DESC")
    fun songsByRowIdDesc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM song ORDER BY create_date DESC")
    fun songsByCreateDateDesc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM song ORDER BY title DESC")
    fun songsByNameDesc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM song ORDER BY totalPlayTime DESC")
    fun songsByPlayTimeDesc(): Flow<List<Song>>

    fun songs(sortType: SongSortType, descending: Boolean) =
        when (sortType) {
            SongSortType.CREATE_DATE -> songsByCreateDateDesc()
            SongSortType.NAME -> songsByNameDesc()
            SongSortType.ARTIST -> songsByRowIdDesc().map { songs ->
                songs.sortedWith(compareBy { song ->
                    song.artists.joinToString(separator = "") { it.name }
                })
            }
            SongSortType.PLAY_TIME -> songsByPlayTimeDesc()
        }.map { it.reversed(!descending) }

    fun likedSongs(sortType: SongSortType, descending: Boolean) =
        songs(sortType, descending).map { songs ->
            songs.filter { it.song.liked }
        }

    @Query("SELECT COUNT(1) FROM song WHERE liked")
    fun likedSongsCount(): Flow<Int>

    fun downloadedSongs(sortType: SongSortType, descending: Boolean) =
        songs(sortType, descending).map { songs ->
            songs.filter { it.song.downloadState == STATE_DOWNLOADED }
        }

    @Query("SELECT COUNT(*) FROM song WHERE download_state = $STATE_DOWNLOADED")
    fun downloadedSongsCount(): Flow<Int>

    @Transaction
    @Query("SELECT song.* FROM song JOIN song_album_map ON song.id = song_album_map.songId WHERE song_album_map.albumId = :albumId")
    fun albumSongs(albumId: String): Flow<List<Song>>

    @Transaction
    @Query("SELECT song.* FROM playlist_song_map JOIN song ON playlist_song_map.songId = song.id WHERE playlistId = :playlistId ORDER BY position")
    fun playlistSongs(playlistId: String): Flow<List<Song>>

    @Transaction
    @Query("SELECT song.* FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = :artistId ORDER BY create_date DESC")
    fun artistSongsByCreateDateDesc(artistId: String): Flow<List<Song>>

    @Transaction
    @Query("SELECT song.* FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = :artistId ORDER BY title DESC")
    fun artistSongsByNameDesc(artistId: String): Flow<List<Song>>

    @Transaction
    @Query("SELECT song.* FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = :artistId ORDER BY totalPlayTime DESC")
    fun artistSongsByPlayTimeDesc(artistId: String): Flow<List<Song>>

    fun artistSongs(artistId: String, sortType: ArtistSongSortType, descending: Boolean) =
        when (sortType) {
            ArtistSongSortType.CREATE_DATE -> artistSongsByCreateDateDesc(artistId)
            ArtistSongSortType.NAME -> artistSongsByNameDesc(artistId)
            ArtistSongSortType.PLAY_TIME -> artistSongsByPlayTimeDesc(artistId)
        }.map { it.reversed(!descending) }

    @Transaction
    @Query("SELECT * FROM song WHERE id = :songId")
    fun song(songId: String?): Flow<Song?>

    @Query("SELECT * FROM format WHERE id = :id")
    fun format(id: String?): Flow<FormatEntity?>

    @Query("SELECT * FROM lyrics WHERE id = :id")
    fun lyrics(id: String?): Flow<LyricsEntity?>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(1) FROM song_artist_map WHERE artistId = artist.id) AS songCount FROM artist ORDER BY rowId DESC")
    fun artistsByCreateDateDesc(): Flow<List<Artist>>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(1) FROM song_artist_map WHERE artistId = artist.id) AS songCount FROM artist ORDER BY name DESC")
    fun artistsByNameDesc(): Flow<List<Artist>>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(1) FROM song_artist_map WHERE artistId = artist.id) AS songCount FROM artist ORDER BY songCount DESC")
    fun artistsBySongCountDesc(): Flow<List<Artist>>

    fun artists(sortType: ArtistSortType, descending: Boolean) =
        when (sortType) {
            ArtistSortType.CREATE_DATE -> artistsByCreateDateDesc()
            ArtistSortType.NAME -> artistsByNameDesc()
            ArtistSortType.SONG_COUNT -> artistsBySongCountDesc()
        }.map { it.reversed(!descending) }

    @Query("SELECT * FROM artist WHERE id = :id")
    fun artist(id: String): Flow<ArtistEntity?>

    @Transaction
    @Query("SELECT * FROM album ORDER BY rowId DESC")
    fun albumsByRowIdDesc(): Flow<List<Album>>

    @Transaction
    @Query("SELECT * FROM album ORDER BY createDate DESC")
    fun albumsByCreateDateDesc(): Flow<List<Album>>

    @Transaction
    @Query("SELECT * FROM album ORDER BY title DESC")
    fun albumsByNameDesc(): Flow<List<Album>>

    @Transaction
    @Query("SELECT * FROM album ORDER BY year DESC")
    fun albumsByYearDesc(): Flow<List<Album>>

    @Transaction
    @Query("SELECT * FROM album ORDER BY songCount DESC")
    fun albumsBySongCountDesc(): Flow<List<Album>>

    @Transaction
    @Query("SELECT * FROM album ORDER BY duration DESC")
    fun albumsByLengthDesc(): Flow<List<Album>>

    fun albums(sortType: AlbumSortType, descending: Boolean) =
        when (sortType) {
            AlbumSortType.CREATE_DATE -> albumsByCreateDateDesc()
            AlbumSortType.NAME -> albumsByNameDesc()
            AlbumSortType.ARTIST -> albumsByRowIdDesc().map { albums ->
                albums.sortedWith(compareBy { album ->
                    album.artists.joinToString(separator = "") { it.name }
                })
            }
            AlbumSortType.YEAR -> albumsByYearDesc()
            AlbumSortType.SONG_COUNT -> albumsBySongCountDesc()
            AlbumSortType.LENGTH -> albumsByLengthDesc()
        }.map { it.reversed(!descending) }

    @Transaction
    @Query("SELECT * FROM album WHERE id = :id")
    fun album(id: String): Album?

    @Transaction
    @Query("SELECT * FROM album WHERE id = :albumId")
    fun albumWithSongs(albumId: String): Flow<AlbumWithSongs?>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist ORDER BY createDate DESC")
    fun playlistsByCreateDateDesc(): Flow<List<Playlist>>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist ORDER BY name DESC")
    fun playlistsByNameDesc(): Flow<List<Playlist>>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist ORDER BY songCount DESC")
    fun playlistsBySongCountDesc(): Flow<List<Playlist>>

    fun playlists(sortType: PlaylistSortType, descending: Boolean) =
        when (sortType) {
            PlaylistSortType.CREATE_DATE -> playlistsByCreateDateDesc()
            PlaylistSortType.NAME -> playlistsByNameDesc()
            PlaylistSortType.SONG_COUNT -> playlistsBySongCountDesc()
        }.map { it.reversed(!descending) }

    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist WHERE id = :playlistId")
    fun playlist(playlistId: String): Flow<Playlist?>

    @Transaction
    @Query("SELECT * FROM song WHERE title LIKE '%' || :query || '%' LIMIT :previewSize")
    fun searchSongs(query: String, previewSize: Int = Int.MAX_VALUE): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM song WHERE title LIKE '%' || :query || '%' AND download_state = $STATE_DOWNLOADED LIMIT :previewSize")
    fun searchDownloadedSongs(query: String, previewSize: Int = Int.MAX_VALUE): Flow<List<Song>>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM song_artist_map WHERE artistId = artist.id) AS songCount FROM artist WHERE name LIKE '%' || :query || '%' LIMIT :previewSize")
    fun searchArtists(query: String, previewSize: Int = Int.MAX_VALUE): Flow<List<Artist>>

    @Transaction
    @Query("SELECT * FROM album WHERE title LIKE '%' || :query || '%' LIMIT :previewSize")
    fun searchAlbums(query: String, previewSize: Int = Int.MAX_VALUE): Flow<List<Album>>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist WHERE name LIKE '%' || :query || '%' LIMIT :previewSize")
    fun searchPlaylists(query: String, previewSize: Int = Int.MAX_VALUE): Flow<List<Playlist>>

    @Query("SELECT * FROM search_history WHERE `query` LIKE :query || '%' ORDER BY id DESC")
    fun searchHistory(query: String = ""): Flow<List<SearchHistory>>

    @Query("DELETE FROM search_history")
    fun clearSearchHistory()

    @Query("UPDATE song SET totalPlayTime = totalPlayTime + :playTime WHERE id = :songId")
    fun incrementTotalPlayTime(songId: String, playTime: Long)

    @Query("""
        UPDATE playlist_song_map SET position = 
            CASE 
                WHEN position < :fromPosition THEN position + 1
                WHEN position > :fromPosition THEN position - 1
                ELSE :toPosition
            END 
        WHERE playlistId = :playlistId AND position BETWEEN MIN(:fromPosition,:toPosition) AND MAX(:fromPosition,:toPosition)
    """)
    fun move(playlistId: Long, fromPosition: Int, toPosition: Int)

    @Query("SELECT * FROM artist WHERE name = :name")
    fun artistByName(name: String): ArtistEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(artist: ArtistEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(map: SongArtistMap)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(map: SongAlbumMap)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(map: AlbumArtistMap)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(map: PlaylistSongMap)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(searchHistory: SearchHistory)

    @Transaction
    fun insert(mediaMetadata: MediaMetadata, block: (SongEntity) -> SongEntity = { it }) {
        insert(mediaMetadata.toSongEntity().let(block))
        mediaMetadata.artists.forEachIndexed { index, artist ->
            val artistId = artist.id ?: artistByName(artist.name)?.id ?: ArtistEntity.generateArtistId()
            insert(ArtistEntity(
                id = artistId,
                name = artist.name
            ))
            insert(SongArtistMap(
                songId = mediaMetadata.id,
                artistId = artistId,
                position = index
            ))
        }
    }

    @Transaction
    fun insert(albumPage: AlbumPage) {
        insert(AlbumEntity(
            id = albumPage.album.browseId,
            title = albumPage.album.title,
            year = albumPage.album.year,
            thumbnailUrl = albumPage.album.thumbnail,
            songCount = albumPage.songs.size,
            duration = albumPage.songs.sumOf { it.duration ?: 0 }
        ))
        albumPage.songs.map(SongItem::toMediaMetadata).forEach(::insert)
        albumPage.songs.mapIndexed { index, song ->
            SongAlbumMap(
                songId = song.id,
                albumId = albumPage.album.browseId,
                index = index
            )
        }.forEach(::upsert)
        albumPage.album.artists?.map { artist ->
            ArtistEntity(
                id = artist.id ?: artistByName(artist.name)?.id ?: ArtistEntity.generateArtistId(),
                name = artist.name
            )
        }?.forEach(::insert)
        albumPage.album.artists?.mapIndexed { index, artist ->
            AlbumArtistMap(
                albumId = albumPage.album.browseId,
                artistId = artist.id ?: artistByName(artist.name)?.id ?: ArtistEntity.generateArtistId(),
                order = index
            )
        }?.forEach(::insert)
    }

    @Transaction
    fun insert(albumWithSongs: AlbumWithSongs) {
        insert(albumWithSongs.album)
        albumWithSongs.songs.map(Song::toMediaMetadata).forEach(::insert)
        albumWithSongs.songs.mapIndexed { index, song ->
            SongAlbumMap(
                songId = song.id,
                albumId = albumWithSongs.album.id,
                index = index
            )
        }.forEach(::upsert)
        albumWithSongs.artists.forEach(::insert)
        albumWithSongs.artists.mapIndexed { index, artist ->
            AlbumArtistMap(
                albumId = albumWithSongs.album.id,
                artistId = artist.id,
                order = index
            )
        }.forEach(::insert)
    }

    @Update
    fun update(song: SongEntity)

    @Update
    fun update(artist: ArtistEntity)

    @Update
    fun update(map: PlaylistSongMap)

    fun update(artist: ArtistEntity, artistPage: ArtistPage) {
        update(artist.copy(
            name = artistPage.artist.title,
            thumbnailUrl = artistPage.artist.thumbnail.resize(400, 400),
            lastUpdateTime = LocalDateTime.now()
        ))
    }

    @Upsert
    fun upsert(map: SongAlbumMap)

    @Upsert
    fun upsert(lyrics: LyricsEntity)

    @Upsert
    fun upsert(format: FormatEntity)

    @Delete
    fun delete(song: SongEntity)

    @Delete
    fun delete(artist: ArtistEntity)

    @Delete
    fun delete(album: AlbumEntity)

    @Delete
    fun delete(lyrics: LyricsEntity)

    @Delete
    fun delete(searchHistory: SearchHistory)

    @Query("SELECT * FROM playlist_song_map WHERE songId = :songId")
    fun playlistSongMaps(songId: String): List<PlaylistSongMap>

    @Query("SELECT * FROM playlist_song_map WHERE playlistId = :playlistId AND position >= :from ORDER BY position")
    fun playlistSongMaps(playlistId: String, from: Int): List<PlaylistSongMap>

    @Query("SELECT COUNT(1) FROM song_artist_map WHERE artistId = :id")
    fun artistSongCount(id: String): Int

    @Transaction
    fun verifyPlaylistSongPosition(playlistId: String, from: Int) {
        val maps = playlistSongMaps(playlistId, from)
        var position = if (from <= 0) 0 else maps[0].position
        maps.map { it.copy(position = position++) }.forEach(::update)
    }

    @Transaction
    fun delete(song: Song) {
        if (song.album != null) return
        delete(song.song)
        song.artists.filter { artistSongCount(it.id) == 0 }.forEach(::delete)
        playlistSongMaps(song.id)
            .groupBy { it.playlistId }
            .mapValues { entry ->
                entry.value.minOf { it.position } - 1
            }
            .forEach { (playlistId, position) ->
                verifyPlaylistSongPosition(playlistId, position)
            }
    }

    @Transaction
    fun delete(album: Album) {
        runBlocking(Dispatchers.IO) {
            albumSongs(album.id).first()
        }.map {
            it.copy(album = null)
        }.forEach(::delete)
        delete(album.album)
        album.artists.filter { artistSongCount(it.id) == 0 }.forEach(::delete)
    }
}
