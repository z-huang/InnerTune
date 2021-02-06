package com.zionhuang.music.youtube

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.youtube.model.SearchResult
import com.google.api.services.youtube.model.Video
import com.zionhuang.music.youtube.newpipe.ExtractorHelper
import com.zionhuang.music.youtube.newpipe.SearchCache
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.services.youtube.YoutubeService

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

    class NewPipeSearch(private val query: String) : PagingSource<Page, InfoItem>() {
        private val youtubeService = NewPipe.getService(ServiceList.YouTube.serviceId) as YoutubeService
        private val contentFilter = emptyList<String>()
        private val sortFilter = ""

        @Suppress("BlockingMethodInNonBlockingContext")
        override suspend fun load(params: LoadParams<Page>): LoadResult<Page, InfoItem> =
                if (query in SearchCache && SearchCache[query]!!.nextKey != params.key) {
                    val cache = SearchCache[query]!!
                    LoadResult.Page(
                            data = cache.items,
                            nextKey = cache.nextKey,
                            prevKey = null
                    )
                } else try {
                    if (params.key == null) {
                        val searchInfo = ExtractorHelper.search(youtubeService, query, contentFilter, sortFilter)
                        SearchCache.add(query, searchInfo)
                        LoadResult.Page(
                                data = searchInfo.relatedItems,
                                nextKey = searchInfo.nextPage,
                                prevKey = null
                        )
                    } else {
                        val infoItemsPage = ExtractorHelper.search(youtubeService, query, contentFilter, sortFilter, params.key!!)
                        SearchCache.add(query, infoItemsPage)
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