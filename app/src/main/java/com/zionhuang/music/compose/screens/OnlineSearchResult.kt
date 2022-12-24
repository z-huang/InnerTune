package com.zionhuang.music.compose.screens

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.zionhuang.music.compose.component.*
import com.zionhuang.music.compose.component.shimmer.ListItemPlaceHolder
import com.zionhuang.music.compose.component.shimmer.ShimmerHost
import com.zionhuang.music.compose.utils.rememberLazyListState
import com.zionhuang.music.constants.*
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.PlayerConnection
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
        contentPadding = WindowInsets.systemBars
            .add(WindowInsets(
                top = AppBarHeight + SearchFilterHeight,
                bottom = NavigationBarHeight + MiniPlayerHeight
            ))
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

@Composable
fun YouTubeSongMenu(
    song: SongItem,
    navController: NavController,
    playerConnection: PlayerConnection,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val artists = remember {
        song.artists.mapNotNull {
            it.navigationEndpoint?.browseEndpoint?.browseId?.let { artistId ->
                MediaMetadata.Artist(id = artistId, name = it.text)
            }
        }
    }

    var showSelectArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSelectArtistDialog) {
        ListDialog(
            onDismiss = { showSelectArtistDialog = false }
        ) {
            items(
                items = artists,
                key = { it.id }
            ) { artist ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(ListItemHeight)
                        .clickable {
                            navController.navigate("artist/${artist.id}")
                            showSelectArtistDialog = false
                            onDismiss()
                        }
                        .padding(horizontal = 12.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .height(ListItemHeight)
                            .clickable {
                                navController.navigate("artist/${artist.id}")
                                showSelectArtistDialog = false
                                onDismiss()
                            }
                            .padding(horizontal = 24.dp),
                    ) {
                        Text(
                            text = artist.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    GridMenu(
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        GridMenuItem(
            icon = R.drawable.ic_radio,
            title = R.string.menu_start_radio
        ) {
            playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = song.id), song.toMediaMetadata()))
            onDismiss()
        }
        GridMenuItem(
            icon = R.drawable.ic_playlist_play,
            title = R.string.menu_play_next
        ) {
            playerConnection.playNext(song.toMediaItem())
            onDismiss()
        }
        GridMenuItem(
            icon = R.drawable.ic_queue_music,
            title = R.string.menu_add_to_queue
        ) {
            playerConnection.addToQueue((song.toMediaItem()))
            onDismiss()
        }
        GridMenuItem(
            icon = R.drawable.ic_library_add,
            title = R.string.action_add_to_library
        ) {

        }
        GridMenuItem(
            icon = R.drawable.ic_playlist_add,
            title = R.string.menu_add_to_playlist
        ) {

        }
        GridMenuItem(
            icon = R.drawable.ic_file_download,
            title = R.string.menu_download
        ) {

        }
        if (artists.isNotEmpty()) {
            GridMenuItem(
                icon = R.drawable.ic_artist,
                title = R.string.menu_view_artist
            ) {
                if (artists.size == 1) {
                    navController.navigate("artist/${artists[0].id}")
                    onDismiss()
                } else {
                    showSelectArtistDialog = true
                }
            }
        }
        song.album?.let { album ->
            GridMenuItem(
                icon = R.drawable.ic_album,
                title = R.string.menu_view_album
            ) {
                navController.navigate("album/${album.navigationEndpoint.browseId}")
                onDismiss()
            }
        }
        GridMenuItem(
            icon = R.drawable.ic_share,
            title = R.string.menu_share
        ) {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, song.shareLink)
            }
            context.startActivity(Intent.createChooser(intent, null))
            onDismiss()
        }
    }
}

@Composable
fun YouTubeAlbumMenu(
    album: AlbumItem,
    navController: NavController,
    playerConnection: PlayerConnection,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    GridMenu(
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        album.menu.radioEndpoint?.watchPlaylistEndpoint?.toWatchEndpoint()?.let { watchEndpoint ->
            GridMenuItem(
                icon = R.drawable.ic_radio,
                title = R.string.menu_start_radio
            ) {
                playerConnection.playQueue(YouTubeQueue(watchEndpoint))
                onDismiss()
            }
        }
        GridMenuItem(
            icon = R.drawable.ic_playlist_play,
            title = R.string.menu_play_next
        ) {
            onDismiss()
        }
        GridMenuItem(
            icon = R.drawable.ic_queue_music,
            title = R.string.menu_add_to_queue
        ) {
            onDismiss()
        }
        GridMenuItem(
            icon = R.drawable.ic_library_add,
            title = R.string.action_add_to_library
        ) {

        }
        GridMenuItem(
            icon = R.drawable.ic_playlist_add,
            title = R.string.menu_add_to_playlist
        ) {

        }
        GridMenuItem(
            icon = R.drawable.ic_file_download,
            title = R.string.menu_download
        ) {

        }
        album.menu.artistEndpoint?.browseEndpoint?.let { browseEndpoint ->
            GridMenuItem(
                icon = R.drawable.ic_artist,
                title = R.string.menu_view_artist
            ) {
                navController.navigate("artist/${browseEndpoint.browseId}")
                onDismiss()
            }
        }
        GridMenuItem(
            icon = R.drawable.ic_share,
            title = R.string.menu_share
        ) {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, album.shareLink)
            }
            context.startActivity(Intent.createChooser(intent, null))
            onDismiss()
        }
    }
}

@Composable
fun YouTubeArtistMenu(
    artist: ArtistItem,
    playerConnection: PlayerConnection,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    GridMenu(
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        artist.menu.radioEndpoint?.watchPlaylistEndpoint?.toWatchEndpoint()?.let { watchEndpoint ->
            GridMenuItem(
                icon = R.drawable.ic_radio,
                title = R.string.menu_start_radio
            ) {
                playerConnection.playQueue(YouTubeQueue(watchEndpoint))
                onDismiss()
            }
        }
        artist.menu.shuffleEndpoint?.watchPlaylistEndpoint?.toWatchEndpoint()?.let { watchEndpoint ->
            GridMenuItem(
                icon = R.drawable.ic_shuffle,
                title = R.string.btn_shuffle
            ) {
                playerConnection.playQueue(YouTubeQueue(watchEndpoint))
                onDismiss()
            }
        }
        GridMenuItem(
            icon = R.drawable.ic_share,
            title = R.string.menu_share
        ) {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, artist.shareLink)
            }
            context.startActivity(Intent.createChooser(intent, null))
            onDismiss()
        }
    }
}