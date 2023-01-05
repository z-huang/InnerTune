package com.zionhuang.music.compose.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerAwareWindowInsets
import com.zionhuang.music.compose.component.ListItem
import com.zionhuang.music.compose.component.PlaylistListItem
import com.zionhuang.music.compose.component.ResizableIconButton
import com.zionhuang.music.compose.component.TextFieldDialog
import com.zionhuang.music.constants.*
import com.zionhuang.music.constants.Constants.DOWNLOADED_PLAYLIST_ID
import com.zionhuang.music.constants.Constants.LIKED_PLAYLIST_ID
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.extensions.mutablePreferenceState
import com.zionhuang.music.models.sortInfo.PlaylistSortType
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.viewmodels.LibraryPlaylistsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun LibraryPlaylistsScreen(
    navController: NavController,
    viewModel: LibraryPlaylistsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val likedSongCount by viewModel.likedSongCount.collectAsState()
    val downloadedSongCount by viewModel.downloadedSongCount.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()

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
                PlaylistHeader(itemCount = playlists.size)
            }

            item(
                key = LIKED_PLAYLIST_ID,
                contentType = CONTENT_TYPE_PLAYLIST
            ) {
                ListItem(
                    title = stringResource(R.string.liked_songs),
                    subtitle = pluralStringResource(R.plurals.song_count, likedSongCount, likedSongCount),
                    thumbnailContent = {
                        Image(
                            painter = painterResource(R.drawable.ic_favorite),
                            contentDescription = null,
                            modifier = Modifier.size(ListThumbnailSize)
                        )
                    },
                    modifier = Modifier
                        .clickable {

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
                    thumbnailContent = {
                        Image(
                            painter = painterResource(R.drawable.ic_save_alt),
                            contentDescription = null,
                            modifier = Modifier.size(ListThumbnailSize)
                        )
                    },
                    modifier = Modifier
                        .clickable {

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("playlist/${playlist.id}")
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlaylistHeader(
    itemCount: Int,
) {
    val (sortType, onSortTypeChange) = mutablePreferenceState(PLAYLIST_SORT_TYPE, PlaylistSortType.CREATE_DATE)
    val (sortDescending, onSortDescendingChange) = mutablePreferenceState(PLAYLIST_SORT_DESCENDING, true)
    val (menuExpanded, onMenuExpandedChange) = remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(when (sortType) {
                PlaylistSortType.CREATE_DATE -> R.string.sort_by_create_date
                PlaylistSortType.NAME -> R.string.sort_by_name
                PlaylistSortType.SONG_COUNT -> R.string.sort_by_song_count
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
                .padding(horizontal = 4.dp, vertical = 8.dp)
        )

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { onMenuExpandedChange(false) },
            modifier = Modifier.widthIn(min = 172.dp)
        ) {
            listOf(
                PlaylistSortType.CREATE_DATE to R.string.sort_by_create_date,
                PlaylistSortType.NAME to R.string.sort_by_name,
                PlaylistSortType.SONG_COUNT to R.string.sort_by_song_count
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
            text = pluralStringResource(R.plurals.playlist_count, itemCount, itemCount),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
