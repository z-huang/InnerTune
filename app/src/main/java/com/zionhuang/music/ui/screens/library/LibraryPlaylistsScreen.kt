package com.zionhuang.music.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.zionhuang.music.constants.CONTENT_TYPE_HEADER
import com.zionhuang.music.constants.CONTENT_TYPE_PLAYLIST
import com.zionhuang.music.constants.ListThumbnailSize
import com.zionhuang.music.constants.PlaylistSortDescendingKey
import com.zionhuang.music.constants.PlaylistSortType
import com.zionhuang.music.constants.PlaylistSortTypeKey
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.DOWNLOADED_PLAYLIST_ID
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.LIKED_PLAYLIST_ID
import com.zionhuang.music.ui.component.ListItem
import com.zionhuang.music.ui.component.LocalMenuState
import com.zionhuang.music.ui.component.PlaylistListItem
import com.zionhuang.music.ui.component.SortHeader
import com.zionhuang.music.ui.component.TextFieldDialog
import com.zionhuang.music.ui.menu.PlaylistMenu
import com.zionhuang.music.ui.utils.isScrollingUp
import com.zionhuang.music.utils.rememberEnumPreference
import com.zionhuang.music.utils.rememberPreference
import com.zionhuang.music.viewmodels.LibraryPlaylistsViewModel

@OptIn(ExperimentalFoundationApi::class)
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
    val downloadedSongCount by viewModel.downloadedSongCount.collectAsState(0)
    val playlists by viewModel.allPlaylists.collectAsState()

    val lazyListState = rememberLazyListState()

    var showAddPlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showAddPlaylistDialog) {
        TextFieldDialog(
            icon = { Icon(painter = painterResource(R.drawable.add), contentDescription = null) },
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
            state = lazyListState,
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
                            painter = painterResource(R.drawable.favorite),
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
                            painter = painterResource(R.drawable.offline),
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
                                painter = painterResource(R.drawable.more_vert),
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

        AnimatedVisibility(
            visible = lazyListState.isScrollingUp(),
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(
                    LocalPlayerAwareWindowInsets.current
                        .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                )
        ) {
            FloatingActionButton(
                modifier = Modifier.padding(16.dp),
                onClick = { showAddPlaylistDialog = true }
            ) {
                Icon(
                    painter = painterResource(R.drawable.add),
                    contentDescription = null
                )
            }
        }
    }
}
