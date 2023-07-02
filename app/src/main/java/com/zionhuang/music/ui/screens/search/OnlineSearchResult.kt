package com.zionhuang.music.ui.screens.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ALBUM
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ARTIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_COMMUNITY_PLAYLIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_FEATURED_PLAYLIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_VIDEO
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.ArtistItem
import com.zionhuang.innertube.models.PlaylistItem
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.innertube.models.YTItem
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.AppBarHeight
import com.zionhuang.music.constants.ListItemHeight
import com.zionhuang.music.constants.SearchFilterHeight
import com.zionhuang.music.extensions.togglePlayPause
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.ui.component.EmptyPlaceholder
import com.zionhuang.music.ui.component.LocalMenuState
import com.zionhuang.music.ui.component.YouTubeListItem
import com.zionhuang.music.ui.component.shimmer.ListItemPlaceHolder
import com.zionhuang.music.ui.component.shimmer.ShimmerHost
import com.zionhuang.music.ui.menu.YouTubeAlbumMenu
import com.zionhuang.music.ui.menu.YouTubeArtistMenu
import com.zionhuang.music.ui.menu.YouTubePlaylistMenu
import com.zionhuang.music.ui.menu.YouTubeSongMenu
import com.zionhuang.music.viewmodels.OnlineSearchViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OnlineSearchResult(
    navController: NavController,
    viewModel: OnlineSearchViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

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

                                is PlaylistItem -> YouTubePlaylistMenu(
                                    playlist = item,
                                    playerConnection = playerConnection,
                                    coroutineScope = coroutineScope,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert),
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier
                .clickable {
                    when (item) {
                        is SongItem -> {
                            if (item.id == mediaMetadata?.id) {
                                playerConnection.player.togglePlayPause()
                            } else {
                                playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = item.id), item.toMediaMetadata()))
                            }
                        }

                        is AlbumItem -> navController.navigate("album/${item.id}")
                        is ArtistItem -> navController.navigate("artist/${item.id}")
                        is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
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
                    key = { "${summary.title}/${it.id}" },
                    itemContent = ytItemContent
                )
            }

            if (searchSummary?.summaries?.isEmpty() == true) {
                item {
                    EmptyPlaceholder(
                        icon = R.drawable.search,
                        text = stringResource(R.string.no_results_found)
                    )
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
                    EmptyPlaceholder(
                        icon = R.drawable.search,
                        text = stringResource(R.string.no_results_found)
                    )
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
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(top = AppBarHeight)
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.width(8.dp))

        listOf(
            null to R.string.filter_all,
            FILTER_SONG to R.string.filter_songs,
            FILTER_VIDEO to R.string.filter_videos,
            FILTER_ALBUM to R.string.filter_albums,
            FILTER_ARTIST to R.string.filter_artists,
            FILTER_COMMUNITY_PLAYLIST to R.string.filter_community_playlists,
            FILTER_FEATURED_PLAYLIST to R.string.filter_featured_playlists
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
