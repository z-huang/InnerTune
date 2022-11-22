package com.zionhuang.music.youtube

import android.content.Context
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.AlbumOrPlaylistHeader
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.music.db.entities.AlbumEntity
import com.zionhuang.music.db.entities.AlbumWithSongs
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.toSong
import com.zionhuang.music.repos.SongRepository

data class YouTubeAlbum(
    val album: AlbumEntity,
    val artists: List<ArtistEntity>,
    val songs: List<SongItem>,
)

suspend fun AlbumItem.toAlbumWithSongs(context: Context): AlbumWithSongs {
    val songIds = YouTube.browse(BrowseEndpoint(browseId = "VL$playlistId")).getOrThrow()
        .items
        .filterIsInstance<SongItem>()
        .map {
            it.id
        }
    val songs = YouTube.getQueue(videoIds = songIds).getOrThrow()
    YouTubeAlbum(
        album = AlbumEntity(
            id = id,
            title = title,
            year = year,
            thumbnailUrl = thumbnails.last().url,
            songCount = songs.size,
            duration = songs.sumOf { it.duration ?: 0 }
        ),
        artists = (YouTube.browse(BrowseEndpoint(browseId = id)).getOrThrow().items.firstOrNull() as? AlbumOrPlaylistHeader)
            ?.artists
            ?.map { run ->
                val artistId = run.navigationEndpoint?.browseEndpoint?.browseId
                    ?: SongRepository(context).getArtistByName(run.text)?.id
                    ?: ArtistEntity.generateArtistId()
                ArtistEntity(
                    id = artistId,
                    name = run.text
                )
            }.orEmpty(),
        songs = songs
    )
    return AlbumWithSongs(
        album = AlbumEntity(
            id = id,
            title = title,
            year = year,
            thumbnailUrl = thumbnails.last().url,
            songCount = songs.size,
            duration = songs.sumOf { it.duration ?: 0 }
        ),
        artists = (YouTube.browse(BrowseEndpoint(browseId = id)).getOrThrow().items.firstOrNull() as? AlbumOrPlaylistHeader)
            ?.artists
            ?.map { run ->
                val artistId = run.navigationEndpoint?.browseEndpoint?.browseId
                    ?: SongRepository(context).getArtistByName(run.text)?.id
                    ?: ArtistEntity.generateArtistId()
                ArtistEntity(
                    id = artistId,
                    name = run.text
                )
            }.orEmpty(),
        songs = songs.map { it.toSong(context) }
    )
}

suspend fun AlbumItem.getYouTubeAlbum(context: Context): YouTubeAlbum {
    val songIds = YouTube.browse(BrowseEndpoint(browseId = "VL$playlistId")).getOrThrow()
        .items
        .filterIsInstance<SongItem>()
        .map {
            it.id
        }
    val songs = YouTube.getQueue(videoIds = songIds).getOrThrow()
    return YouTubeAlbum(
        album = AlbumEntity(
            id = id,
            title = title,
            year = year,
            thumbnailUrl = thumbnails.last().url,
            songCount = songs.size,
            duration = songs.sumOf { it.duration ?: 0 }
        ),
        artists = (YouTube.browse(BrowseEndpoint(browseId = id)).getOrThrow().items.firstOrNull() as? AlbumOrPlaylistHeader)
            ?.artists
            ?.map { run ->
                val artistId = run.navigationEndpoint?.browseEndpoint?.browseId
                    ?: SongRepository(context).getArtistByName(run.text)?.id
                    ?: ArtistEntity.generateArtistId()
                ArtistEntity(
                    id = artistId,
                    name = run.text
                )
            }.orEmpty(),
        songs = songs
    )
}