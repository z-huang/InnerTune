package com.zionhuang.music.repos.base

import androidx.paging.PagingSource
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.stream.StreamInfo

interface RemoteRepository {
    fun search(query: String, filter: String): PagingSource<Page, InfoItem>
    suspend fun suggestionsFor(query: String): List<String>

    @Throws(UnsupportedOperationException::class)
    suspend fun getStream(songId: String): StreamInfo

    @Throws(UnsupportedOperationException::class)
    fun getChannel(channelId: String): PagingSource<Page, InfoItem>

    @Throws(UnsupportedOperationException::class)
    fun getPlaylist(playlistId: String): PagingSource<Page, InfoItem>

    @Throws(UnsupportedOperationException::class)
    fun getAlbum(albumId: String)
}