package com.zionhuang.music.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ALBUM
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ARTIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_COMMUNITY_PLAYLIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_FEATURED_PLAYLIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_VIDEO
import com.zionhuang.innertube.models.*
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.AppBarHeight
import com.zionhuang.music.constants.ListItemHeight
import com.zionhuang.music.constants.SearchFilterHeight
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.ui.component.LocalMenuState
import com.zionhuang.music.ui.component.NoResultFound
import com.zionhuang.music.ui.component.YouTubeListItem
import com.zionhuang.music.ui.component.shimmer.ListItemPlaceHolder
import com.zionhuang.music.ui.component.shimmer.ShimmerHost
import com.zionhuang.music.ui.menu.YouTubeAlbumMenu
import com.zionhuang.music.ui.menu.YouTubeArtistMenu
import com.zionhuang.music.ui.menu.YouTubeSongMenu
import com.zionhuang.music.viewmodels.MainViewModel
import com.zionhuang.music.viewmodels.OnlineSearchViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OnlineSearchResult(
    navController: NavController,
    viewModel: OnlineSearchViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel(),
) {
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val librarySongIds by mainViewModel.librarySongIds.collectAsState()
    val likedSongIds by mainViewModel.likedSongIds.collectAsState()
    val libraryAlbumIds by mainViewModel.libraryAlbumIds.collectAsState()
    val libraryPlaylistIds by mainViewModel.libraryPlaylistIds.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val searchFilter by viewModel.filter.collectAsState()
    val searchSummary = viewModel.summaryPage
    val itemsPage by remember(searchFilter) {
        derivedStateOf {
            searchFilter?.value?.let {
                viewModel.viewStateMap[it]
            }
        }
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.layoutInfo.visibleItemsInfo.any { it.key == "loading" }
        }.collect { shouldLoadMore ->
            if (!shouldLoadMore) return@collect
            viewModel.loadMore()
        }
    }

    val ytItemContent: @Composable LazyItemScope.(YTItem) -> Unit = { item: YTItem ->
        YouTubeListItem(
            item = item,
            badges = {
                if (item.explicit) {
                    Icon(
                        painter = painterResource(R.drawable.ic_explicit),
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 2.dp)
                    )
                }
                if (item is SongItem && item.id in librarySongIds ||
                    item is AlbumItem && item.id in libraryAlbumIds ||
                    item is PlaylistItem && item.id in libraryPlaylistIds
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_library_add_check),
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 2.dp)
                    )
                }
                if (item is SongItem && item.id in likedSongIds) {
                    Icon(
                        painter = painterResource(R.drawable.ic_favorite),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 2.dp)
                    )
                }
            },
            isPlaying = when (item) {
                is SongItem -> mediaMetadata?.id == item.id
                is AlbumItem -> mediaMetadata?.album?.id == item.id
                else -> false
            },
            playWhenReady = playWhenReady,
            trailingContent = {
                IconButton(
                    onClick = {
                        menuState.show {
                            when (item) {
                                is SongItem -> YouTubeSongMenu(
                                    song = item,
                                    navController = navController,
                                    playerConnection = playerConnection,
                                    coroutineScope = coroutineScope,
                                    onDismiss = menuState::dismiss
                                )
                                is AlbumItem -> YouTubeAlbumMenu(
                                    album = item,
                                    navController = navController,
                                    playerConnection = playerConnection,
                                    coroutineScope = coroutineScope,
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
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = null
                    )
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
    }

    LazyColumn(
        state = lazyListState,
        contentPadding = LocalPlayerAwareWindowInsets.current
            .add(WindowInsets(top = SearchFilterHeight))
            .asPaddingValues()
    ) {
        if (searchFilter == null) {
            searchSummary?.summaries?.forEach { summary ->
                item {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ListItemHeight)
                            .padding(12.dp)
                            .animateItemPlacement()
                    ) {
                        Text(
                            text = summary.title,
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 1
                        )
                    }
                }

                items(
                    items = summary.items,
                    key = { it.id },
                    itemContent = ytItemContent
                )
            }

            if (searchSummary?.summaries?.isEmpty() == true) {
                item {
                    NoResultFound()
                }
            }
        } else {
            items(
                items = itemsPage?.items.orEmpty(),
                key = { it.id },
                itemContent = ytItemContent
            )

            if (itemsPage?.continuation != null) {
                item(key = "loading") {
                    ShimmerHost {
                        repeat(3) {
                            ListItemPlaceHolder()
                        }
                    }
                }
            }

            if (itemsPage?.items?.isEmpty() == true) {
                item {
                    NoResultFound()
                }
            }
        }

        if (searchFilter == null && searchSummary == null || searchFilter != null && itemsPage == null) {
            item {
                ShimmerHost {
                    repeat(8) {
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
                    if (viewModel.filter.value != filter) {
                        viewModel.filter.value = filter
                    }
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(0)
                    }
                }
            )
            Spacer(Modifier.width(8.dp))
        }
    }
}
