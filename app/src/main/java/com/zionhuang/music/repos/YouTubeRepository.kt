package com.zionhuang.music.repos

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.ArtistHeader
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.YTBaseItem
import com.zionhuang.music.extensions.toPage
import com.zionhuang.music.youtube.InfoCache.checkCache
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object YouTubeRepository {
    fun searchAll(query: String) = object : PagingSource<List<String>, YTBaseItem>() {
        override suspend fun load(params: LoadParams<List<String>>) = withContext(IO) {
            try {
                YouTube.searchAllType(query).toPage()
            } catch (e: Exception) {
                e.printStackTrace()
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<List<String>, YTBaseItem>): List<String>? = null
    }

    fun search(query: String, filter: YouTube.SearchFilter): PagingSource<List<String>, YTBaseItem> = object : PagingSource<List<String>, YTBaseItem>() {
        override suspend fun load(params: LoadParams<List<String>>) = withContext(IO) {
            try {
                if (params.key == null) {
                    YouTube.search(query, filter)
                } else {
                    YouTube.search(params.key!![0])
                }.toPage()
            } catch (e: Exception) {
                e.printStackTrace()
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<List<String>, YTBaseItem>): List<String>? = null
    }

    fun browse(endpoint: BrowseEndpoint): PagingSource<List<String>, YTBaseItem> = object : PagingSource<List<String>, YTBaseItem>() {
        override suspend fun load(params: LoadParams<List<String>>) = withContext(IO) {
            try {
                if (params.key == null) {
                    val browseResult = YouTube.browse(endpoint)
                    if (endpoint.isArtistEndpoint) {
                        browseResult.copy(
                            items = browseResult.items.toMutableList().apply {
                                addAll(if (browseResult.items.firstOrNull() is ArtistHeader) 1 else 0, SongRepository.getArtistSongsPreview(endpoint.browseId))
                            }
                        )
                    } else {
                        browseResult
                    }
                } else {
                    YouTube.browse(params.key!!)
                }.toPage()
            } catch (e: Exception) {
                e.printStackTrace()
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<List<String>, YTBaseItem>): List<String>? = null
    }

    suspend fun getSuggestions(query: String): List<YTBaseItem> = withContext(IO) {
        checkCache("SU$query") {
            YouTube.getSearchSuggestions(query)
        }
    }
}