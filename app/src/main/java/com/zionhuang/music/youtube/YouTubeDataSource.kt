package com.zionhuang.music.youtube

import androidx.paging.PagingSource
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.youtube.model.SearchResult
import com.google.api.services.youtube.model.Video

class YouTubeDataSource {
    class Search(private val youTubeRepository: YouTubeRepository, private val query: String) : PagingSource<String, SearchResult>() {
        @Suppress("BlockingMethodInNonBlockingContext")
        override suspend fun load(params: LoadParams<String>): LoadResult<String, SearchResult> {
            return try {
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
        }

        override fun getRefreshKey(state: PagingState<String, SearchResult>): String? = null
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