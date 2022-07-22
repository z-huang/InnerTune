package com.zionhuang.music.repos

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.BaseItem
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.music.extensions.toPage
import com.zionhuang.music.youtube.InfoCache.checkCache
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object YouTubeRepository {
    fun searchAll(query: String) = object : PagingSource<List<String>, BaseItem>() {
        override suspend fun load(params: LoadParams<List<String>>) = withContext(IO) {
            try {
                YouTube.searchAllType(query).toPage()
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<List<String>, BaseItem>): List<String>? = null
    }

    fun search(query: String, filter: YouTube.SearchFilter): PagingSource<List<String>, BaseItem> = object : PagingSource<List<String>, BaseItem>() {
        override suspend fun load(params: LoadParams<List<String>>) = withContext(IO) {
            try {
                if (params.key == null) {
                    YouTube.search(query, filter)
                } else {
                    YouTube.search(params.key!![0])
                }.toPage()
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<List<String>, BaseItem>): List<String>? = null
    }

    fun browse(endpoint: BrowseEndpoint): PagingSource<List<String>, BaseItem> = object : PagingSource<List<String>, BaseItem>() {
        override suspend fun load(params: LoadParams<List<String>>) = withContext(IO) {
            try {
                if (params.key == null) {
                    YouTube.browse(endpoint)
                } else {
                    YouTube.browse(params.key!!)
                }.toPage()
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<List<String>, BaseItem>): List<String>? = null
    }

    suspend fun getSuggestions(query: String): List<BaseItem> = withContext(IO) {
        checkCache("SU$query") {
            YouTube.getSearchSuggestions(query)
        }
    }
}