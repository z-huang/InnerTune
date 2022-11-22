package com.zionhuang.music.compose.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ALBUM
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ARTIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_COMMUNITY_PLAYLIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_FEATURED_PLAYLIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_VIDEO
import com.zionhuang.innertube.models.*
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.YouTubeListItem
import com.zionhuang.music.compose.component.shimmer.ListItemPlaceHolder
import com.zionhuang.music.compose.component.shimmer.ShimmerHost
import com.zionhuang.music.constants.*
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.repos.YouTubeRepository
import com.zionhuang.music.viewmodels.SearchViewModel
import com.zionhuang.music.viewmodels.SearchViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnlineSearchResult(
    query: String,
    navController: NavController,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(
        repository = YouTubeRepository(LocalContext.current),
        query = query
    )),
) {
    val playerConnection = LocalPlayerConnection.current
    val playWhenReady by playerConnection.playWhenReady.collectAsState(initial = false)
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState(initial = null)

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val items = viewModel.pagingData.collectAsLazyPagingItems()
    val searchFilter by viewModel.filter.observeAsState()

    LazyColumn(
        state = lazyListState,
        contentPadding = WindowInsets.systemBars
            .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
            .add(WindowInsets(
                top = AppBarHeight.dp + SearchFilterHeight.dp,
                bottom = NavigationBarHeight.dp + MiniPlayerHeight.dp
            ))
            .asPaddingValues()
    ) {
        if (items.loadState.refresh !is LoadState.Loading) {
            items(items) { item ->
                when (item) {
                    is Header -> {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ListItemHeight.dp)
                                .padding(12.dp)
                                .animateItemPlacement()
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.headlineMedium,
                                maxLines = 1
                            )
                        }
                    }
                    is YTItem -> {
                        YouTubeListItem(
                            item = item,
                            playingIndicator = mediaMetadata?.id == item.id,
                            playWhenReady = playWhenReady,
                            modifier = Modifier
                                .clickable {
                                    when (item) {
                                        is SongItem -> {
                                            playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = item.id), item))
                                        }
                                        is AlbumItem -> {
                                            navController.navigate("album/${item.id}?playlistId=${item.playlistId}")
                                        }
                                        is ArtistItem -> TODO()
                                        is PlaylistItem -> TODO()
                                    }
                                    when (val endpoint = item.navigationEndpoint.endpoint) {
                                        is WatchEndpoint -> {
                                            playerConnection.playQueue(YouTubeQueue(endpoint, item as? SongItem))
                                        }
                                        is WatchPlaylistEndpoint -> {
                                            playerConnection.playQueue(YouTubeQueue(endpoint.toWatchEndpoint(), item as? SongItem))
                                        }
                                        is BrowseEndpoint -> {}
                                        is SearchEndpoint -> {}
                                        is QueueAddEndpoint -> playerConnection.songPlayer?.handleQueueAddEndpoint(endpoint, item)
                                        is ShareEntityEndpoint -> {}
                                        is BrowseLocalArtistSongsEndpoint -> {}
                                        null -> {}
                                    }
                                }
                                .animateItemPlacement()
                        )
                    }
                    else -> {}
                }
            }
        }

        item {
            ShimmerHost {
                repeat(when {
                    items.loadState.refresh is LoadState.Loading -> 8
                    items.loadState.append is LoadState.Loading -> 3
                    else -> 0
                }) {
                    ListItemPlaceHolder()
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .padding(WindowInsets.systemBars
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                .add(WindowInsets(top = AppBarHeight.dp))
                .asPaddingValues())
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.width(8.dp))

        listOf(
            null to R.string.search_filter_all,
            FILTER_SONG to R.string.search_filter_songs,
            FILTER_VIDEO to R.string.search_filter_videos,
            FILTER_ALBUM to R.string.search_filter_albums,
            FILTER_ARTIST to R.string.search_filter_artists,
            FILTER_COMMUNITY_PLAYLIST to R.string.search_filter_community_playlists,
            FILTER_FEATURED_PLAYLIST to R.string.search_filter_featured_playlists
        ).forEach { (filter, label) ->
            FilterChip(
                label = { Text(text = stringResource(label)) },
                selected = searchFilter == filter,
                colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.background),
                onClick = {
                    if (viewModel.filter.value == filter) {
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(0)
                        }
                    } else {
                        viewModel.filter.value = filter
                        items.refresh()
                    }
                }
            )
            Spacer(Modifier.width(8.dp))
        }
    }
}