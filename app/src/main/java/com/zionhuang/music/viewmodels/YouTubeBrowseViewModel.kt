package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.*
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.NavigationEndpoint
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.models.YTBaseItem
import com.zionhuang.music.repos.YouTubeRepository

class YouTubeBrowseViewModel(application: Application, private val browseEndpoint: BrowseEndpoint) : AndroidViewModel(application) {
    private var albumSongs: List<SongItem>? = null
    fun getAlbumSongs() = albumSongs

    val pagingData = Pager(PagingConfig(pageSize = 20)) {
        if (browseEndpoint.isAlbumEndpoint) {
            object : PagingSource<List<String>, YTBaseItem>() {
                override suspend fun load(params: LoadParams<List<String>>): LoadResult<List<String>, YTBaseItem> = LoadResult.Page(
                    data = YouTube.browse(browseEndpoint).items.also { items ->
                        albumSongs = items.filterIsInstance<SongItem>().map {
                            // replaced album audio items have inappropriate navigation endpoint, so we remove it and let clicking handled by fragment
                            it.copy(navigationEndpoint = NavigationEndpoint())
                        }
                    },
                    prevKey = null,
                    nextKey = null
                )

                override fun getRefreshKey(state: PagingState<List<String>, YTBaseItem>): List<String>? = null
            }
        } else {
            YouTubeRepository.browse(browseEndpoint)
        }
    }.flow.cachedIn(viewModelScope)
}

class YouTubeBrowseViewModelFactory(
    val application: Application,
    private val browseEndpoint: BrowseEndpoint,
) : ViewModelProvider.AndroidViewModelFactory(application) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        YouTubeBrowseViewModel(application, browseEndpoint) as T
}