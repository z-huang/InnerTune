package com.zionhuang.music.extensions

import androidx.paging.PagingSource.LoadResult
import com.zionhuang.innertube.models.*
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ALBUM
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ARTIST
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.utils.joinByBullet
import com.zionhuang.music.utils.makeTimeString
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.ListInfo
import org.schabi.newpipe.extractor.Page

// the SongItem should be produced by get_queue endpoint to have detailed information
fun SongItem.toSongEntity() = SongEntity(
    id = id,
    title = title,
    duration = duration!!,
    thumbnailUrl = thumbnails.last().url,
    albumId = album?.navigationEndpoint?.browseId,
    albumName = album?.text
)

fun PlaylistItem.toPlaylistEntity() = PlaylistEntity(
    id = id,
    name = title,
    thumbnailUrl = thumbnails.last().url
)

fun AlbumOrPlaylistHeader.toPlaylistEntity() = PlaylistEntity(
    id = id,
    name = name,
    author = artists?.firstOrNull()?.text,
    authorId = artists?.firstOrNull()?.navigationEndpoint?.browseEndpoint?.browseId,
    year = year,
    thumbnailUrl = thumbnails.lastOrNull()?.url
)

fun <T : InfoItem> ListInfo<T>.toPage() = LoadResult.Page<Page, InfoItem>(
    data = relatedItems,
    nextKey = nextPage,
    prevKey = null
)

fun <T : InfoItem> InfoItemsPage<T>.toPage() = LoadResult.Page<Page, InfoItem>(
    data = items,
    nextKey = nextPage,
    prevKey = null
)

fun BrowseResult.toPage() = LoadResult.Page(
    data = items,
    nextKey = continuations?.ifEmpty { null },
    prevKey = null
)