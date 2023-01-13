package com.zionhuang.music.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zionhuang.innertube.models.*
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.GridThumbnailHeight
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.ui.component.AppBarConfig
import com.zionhuang.music.ui.component.LocalMenuState
import com.zionhuang.music.ui.component.YouTubeGridItem
import com.zionhuang.music.ui.component.YouTubeListItem
import com.zionhuang.music.ui.component.shimmer.GridItemPlaceHolder
import com.zionhuang.music.ui.component.shimmer.ListItemPlaceHolder
import com.zionhuang.music.ui.component.shimmer.ShimmerHost
import com.zionhuang.music.ui.menu.YouTubeAlbumMenu
import com.zionhuang.music.ui.menu.YouTubeArtistMenu
import com.zionhuang.music.ui.menu.YouTubeSongMenu
import com.zionhuang.music.viewmodels.ArtistItemsViewModel
import com.zionhuang.music.viewmodels.MainViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistItemsScreen(
    navController: NavController,
    appBarConfig: AppBarConfig,
    viewModel: ArtistItemsViewModel = viewModel(),
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

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val title by viewModel.title.collectAsState()
    val itemsPage by viewModel.itemsPage.collectAsState()

    LaunchedEffect(title) {
        appBarConfig.title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
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

    if (itemsPage?.items?.firstOrNull() is AlbumItem) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = GridThumbnailHeight + 24.dp),
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
        ) {
            items(
                items = itemsPage?.items.orEmpty(),
                key = { it.id }
            ) { item ->
                YouTubeGridItem(
                    item = item,
                    badges = {
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
                    fillMaxWidth = true,
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {
                                when (item) {
                                    is SongItem -> playerConnection.playQueue(YouTubeQueue(item.endpoint ?: WatchEndpoint(videoId = item.id), item.toMediaMetadata()))
                                    is AlbumItem -> navController.navigate("album/${item.id}?playlistId=${item.playlistId}")
                                    is ArtistItem -> navController.navigate("artist/${item.id}")
                                    is PlaylistItem -> {}
                                }
                            },
                            onLongClick = {
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
                        )
                )
            }

            if (itemsPage?.continuation != null) {
                item(key = "loading") {
                    ShimmerHost {
                        repeat(4) {
                            GridItemPlaceHolder()
                        }
                    }
                }
            }
        }
    } else {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
        ) {
            items(
                items = itemsPage?.items.orEmpty(),
                key = { it.id }
            ) { item ->
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
                                is SongItem -> playerConnection.playQueue(YouTubeQueue(item.endpoint ?: WatchEndpoint(videoId = item.id), item.toMediaMetadata()))
                                is AlbumItem -> navController.navigate("album/${item.id}?playlistId=${item.playlistId}")
                                is ArtistItem -> navController.navigate("artist/${item.id}")
                                is PlaylistItem -> {}
                            }
                        }
                )
            }

            if (itemsPage == null) {
                item {
                    ShimmerHost {
                        repeat(8) {
                            ListItemPlaceHolder()
                        }
                    }
                }
            }

            if (itemsPage?.continuation != null) {
                item(key = "loading") {
                    ShimmerHost {
                        repeat(3) {
                            ListItemPlaceHolder()
                        }
                    }
                }
            }
        }
    }
}
