package com.zionhuang.music.youtube

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.youtube.model.SearchResult
import com.google.api.services.youtube.model.Video
import com.zionhuang.music.youtube.newpipe.ExtractorHelper
import com.zionhuang.music.youtube.newpipe.SearchQuery
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page

class YouTubeDataSource {
    class Search(private val youTubeRepository: YouTubeRepository, private val query: String) : PagingSource<String, SearchResult>() {
        @Suppress("BlockingMethodInNonBlockingContext")
        override suspend fun load(params: LoadParams<String>): LoadResult<String, SearchResult> =
                try {
                    val res = youTubeRepository.search(query, params.key)
                    LoadResult.Page(
                            data = res.items,
                            prevKey = res.prevPageToken,
                            nextKey = res.nextPageToken
                    )
                } catch (e: GoogleJsonResponseException) {
                    LoadResult.Error(Throwable(e.details.message))
                } catch (e: Exception) {
                    LoadResult.Error(e)
                }

        override fun getRefreshKey(state: PagingState<String, SearchResult>): String? = null
    }

    class NewPipeSearch(val queryString: String, filter: String) : PagingSource<Page, InfoItem>() {
        private val searchQuery = SearchQuery(queryString, filter)
        private val contentFilter = listOf(filter)

        @Suppress("BlockingMethodInNonBlockingContext")
        override suspend fun load(params: LoadParams<Page>): LoadResult<Page, InfoItem> = try {
            if (params.key == null) {
                val searchInfo = ExtractorHelper.search(queryString, contentFilter)
                LoadResult.Page(
                        data = searchInfo.relatedItems,
                        nextKey = searchInfo.nextPage,
                        prevKey = null
                )
            } else {
                val infoItemsPage = ExtractorHelper.search(queryString, contentFilter, params.key!!)
                LoadResult.Page(
                        data = infoItemsPage.items,
                        nextKey = infoItemsPage.nextPage,
                        prevKey = null
                )
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

        override fun getRefreshKey(state: PagingState<Page, InfoItem>): Page? = null
    }

    class NewPipePlaylist(private val url: String) : PagingSource<Page, InfoItem>() {
        override suspend fun load(params: LoadParams<Page>): LoadResult<Page, InfoItem> = try {
            if (params.key == null) {
                val playlistInfo = ExtractorHelper.getPlaylist(url)
                LoadResult.Page(
                        data = playlistInfo.relatedItems,
                        nextKey = playlistInfo.nextPage,
                        prevKey = null
                )
            } else {
                val infoItemsPage = ExtractorHelper.getPlaylist(url, params.key!!)
                LoadResult.Page(
                        data = infoItemsPage.items,
                        nextKey = infoItemsPage.nextPage,
                        prevKey = null
                )
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

        override fun getRefreshKey(state: PagingState<Page, InfoItem>): Page? = null
    }

    class Popular(private val youTubeRepository: YouTubeRepository) : PagingSource<String, Video>() {
        @Suppress("BlockingMethodInNonBlockingContext")
        override suspend fun load(params: LoadParams<String>): LoadResult<String, Video> {
            return try {
                val res = youTubeRepository.getPopularMusic(params.key)
                LoadResult.Page(
                        data = res.items,
                        prevKey = res.prevPageToken,
                        nextKey = res.nextPageToken
                )
            } catch (e: GoogleJsonResponseException) {
                LoadResult.Error(Throwable(e.details.message))
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<String, Video>): String? = null
    }
}