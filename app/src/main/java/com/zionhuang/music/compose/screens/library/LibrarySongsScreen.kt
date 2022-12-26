package com.zionhuang.music.compose.screens.library

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerAwareWindowInsets
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.*
import com.zionhuang.music.constants.*
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.mutablePreferenceState
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.models.sortInfo.PlaylistSortInfoPreference
import com.zionhuang.music.models.sortInfo.SongSortType
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.PlayerConnection
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibrarySongsScreen(
    navController: NavController,
    viewModel: SongsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState(initial = false)
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState(initial = null)

    val (sortType, onSortTypeChange) = mutablePreferenceState(SONG_SORT_TYPE, SongSortType.CREATE_DATE)
    val (sortDescending, onSortDescendingChange) = mutablePreferenceState(SONG_SORT_DESCENDING, true)
    val (sortTypeMenuExpanded, onSortTypeMenuExpandedChange) = remember { mutableStateOf(false) }

    val items by viewModel.allSongsFlow.collectAsState()

    val queueTitle = stringResource(R.string.queue_all_songs)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = WindowInsets.systemBars
                .add(WindowInsets(
                    top = AppBarHeight,
                    bottom = NavigationBarHeight + MiniPlayerHeight
                ))
                .asPaddingValues()
        ) {
            item(
                key = "header",
                contentType = CONTENT_TYPE_HEADER
            ) {
                SongHeader(
                    sortType = sortType,
                    onSortTypeChange = onSortTypeChange,
                    sortDescending = sortDescending,
                    onSortDescendingChange = onSortDescendingChange,
                    menuExpanded = sortTypeMenuExpanded,
                    onMenuExpandedChange = onSortTypeMenuExpandedChange,
                    songCount = items.size
                )
            }

            itemsIndexed(
                items = items,
                key = { _, item -> item.id },
                contentType = { _, _ -> CONTENT_TYPE_SONG }
            ) { index, song ->
                SongListItem(
                    song = song,
                    playingIndicator = song.id == mediaMetadata?.id,
                    playWhenReady = playWhenReady,
                    onShowMenu = {
                        menuState.show {
                            SongMenu(
                                originalSong = song,
                                navController = navController,
                                playerConnection = playerConnection,
                                coroutineScope = coroutineScope,
                                onDismiss = menuState::dismiss
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable {
                            playerConnection.playQueue(ListQueue(
                                title = queueTitle,
                                items = items.map { it.toMediaItem() },
                                startIndex = index
                            ))
                        }
                        .animateItemPlacement()
                )
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                    .asPaddingValues())
                .padding(16.dp),
            onClick = {
                playerConnection.playQueue(ListQueue(
                    title = queueTitle,
                    items = items.shuffled().map { it.toMediaItem() },
                ))
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_shuffle),
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SongHeader(
    sortType: SongSortType,
    onSortTypeChange: (SongSortType) -> Unit,
    sortDescending: Boolean,
    onSortDescendingChange: (Boolean) -> Unit,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    songCount: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(when (sortType) {
                SongSortType.CREATE_DATE -> R.string.sort_by_create_date
                SongSortType.NAME -> R.string.sort_by_name
                SongSortType.ARTIST -> R.string.sort_by_artist
                SongSortType.PLAY_TIME -> R.string.sort_by_play_time
            }),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false)
                ) {
                    onMenuExpandedChange(!menuExpanded)
                }
                .padding(8.dp)
        )

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { onMenuExpandedChange(false) },
            modifier = Modifier.widthIn(min = 172.dp)
        ) {
            listOf(
                SongSortType.CREATE_DATE to R.string.sort_by_create_date,
                SongSortType.NAME to R.string.sort_by_name,
                SongSortType.ARTIST to R.string.sort_by_artist,
                SongSortType.PLAY_TIME to R.string.sort_by_play_time
            ).forEach { (type, text) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(text),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(if (sortType == type) R.drawable.ic_radio_button_checked else R.drawable.ic_radio_button_unchecked),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onSortTypeChange(type)
                        onMenuExpandedChange(false)
                    }
                )
            }
        }

        ResizableIconButton(
            icon = if (sortDescending) R.drawable.ic_arrow_downward else R.drawable.ic_arrow_upward,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(32.dp)
                .padding(8.dp),
            onClick = { onSortDescendingChange(!sortDescending) }
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = pluralStringResource(R.plurals.song_count, songCount, songCount),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun SongMenu(
    originalSong: Song,
    navController: NavController,
    playerConnection: PlayerConnection,
    coroutineScope: CoroutineScope,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val songRepository = SongRepository(context)
    val songState = songRepository.getSongById(originalSong.id).flow.collectAsState(initial = originalSong)
    val song = songState.value ?: originalSong

    var showChoosePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showCreatePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showSelectArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var playlists by remember {
        mutableStateOf(emptyList<Playlist>())
    }

    LaunchedEffect(Unit) {
        SongRepository(context).getAllPlaylists(PlaylistSortInfoPreference).flow.collect {
            playlists = it
        }
    }

    if (showChoosePlaylistDialog) {
        ListDialog(
            onDismiss = { showChoosePlaylistDialog = false }
        ) {
            item {
                ListItem(
                    title = stringResource(R.string.dialog_title_create_playlist),
                    thumbnailDrawable = R.drawable.ic_add,
                    showMenuButton = false,
                    modifier = Modifier.clickable {
                        showCreatePlaylistDialog = true
                    }
                )
            }

            items(playlists) { playlist ->
                PlaylistListItem(
                    playlist = playlist,
                    showMenuButton = false,
                    modifier = Modifier.clickable {
                        showChoosePlaylistDialog = false
                        onDismiss()
                        coroutineScope.launch {
                            SongRepository(context).addToPlaylist(playlist.playlist, song)
                        }
                    }
                )
            }
        }
    }

    if (showCreatePlaylistDialog) {
        TextFieldDialog(
            icon = { Icon(painter = painterResource(R.drawable.ic_add), contentDescription = null) },
            title = { Text(text = stringResource(R.string.dialog_title_create_playlist)) },
            onDismiss = { showCreatePlaylistDialog = false },
            onDone = { playlistName ->
                coroutineScope.launch {
                    SongRepository(context).insertPlaylist(PlaylistEntity(
                        name = playlistName
                    ))
                }
            }
        )
    }

    if (showSelectArtistDialog) {
        ListDialog(
            onDismiss = { showSelectArtistDialog = false }
        ) {
            items(
                items = song.artists,
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
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = artist.thumbnailUrl,
                            contentDescription = null,
                            placeholder = painterResource(R.drawable.ic_artist),
                            error = painterResource(R.drawable.ic_artist),
                            modifier = Modifier
                                .size(ListThumbnailSize)
                                .clip(CircleShape)
                        )
                    }
                    Text(
                        text = artist.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }

    SongListItem(
        song = song,
        showMenuButton = false,
        trailingContent = {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        songRepository.toggleLiked(song)
                    }
                }
            ) {
                Icon(
                    painter = painterResource(if (song.song.liked) R.drawable.ic_favorite else R.drawable.ic_favorite_border),
                    tint = MaterialTheme.colorScheme.error,
                    contentDescription = null
                )
            }
        }
    )

    Divider()

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
            icon = R.drawable.ic_edit,
            title = R.string.menu_edit,
            enabled = false
        ) {

        }
        GridMenuItem(
            icon = R.drawable.ic_playlist_add,
            title = R.string.menu_add_to_playlist
        ) {
            showChoosePlaylistDialog = true
        }
        GridMenuItem(
            icon = R.drawable.ic_file_download,
            title = R.string.menu_download,
            enabled = false
        ) {

        }
        GridMenuItem(
            icon = R.drawable.ic_artist,
            title = R.string.menu_view_artist
        ) {
            if (song.artists.size == 1) {
                navController.navigate("artist/${song.artists[0].id}")
                onDismiss()
            } else {
                showSelectArtistDialog = true
            }
        }
        if (song.song.albumId != null) {
            GridMenuItem(
                icon = R.drawable.ic_album,
                title = R.string.menu_view_album
            ) {
                navController.navigate("album/${song.song.albumId}")
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
                putExtra(Intent.EXTRA_TEXT, "https://music.youtube.com/watch?v=${song.id}")
            }
            context.startActivity(Intent.createChooser(intent, null))
            onDismiss()
        }
        GridMenuItem(
            icon = R.drawable.ic_cached,
            title = R.string.menu_refetch
        ) {
            coroutineScope.launch {
                songRepository.refetchSong(song)
            }
            onDismiss()
        }
        GridMenuItem(
            icon = R.drawable.ic_delete,
            title = R.string.menu_delete
        ) {
            coroutineScope.launch {
                songRepository.deleteSong(song)
            }
            onDismiss()
        }
    }
}