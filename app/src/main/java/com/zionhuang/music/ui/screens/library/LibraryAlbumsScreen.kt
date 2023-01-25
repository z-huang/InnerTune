package com.zionhuang.music.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.*
import com.zionhuang.music.ui.component.AlbumListItem
import com.zionhuang.music.ui.component.SortHeader
import com.zionhuang.music.utils.rememberEnumPreference
import com.zionhuang.music.utils.rememberPreference
import com.zionhuang.music.viewmodels.LibraryAlbumsViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun LibraryAlbumsScreen(
    navController: NavController,
    viewModel: LibraryAlbumsViewModel = hiltViewModel(),
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState()
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
                        }
                    },
                    trailingText = pluralStringResource(R.plurals.album_count, albums.size, albums.size)
                )
            }

            items(
                items = albums,
                key = { it.id },
                contentType = { CONTENT_TYPE_ALBUM }
            ) { album ->
                AlbumListItem(
                    album = album,
                    isPlaying = album.id == mediaMetadata?.album?.id,
                    playWhenReady = playWhenReady,
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
