package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.*
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.*
import com.zionhuang.music.repos.YouTubeRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class YouTubeBrowseViewModel(application: Application, private val browseEndpoint: BrowseEndpoint) : AndroidViewModel(application) {
    private val youTubeRepository = YouTubeRepository(application)
    private var _albumName: String? = null
    val albumName: String? get() = _albumName
    private var _albumSongs: List<SongItem>? = null
    val albumSongs: List<SongItem>? get() = _albumSongs

    val pagingData = Pager(PagingConfig(pageSize = 20)) {
        if (browseEndpoint.isAlbumEndpoint) {
            object : PagingSource<List<String>, YTBaseItem>() {
                override suspend fun load(params: LoadParams<List<String>>): LoadResult<List<String>, YTBaseItem> = withContext(IO) {
                    YouTube.browse(browseEndpoint).map { result ->
                        _albumName = (result.items.firstOrNull() as? AlbumOrPlaylistHeader)?.name
                        LoadResult.Page<List<String>, YTBaseItem>(
                            data = result.items.also { items ->
                                _albumSongs = items.filterIsInstance<SongItem>().map {
                                    // replaced album audio items have inappropriate navigation endpoint, so we remove it and let clicking handled by fragment
                                    it.copy(navigationEndpoint = NavigationEndpoint())
                                }
                            },
                            prevKey = null,
                            nextKey = null
                        )
                    }.getOrElse { throwable ->
                        LoadResult.Error(throwable)
                    }
                }

                override fun getRefreshKey(state: PagingState<List<String>, YTBaseItem>): List<String>? = null
            }
        } else {
            youTubeRepository.browse(browseEndpoint)
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