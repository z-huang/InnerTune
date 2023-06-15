package com.zionhuang.music.ui.screens.playlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastSumBy
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.utils.completed
import com.zionhuang.music.LocalDatabase
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.AlbumThumbnailSize
import com.zionhuang.music.constants.ThumbnailCornerRadius
import com.zionhuang.music.db.entities.PlaylistSongMap
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.ui.component.*
import com.zionhuang.music.ui.menu.SongMenu
import com.zionhuang.music.ui.utils.reordering.*
import com.zionhuang.music.utils.makeTimeString
import com.zionhuang.music.viewmodels.LocalPlaylistViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LocalPlaylistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: LocalPlaylistViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val playlist by viewModel.playlist.collectAsState()
    val songs by viewModel.playlistSongs.collectAsState()
    val playlistLength = remember(songs) {
        songs.fastSumBy { it.song.duration }
    }

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val showTopBarTitle by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0
        }
    }

    var showEditDialog by remember {
        mutableStateOf(false)
    }

    if (showEditDialog) {
        playlist?.playlist?.let { playlistEntity ->
            TextFieldDialog(
                icon = { Icon(painter = painterResource(R.drawable.ic_edit), contentDescription = null) },
                title = { Text(text = stringResource(R.string.edit_playlist)) },
                onDismiss = { showEditDialog = false },
                initialTextFieldValue = TextFieldValue(playlistEntity.name, TextRange(playlistEntity.name.length)),
                onDone = { name ->
                    database.query {
                        update(playlistEntity.copy(name = name))
                    }
                }
            )
        }
    }

    val reorderingState = rememberReorderingState(
        lazyListState = lazyListState,
        key = songs,
        onDragEnd = { fromIndex, toIndex ->
            database.query {
                move(viewModel.playlistId, fromIndex, toIndex)
            }
        },
        extraItemCount = 1
    )

    ReorderingLazyColumn(
        reorderingState = reorderingState,
        contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
    ) {
        playlist?.let { playlist ->
            item {
                if (playlist.songCount == 0) {
                    EmptyPlaceholder(
                        icon = R.drawable.ic_music_note,
                        text = stringResource(R.string.playlist_is_empty)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (playlist.thumbnails.size == 1) {
                                AsyncImage(
                                    model = playlist.thumbnails[0],
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(AlbumThumbnailSize)
                                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                )
                            } else if (playlist.thumbnails.size > 1) {
                                Box(
                                    modifier = Modifier
                                        .size(AlbumThumbnailSize)
                                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                ) {
                                    listOf(
                                        Alignment.TopStart,
                                        Alignment.TopEnd,
                                        Alignment.BottomStart,
                                        Alignment.BottomEnd
                                    ).fastForEachIndexed { index, alignment ->
                                        AsyncImage(
                                            model = playlist.thumbnails.getOrNull(index),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .align(alignment)
                                                .size(AlbumThumbnailSize / 2)
                                        )
                                    }
                                }
                            }

                            Column(
                                verticalArrangement = Arrangement.Center,
                            ) {
                                AutoResizeText(
                                    text = playlist.playlist.name,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSizeRange = FontSizeRange(16.sp, 22.sp)
                                )

                                Text(
                                    text = pluralStringResource(R.plurals.n_song, playlist.songCount, playlist.songCount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Normal
                                )

                                Text(
                                    text = makeTimeString(playlistLength * 1000L),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Normal
                                )

                                Row {
                                    IconButton(
                                        onClick = { showEditDialog = true }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_edit),
                                            contentDescription = null
                                        )
                                    }

                                    if (playlist.playlist.browseId != null) {
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    val playlistPage = YouTube.playlist(playlist.playlist.browseId).completed().getOrNull() ?: return@launch
                                                    database.transaction {
                                                        clearPlaylist(playlist.id)
                                                        playlistPage.songs
                                                            .map(SongItem::toMediaMetadata)
                                                            .onEach(::insert)
                                                            .mapIndexed { position, song ->
                                                                PlaylistSongMap(
                                                                    songId = song.id,
                                                                    playlistId = playlist.id,
                                                                    position = position
                                                                )
                                                            }
                                                            .forEach(::insert)
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_sync),
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    playerConnection.playQueue(
                                        ListQueue(
                                            title = playlist.playlist.name,
                                            items = songs.map(Song::toMediaItem)
                                        )
                                    )
                                },
                                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_play),
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text(stringResource(R.string.play))
                            }

                            OutlinedButton(
                                onClick = {
                                    playerConnection.playQueue(
                                        ListQueue(
                                            title = playlist.playlist.name,
                                            items = songs.shuffled().map(Song::toMediaItem)
                                        )
                                    )
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
                        }
                    }
                }
            }
        }

        itemsIndexed(
            items = songs
        ) { index, song ->
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

                    IconButton(
                        onClick = { },
                        modifier = Modifier.reorder(reorderingState = reorderingState, index = index)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_drag_handle),
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable {
                        playerConnection.playQueue(
                            ListQueue(
                                title = playlist!!.playlist.name,
                                items = songs.map(Song::toMediaItem),
                                startIndex = index
                            )
                        )
                    }
                    .animateItemPlacement(reorderingState = reorderingState)
                    .draggedItem(reorderingState = reorderingState, index = index)
            )
        }
    }

    TopAppBar(
        title = { if (showTopBarTitle) Text(playlist?.playlist?.name.orEmpty()) },
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
}
