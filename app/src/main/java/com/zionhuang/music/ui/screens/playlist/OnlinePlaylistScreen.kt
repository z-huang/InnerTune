package com.zionhuang.music.ui.screens.playlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.music.LocalDatabase
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.AlbumThumbnailSize
import com.zionhuang.music.constants.ThumbnailCornerRadius
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.PlaylistSongMap
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.ui.component.AutoResizeText
import com.zionhuang.music.ui.component.FontSizeRange
import com.zionhuang.music.ui.component.LocalMenuState
import com.zionhuang.music.ui.component.YouTubeListItem
import com.zionhuang.music.ui.component.shimmer.ButtonPlaceholder
import com.zionhuang.music.ui.component.shimmer.ListItemPlaceHolder
import com.zionhuang.music.ui.component.shimmer.ShimmerHost
import com.zionhuang.music.ui.component.shimmer.TextPlaceholder
import com.zionhuang.music.ui.menu.YouTubeSongMenu
import com.zionhuang.music.viewmodels.MainViewModel
import com.zionhuang.music.viewmodels.OnlinePlaylistViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnlinePlaylistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: OnlinePlaylistViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val librarySongIds by mainViewModel.librarySongIds.collectAsState()
    val likedSongIds by mainViewModel.likedSongIds.collectAsState()

    val playlist by viewModel.playlist.collectAsState()
    val songs by viewModel.playlistSongs.collectAsState()

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val showTopBarTitle by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
        ) {
            playlist.let { playlist ->
                if (playlist != null) {
                    item {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = playlist.thumbnail,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(AlbumThumbnailSize)
                                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                )

                                Spacer(Modifier.width(16.dp))

                                Column(
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    AutoResizeText(
                                        text = playlist.title,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSizeRange = FontSizeRange(16.sp, 22.sp)
                                    )

                                    val annotatedString = buildAnnotatedString {
                                        withStyle(
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onBackground
                                            ).toSpanStyle()
                                        ) {
                                            if (playlist.author.id != null) {
                                                pushStringAnnotation(playlist.author.id!!, playlist.author.name)
                                                append(playlist.author.name)
                                                pop()
                                            } else {
                                                append(playlist.author.name)
                                            }
                                        }
                                    }
                                    ClickableText(annotatedString) { offset ->
                                        annotatedString.getStringAnnotations(offset, offset).firstOrNull()?.let { range ->
                                            navController.navigate("artist/${range.tag}")
                                        }
                                    }

                                    playlist.songCountText?.let { songCountText ->
                                        Text(
                                            text = songCountText,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                database.transaction {
                                                    val playlistEntity = PlaylistEntity(name = playlist.title)
                                                    insert(playlistEntity)
                                                    songs.map(SongItem::toMediaMetadata)
                                                        .onEach(::insert)
                                                        .mapIndexed { index, song ->
                                                            PlaylistSongMap(
                                                                songId = song.id,
                                                                playlistId = playlistEntity.id,
                                                                position = index
                                                            )
                                                        }
                                                        .forEach(::insert)
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(context.getString(R.string.snackbar_playlist_imported))
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_input),
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        playerConnection.playQueue(YouTubeQueue(playlist.shuffleEndpoint))
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
                                    Text(stringResource(R.string.shuffle))
                                }

                                OutlinedButton(
                                    onClick = {
                                        playerConnection.playQueue(YouTubeQueue(playlist.radioEndpoint))
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
                                    Text(stringResource(R.string.radio))
                                }
                            }
                        }
                    }

                    items(
                        items = songs,
                        key = { it.id }
                    ) { song ->
                        YouTubeListItem(
                            item = song,
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
                                    playerConnection.playQueue(YouTubeQueue(song.endpoint ?: WatchEndpoint(videoId = song.id), song.toMediaMetadata()))
                                }
                                .animateItemPlacement()
                        )
                    }
                } else {
                    item {
                        ShimmerHost {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(
                                        modifier = Modifier
                                            .size(AlbumThumbnailSize)
                                            .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                            .background(MaterialTheme.colorScheme.onSurface)
                                    )

                                    Spacer(Modifier.width(16.dp))

                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        TextPlaceholder()
                                        TextPlaceholder()
                                        TextPlaceholder()
                                    }
                                }

                                Spacer(Modifier.padding(8.dp))

                                Row {
                                    ButtonPlaceholder(Modifier.weight(1f))

                                    Spacer(Modifier.width(12.dp))

                                    ButtonPlaceholder(Modifier.weight(1f))
                                }
                            }

                            repeat(6) {
                                ListItemPlaceHolder()
                            }
                        }
                    }
                }
            }
        }

        TopAppBar(
            title = { if (showTopBarTitle) Text(playlist?.title.orEmpty()) },
            navigationIcon = {
                IconButton(onClick = navController::navigateUp) {
                    Icon(
                        painterResource(R.drawable.ic_arrow_back),
                        contentDescription = null
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
                .align(Alignment.BottomCenter)
        )
    }
}
