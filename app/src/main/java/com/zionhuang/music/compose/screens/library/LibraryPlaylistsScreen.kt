package com.zionhuang.music.compose.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerAwareWindowInsets
import com.zionhuang.music.compose.component.ListItem
import com.zionhuang.music.compose.component.PlaylistListItem
import com.zionhuang.music.compose.utils.rememberPreference
import com.zionhuang.music.constants.CONTENT_TYPE_HEADER
import com.zionhuang.music.constants.CONTENT_TYPE_PLAYLIST
import com.zionhuang.music.constants.Constants.DOWNLOADED_PLAYLIST_ID
import com.zionhuang.music.constants.Constants.LIKED_PLAYLIST_ID
import com.zionhuang.music.constants.PLAYLIST_SORT_DESCENDING
import com.zionhuang.music.constants.PLAYLIST_SORT_TYPE
import com.zionhuang.music.models.sortInfo.PlaylistSortType
import com.zionhuang.music.viewmodels.SongsViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun LibraryPlaylistsScreen(
    paddingModifier: PaddingValues,
    viewModel: SongsViewModel = viewModel(),
) {
    val sortType by rememberPreference(PLAYLIST_SORT_TYPE, PlaylistSortType.CREATE_DATE)
    val sortDescending by rememberPreference(PLAYLIST_SORT_DESCENDING, true)
    val likedSongCount by viewModel.likedSongCount.collectAsState(initial = 0)
    val downloadedSongCount by viewModel.downloadedSongCount.collectAsState(initial = 0)
    val items by viewModel.allPlaylistsFlow.collectAsState()

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
    }
}

