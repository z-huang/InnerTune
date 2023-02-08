package com.zionhuang.music.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zionhuang.music.LocalDatabase
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.constants.*
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.DOWNLOADED_PLAYLIST_ID
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.LIKED_PLAYLIST_ID
import com.zionhuang.music.ui.component.*
import com.zionhuang.music.ui.menu.PlaylistMenu
import com.zionhuang.music.utils.rememberEnumPreference
import com.zionhuang.music.utils.rememberPreference
import com.zionhuang.music.viewmodels.LibraryPlaylistsViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun LibraryPlaylistsScreen(
    navController: NavController,
    viewModel: LibraryPlaylistsViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current

    val coroutineScope = rememberCoroutineScope()

    val (sortType, onSortTypeChange) = rememberEnumPreference(PlaylistSortTypeKey, PlaylistSortType.CREATE_DATE)
    val (sortDescending, onSortDescendingChange) = rememberPreference(PlaylistSortDescendingKey, true)

    val likedSongCount by viewModel.likedSongCount.collectAsState()
    val downloadedSongCount by viewModel.downloadedSongCount.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()

    var showAddPlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showAddPlaylistDialog) {
        TextFieldDialog(
            icon = { Icon(painter = painterResource(R.drawable.ic_add), contentDescription = null) },
            title = { Text(text = stringResource(R.string.create_playlist)) },
            onDismiss = { showAddPlaylistDialog = false },
            onDone = { playlistName ->
                database.query {
                    insert(
                        PlaylistEntity(
                            name = playlistName
                        )
                    )
                }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
        ) {
            item(
                key = "header",
                contentType = CONTENT_TYPE_HEADER
            ) {
                SortHeader(
                    sortType = sortType,
                    sortDescending = sortDescending,
                    onSortTypeChange = onSortTypeChange,
                    onSortDescendingChange = onSortDescendingChange,
                    sortTypeText = { sortType ->
                        when (sortType) {
                            PlaylistSortType.CREATE_DATE -> R.string.sort_by_create_date
                            PlaylistSortType.NAME -> R.string.sort_by_name
                            PlaylistSortType.SONG_COUNT -> R.string.sort_by_song_count
                        }
                    },
                    trailingText = pluralStringResource(R.plurals.n_playlist, playlists.size, playlists.size)
                )
            }

            item(
                key = LIKED_PLAYLIST_ID,
                contentType = CONTENT_TYPE_PLAYLIST
            ) {
                ListItem(
                    title = stringResource(R.string.liked_songs),
                    subtitle = pluralStringResource(R.plurals.n_song, likedSongCount, likedSongCount),
                    thumbnailContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_favorite),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(ListThumbnailSize)
                        )
                    },
                    modifier = Modifier
                        .clickable {
                            navController.navigate("local_playlist/$LIKED_PLAYLIST_ID")
                        }
                        .animateItemPlacement()
                )
            }

            item(
                key = DOWNLOADED_PLAYLIST_ID,
                contentType = CONTENT_TYPE_PLAYLIST
            ) {
                ListItem(
                    title = stringResource(R.string.downloaded_songs),
                    subtitle = pluralStringResource(R.plurals.n_song, downloadedSongCount, downloadedSongCount),
                    thumbnailContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_save_alt),
                            contentDescription = null,
                            modifier = Modifier.size(ListThumbnailSize)
                        )
                    },
                    modifier = Modifier
                        .clickable {
                            navController.navigate("local_playlist/$DOWNLOADED_PLAYLIST_ID")
                        }
                        .animateItemPlacement()
                )
            }

            items(
                items = playlists,
                key = { it.id },
                contentType = { CONTENT_TYPE_PLAYLIST }
            ) { playlist ->
                PlaylistListItem(
                    playlist = playlist,
                    trailingContent = {
                        IconButton(
                            onClick = {
                                menuState.show {
                                    PlaylistMenu(
                                        playlist = playlist,
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
                        .clickable {
                            navController.navigate("local_playlist/${playlist.id}")
                        }
                        .animateItemPlacement()
                )
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(
                    LocalPlayerAwareWindowInsets.current
                        .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                )
                .padding(16.dp),
            onClick = { showAddPlaylistDialog = true }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null
            )
        }
    }
}
