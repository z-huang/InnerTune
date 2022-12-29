package com.zionhuang.music.compose.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.zionhuang.music.compose.LocalPlayerAwareWindowInsets
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.LocalMenuState
import com.zionhuang.music.compose.component.YouTubeListItem
import com.zionhuang.music.compose.component.shimmer.ListItemPlaceHolder
import com.zionhuang.music.compose.component.shimmer.ShimmerHost
import com.zionhuang.music.compose.menu.YouTubeAlbumMenu
import com.zionhuang.music.compose.menu.YouTubeArtistMenu
import com.zionhuang.music.compose.menu.YouTubeSongMenu
import com.zionhuang.music.compose.utils.rememberLazyListState
import com.zionhuang.music.constants.AppBarHeight
import com.zionhuang.music.constants.ListItemHeight
import com.zionhuang.music.constants.SearchFilterHeight
import com.zionhuang.music.models.toMediaMetadata
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
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState(initial = false)
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState(initial = null)

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val items = viewModel.pagingData.collectAsLazyPagingItems()
    val searchFilter by viewModel.filter.observeAsState()

    LazyColumn(
        state = items.rememberLazyListState(),
        contentPadding = LocalPlayerAwareWindowInsets.current
            .add(WindowInsets(top = SearchFilterHeight))
            .asPaddingValues()
    ) {
        if (items.loadState.refresh !is LoadState.Loading) {
            items(items) { item ->
                when (item) {
                    is Header -> Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ListItemHeight)
                            .padding(12.dp)
                            .animateItemPlacement()
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 1
                        )
                    }
                    is YTItem -> YouTubeListItem(
                        item = item,
                        playingIndicator = when (item) {
                            is SongItem -> mediaMetadata?.id == item.id
                            is AlbumItem -> mediaMetadata?.album?.id == item.id
                            else -> false
                        },
                        playWhenReady = playWhenReady,
                        onShowMenu = {
                            menuState.show {
                                when (item) {
                                    is SongItem -> YouTubeSongMenu(
                                        song = item,
                                        navController = navController,
                                        playerConnection = playerConnection,
                                        onDismiss = menuState::dismiss
                                    )
                                    is AlbumItem -> YouTubeAlbumMenu(
                                        album = item,
                                        navController = navController,
                                        playerConnection = playerConnection,
                                        onDismiss = menuState::dismiss
                                    )
                                    is ArtistItem -> YouTubeArtistMenu(
                                        artist = item,
                                        playerConnection = playerConnection,
                                        onDismiss = menuState::dismiss
                                    )
                                    is PlaylistItem -> {}
                                }
                            }
                        },
                        modifier = Modifier
                            .clickable {
                                when (item) {
                                    is SongItem -> playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = item.id), item.toMediaMetadata()))
                                    is AlbumItem -> navController.navigate("album/${item.id}?playlistId=${item.playlistId}")
                                    is ArtistItem -> navController.navigate("artist/${item.id}")
                                    is PlaylistItem -> {}
                                }
                            }
                            .animateItemPlacement()
                    )
                    else -> {}
                }
            }

            if (items.itemCount == 0) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.no_results_found),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        if (items.loadState.refresh is LoadState.Loading || items.loadState.append is LoadState.Loading) {
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
    }

    Row(
        modifier = Modifier
            .padding(WindowInsets.systemBars
                .add(WindowInsets(top = AppBarHeight))
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