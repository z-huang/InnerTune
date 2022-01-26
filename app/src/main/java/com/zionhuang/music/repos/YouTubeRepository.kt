package com.zionhuang.music.repos

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.zionhuang.music.extensions.toPage
import com.zionhuang.music.repos.base.RemoteRepository
import com.zionhuang.music.youtube.newpipe.NewPipeYouTubeHelper
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page

object YouTubeRepository : RemoteRepository {
    override fun search(query: String, filter: String): PagingSource<Page, InfoItem> = object : PagingSource<Page, InfoItem>() {
        @Suppress("BlockingMethodInNonBlockingContext")
        override suspend fun load(params: LoadParams<Page>) = try {
            if (params.key == null) NewPipeYouTubeHelper.search(query, listOf(filter)).toPage()
            else NewPipeYouTubeHelper.search(query, listOf(filter), params.key!!).toPage()
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

        override fun getRefreshKey(state: PagingState<Page, InfoItem>): Page? = null
    }

    override suspend fun suggestionsFor(query: String): List<String> = NewPipeYouTubeHelper.suggestionsFor(query)

    override fun getChannel(channelId: String) = object : PagingSource<Page, InfoItem>() {
        override suspend fun load(params: LoadParams<Page>) = try {
            if (params.key == null) NewPipeYouTubeHelper.getChannel(channelId).toPage()
            else NewPipeYouTubeHelper.getChannel(channelId, params.key!!).toPage()
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

        override fun getRefreshKey(state: PagingState<Page, InfoItem>): Page? = null
    }

    override fun getPlaylist(playlistId: String) = object : PagingSource<Page, InfoItem>() {
        override suspend fun load(params: LoadParams<Page>) = try {
            if (params.key == null) NewPipeYouTubeHelper.getPlaylist(playlistId).toPage()
            else NewPipeYouTubeHelper.getPlaylist(playlistId, params.key!!).toPage()
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

        override fun getRefreshKey(state: PagingState<Page, InfoItem>): Page? = null
    }

    override fun getAlbum(albumId: String) {
        TODO("Not yet implemented")
    }
}