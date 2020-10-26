package com.zionhuang.music.youtube

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.google.api.services.youtube.model.SearchResult
import com.zionhuang.music.db.RemoteDatabase
import com.zionhuang.music.db.toSearchEntity
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class YouTubeRemoteMediator(
        private val query: String,
        remoteDatabase: RemoteDatabase,
        private val apiService: YouTubeAPIService
) : RemoteMediator<String, SearchResult>() {
    private val remoteDao = remoteDatabase.remoteDao

    override suspend fun load(loadType: LoadType, state: PagingState<String, SearchResult>): MediatorResult {
        return try {
            var queryId: Long = -1
            val pageToken = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(true)
                LoadType.APPEND -> remoteDao.getRemoteKey(query).also { queryId = it.queryId }.nextPageToken
                        ?: return MediatorResult.Success(true)
            }
            if (pageToken == null) {
                return MediatorResult.Success(true)
            }
            val searchResult = apiService.searchAsync(query, pageToken)
            remoteDao.insertSearchEntities(searchResult.items.map {
                it.toSearchEntity(queryId)
            })
            MediatorResult.Success(searchResult.nextPageToken == null)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}