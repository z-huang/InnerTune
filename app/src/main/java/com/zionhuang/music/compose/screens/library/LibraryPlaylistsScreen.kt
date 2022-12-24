package com.zionhuang.music.compose.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerAwareWindowInsets
import com.zionhuang.music.compose.component.ListItem
import com.zionhuang.music.compose.component.PlaylistListItem
import com.zionhuang.music.compose.component.TextFieldDialog
import com.zionhuang.music.compose.utils.rememberPreference
import com.zionhuang.music.constants.CONTENT_TYPE_HEADER
import com.zionhuang.music.constants.CONTENT_TYPE_PLAYLIST
import com.zionhuang.music.constants.Constants.DOWNLOADED_PLAYLIST_ID
import com.zionhuang.music.constants.Constants.LIKED_PLAYLIST_ID
import com.zionhuang.music.constants.PLAYLIST_SORT_DESCENDING
import com.zionhuang.music.constants.PLAYLIST_SORT_TYPE
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.models.sortInfo.PlaylistSortType
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryPlaylistsScreen(
    viewModel: SongsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sortType by rememberPreference(PLAYLIST_SORT_TYPE, PlaylistSortType.CREATE_DATE)
    val sortDescending by rememberPreference(PLAYLIST_SORT_DESCENDING, true)
    val likedSongCount by viewModel.likedSongCount.collectAsState(initial = 0)
    val downloadedSongCount by viewModel.downloadedSongCount.collectAsState(initial = 0)
    val items by viewModel.allPlaylistsFlow.collectAsState()

    var showAddPlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showAddPlaylistDialog) {
        TextFieldDialog(
            icon = { Icon(painter = painterResource(R.drawable.ic_add), contentDescription = null) },
            title = { Text(text = stringResource(R.string.dialog_title_create_playlist)) },
            onDismiss = { showAddPlaylistDialog = false },
            onDone = { playlistName ->
                coroutineScope.launch {
                    SongRepository(context).insertPlaylist(PlaylistEntity(
                        name = playlistName
                    ))
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
                TextButton(onClick = {}) {
                    Text(stringResource(when (sortType) {
                        PlaylistSortType.CREATE_DATE -> R.string.sort_by_create_date
                        PlaylistSortType.NAME -> R.string.sort_by_name
                        PlaylistSortType.SONG_COUNT -> R.string.sort_by_song_count
                    }))
                }
            }

            item(
                key = LIKED_PLAYLIST_ID,
                contentType = CONTENT_TYPE_PLAYLIST
            ) {
                ListItem(
                    title = stringResource(R.string.liked_songs),
                    subtitle = pluralStringResource(R.plurals.song_count, likedSongCount, likedSongCount),
                    thumbnailDrawable = R.drawable.ic_favorite,
                    modifier = Modifier
                        .combinedClickable {

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
                    subtitle = pluralStringResource(R.plurals.song_count, downloadedSongCount, downloadedSongCount),
                    thumbnailDrawable = R.drawable.ic_save_alt,
                    modifier = Modifier
                        .combinedClickable {

                        }
                        .animateItemPlacement()
                )
            }

            itemsIndexed(
                items = items,
                key = { _, item -> item.id },
                contentType = { _, _ -> CONTENT_TYPE_PLAYLIST }
            ) { index, playlist ->
                PlaylistListItem(
                    playlist = playlist,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable {

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
            onClick = { showAddPlaylistDialog = true }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null
            )
        }
    }
}

