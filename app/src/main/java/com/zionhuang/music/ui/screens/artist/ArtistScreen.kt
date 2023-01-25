package com.zionhuang.music.ui.screens.artist

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer
import com.zionhuang.innertube.models.*
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.AppBarHeight
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.ui.component.*
import com.zionhuang.music.ui.component.shimmer.ButtonPlaceholder
import com.zionhuang.music.ui.component.shimmer.ListItemPlaceHolder
import com.zionhuang.music.ui.component.shimmer.ShimmerHost
import com.zionhuang.music.ui.component.shimmer.TextPlaceholder
import com.zionhuang.music.ui.menu.SongMenu
import com.zionhuang.music.ui.menu.YouTubeAlbumMenu
import com.zionhuang.music.ui.menu.YouTubeArtistMenu
import com.zionhuang.music.ui.menu.YouTubeSongMenu
import com.zionhuang.music.ui.utils.fadingEdge
import com.zionhuang.music.ui.utils.resize
import com.zionhuang.music.viewmodels.ArtistViewModel
import com.zionhuang.music.viewmodels.MainViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistScreen(
    navController: NavController,
    appBarConfig: AppBarConfig,
    viewModel: ArtistViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val coroutineScope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val librarySongIds by mainViewModel.librarySongIds.collectAsState()
    val likedSongIds by mainViewModel.likedSongIds.collectAsState()
    val libraryAlbumIds by mainViewModel.libraryAlbumIds.collectAsState()
    val libraryPlaylistIds by mainViewModel.libraryPlaylistIds.collectAsState()

    val artistPage = viewModel.artistPage
    val librarySongs by viewModel.librarySongs.collectAsState()

    val lazyListState = rememberLazyListState()

    val transparentAppBar by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0
        }
    }
    LaunchedEffect(transparentAppBar) {
        appBarConfig.transparentBackground = transparentAppBar
    }
    LaunchedEffect(artistPage) {
        appBarConfig.title = {
            Text(
                text = if (!transparentAppBar) artistPage?.artist?.title.orEmpty() else "",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }

    LazyColumn(
        state = lazyListState,
        contentPadding = LocalPlayerAwareWindowInsets.current
            .add(WindowInsets(top = -WindowInsets.systemBars.asPaddingValues().calculateTopPadding() - AppBarHeight))
            .asPaddingValues()
    ) {
        artistPage.let {
            if (artistPage != null) {
                item(key = "header") {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3)
                        ) {
                            AsyncImage(
                                model = artistPage.artist.thumbnail.resize(1200, 900),
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fadingEdge(
                                        top = WindowInsets.systemBars
                                            .asPaddingValues()
                                            .calculateTopPadding() + AppBarHeight,
                                        bottom = 64.dp
                                    )
                            )
                            AutoResizeText(
                                text = artistPage.artist.title,
                                style = MaterialTheme.typography.displayLarge,
                                fontSizeRange = FontSizeRange(32.sp, 58.sp),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 48.dp)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            artistPage.artist.shuffleEndpoint?.let { shuffleEndpoint ->
                                Button(
                                    onClick = {
                                        playerConnection.playQueue(YouTubeQueue(shuffleEndpoint))
                                    },
                                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_shuffle),
                                        contentDescription = null,
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(
                                        text = stringResource(R.string.btn_shuffle)
                                    )
                                }
                            }

                            artistPage.artist.radioEndpoint?.let { radioEndpoint ->
                                OutlinedButton(
                                    onClick = {
                                        playerConnection.playQueue(YouTubeQueue(radioEndpoint))
                                    },
                                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_radio),
                                        contentDescription = null,
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(stringResource(R.string.btn_radio))
                                }
                            }
                        }
                    }
                }

                if (librarySongs.isNotEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("artistSongs/${artistPage.artist.id}")
                                }
                                .padding(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(R.string.header_from_your_library),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                            Icon(
                                painter = painterResource(R.drawable.ic_navigate_next),
                                contentDescription = null
                            )
                        }
                    }

                    items(
                        items = librarySongs,
                        key = { "local_${it.id}" }
                    ) { song ->
                        SongListItem(
                            song = song,
                            isPlaying = song.id == mediaMetadata?.id,
                            playWhenReady = playWhenReady,
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        menuState.show {
                                            SongMenu(
                                                originalSong = song,
                                                navController = navController,
                                                playerConnection = playerConnection,
                                                coroutineScope = coroutineScope,
                                                onDismiss = menuState::dismiss
                                            )
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
                                .fillMaxWidth()
                                .combinedClickable {
                                    playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = song.id), song.toMediaMetadata()))
                                }
                                .animateItemPlacement()
                        )
                    }
                }

                artistPage.sections.fastForEach { section ->
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = section.moreEndpoint != null) {
                                    navController.navigate("artistItems/${section.moreEndpoint?.browseId}?params=${section.moreEndpoint?.params}")
                                }
                                .padding(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                            if (section.moreEndpoint != null) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_navigate_next),
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    if ((section.items.firstOrNull() as? SongItem)?.album != null) {
                        items(
                            items = section.items,
                            key = { it.id }
                        ) { song ->
                            YouTubeListItem(
                                item = song as SongItem,
                                badges = {
                                    if (song.explicit) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_explicit),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .padding(end = 2.dp)
                                        )
                                    }
                                    if (song.id in librarySongIds) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_library_add_check),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .padding(end = 2.dp)
                                        )
                                    }
                                    if (song.id in likedSongIds) {
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
                                isPlaying = mediaMetadata?.id == song.id,
                                playWhenReady = playWhenReady,
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            menuState.show {
                                                YouTubeSongMenu(
                                                    song = song,
                                                    navController = navController,
                                                    playerConnection = playerConnection,
                                                    coroutineScope = coroutineScope,
                                                    onDismiss = menuState::dismiss
                                                )
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
                                        playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = song.id), song.toMediaMetadata()))
                                    }
                                    .animateItemPlacement()
                            )
                        }
                    } else {
                        item {
                            LazyRow {
                                items(
                                    items = section.items,
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
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    when (item) {
                                                        is SongItem -> playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = item.id), item.toMediaMetadata()))
                                                        is AlbumItem -> navController.navigate("album/${item.id}")
                                                        is ArtistItem -> navController.navigate("artist/${item.id}")
                                                        is PlaylistItem -> navController.navigate("playlist/${item.id}")
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
                                            .animateItemPlacement()
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item(key = "shimmer") {
                    ShimmerHost {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3)
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .shimmer()
                                    .background(MaterialTheme.colorScheme.onSurface)
                                    .fadingEdge(
                                        top = WindowInsets.systemBars
                                            .asPaddingValues()
                                            .calculateTopPadding() + AppBarHeight,
                                        bottom = 108.dp
                                    )
                            )
                            TextPlaceholder(
                                height = 56.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 48.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            ButtonPlaceholder(Modifier.weight(1f))

                            Spacer(Modifier.width(12.dp))

                            ButtonPlaceholder(Modifier.weight(1f))
                        }

                        repeat(6) {
                            ListItemPlaceHolder()
                        }
                    }
                }
            }
        }
    }
}
