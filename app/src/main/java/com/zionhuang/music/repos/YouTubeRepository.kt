package com.zionhuang.music.repos

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.YouTube.EXPLORE_BROWSE_ID
import com.zionhuang.innertube.YouTube.HOME_BROWSE_ID
import com.zionhuang.innertube.models.*
import com.zionhuang.innertube.models.Icon.Companion.ICON_EXPLORE
import com.zionhuang.innertube.utils.plus
import com.zionhuang.music.R
import com.zionhuang.music.extensions.getApplication
import com.zionhuang.music.extensions.toPage
import com.zionhuang.music.utils.InfoCache.checkCache
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class YouTubeRepository(val context: Context) {
    private val songRepository = SongRepository(context)

    fun searchAll(query: String) = object : PagingSource<List<String>, YTBaseItem>() {
        override suspend fun load(params: LoadParams<List<String>>) = withContext(IO) {
            try {
                YouTube.searchAllType(query).getOrThrow().toPage()
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<List<String>, YTBaseItem>): List<String>? = null
    }

    fun search(query: String, filter: YouTube.SearchFilter): PagingSource<List<String>, YTBaseItem> = object : PagingSource<List<String>, YTBaseItem>() {
        override suspend fun load(params: LoadParams<List<String>>) = withContext(IO) {
            try {
                if (params.key == null) {
                    YouTube.search(query, filter).getOrThrow()
                } else {
                    YouTube.search(params.key!![0]).getOrThrow()
                }.toPage()
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<List<String>, YTBaseItem>): List<String>? = null
    }

    fun browse(endpoint: BrowseEndpoint): PagingSource<List<String>, YTBaseItem> = object : PagingSource<List<String>, YTBaseItem>() {
        override suspend fun load(params: LoadParams<List<String>>) = withContext(IO) {
            try {
                if (params.key == null) {
                    val browseResult = YouTube.browse(endpoint).getOrThrow()
                    if (endpoint.browseId == HOME_BROWSE_ID) {
                        // inject explore link
                        browseResult.copy(
                            items = NavigationItem(
                                title = getApplication().getString(R.string.title_explore),
                                icon = ICON_EXPLORE,
                                navigationEndpoint = NavigationEndpoint(
                                    browseEndpoint = BrowseEndpoint(browseId = EXPLORE_BROWSE_ID)
                                )
                            ) + browseResult.items
                        )
                    } else if (endpoint.isArtistEndpoint && endpoint.params == null) {
                        // inject library artist songs preview
                        browseResult.copy(
                            items = browseResult.items.toMutableList().apply {
                                addAll(if (browseResult.items.firstOrNull() is ArtistHeader) 1 else 0, songRepository.getArtistSongsPreview(endpoint.browseId).getOrThrow())
                            }
                        )
                    } else {
                        browseResult
                    }
                } else {
                    YouTube.browse(params.key!!).getOrThrow()
                }.toPage()
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<List<String>, YTBaseItem>): List<String>? = null
    }

    suspend fun getSuggestions(query: String): List<YTBaseItem> = withContext(IO) {
        checkCache("SU$query") {
            YouTube.getSearchSuggestions(query).getOrThrow()
        }
    }
}