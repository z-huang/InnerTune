package com.zionhuang.music.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.*
import com.zionhuang.music.ui.component.AlbumListItem
import com.zionhuang.music.ui.component.LocalMenuState
import com.zionhuang.music.ui.component.SortHeader
import com.zionhuang.music.ui.menu.AlbumMenu
import com.zionhuang.music.utils.rememberEnumPreference
import com.zionhuang.music.utils.rememberPreference
import com.zionhuang.music.viewmodels.LibraryAlbumsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryAlbumsScreen(
    navController: NavController,
    viewModel: LibraryAlbumsViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val (sortType, onSortTypeChange) = rememberEnumPreference(AlbumSortTypeKey, AlbumSortType.CREATE_DATE)
    val (sortDescending, onSortDescendingChange) = rememberPreference(AlbumSortDescendingKey, true)

    val albums by viewModel.allAlbums.collectAsState()

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
                            AlbumSortType.CREATE_DATE -> R.string.sort_by_create_date
                            AlbumSortType.NAME -> R.string.sort_by_name
                            AlbumSortType.ARTIST -> R.string.sort_by_artist
                            AlbumSortType.YEAR -> R.string.sort_by_year
                            AlbumSortType.SONG_COUNT -> R.string.sort_by_song_count
                            AlbumSortType.LENGTH -> R.string.sort_by_length
                            AlbumSortType.PLAY_TIME -> R.string.sort_by_play_time
                        }
                    },
                    trailingText = pluralStringResource(R.plurals.n_album, albums.size, albums.size)
                )
            }

            items(
                items = albums,
                key = { it.id },
                contentType = { CONTENT_TYPE_ALBUM }
            ) { album ->
                AlbumListItem(
                    album = album,
                    isActive = album.id == mediaMetadata?.album?.id,
                    isPlaying = isPlaying,
                    trailingContent = {
                        IconButton(
                            onClick = {
                                menuState.show {
                                    AlbumMenu(
                                        album = album,
                                        navController = navController,
                                        playerConnection = playerConnection,
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
                        .combinedClickable {
                            navController.navigate("album/${album.id}")
                        }
                        .animateItemPlacement()
                )
            }
        }
    }
}
