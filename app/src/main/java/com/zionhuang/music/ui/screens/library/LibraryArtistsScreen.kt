package com.zionhuang.music.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.constants.*
import com.zionhuang.music.ui.component.ArtistListItem
import com.zionhuang.music.ui.component.SortHeader
import com.zionhuang.music.utils.rememberEnumPreference
import com.zionhuang.music.utils.rememberPreference
import com.zionhuang.music.viewmodels.LibraryArtistsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryArtistsScreen(
    navController: NavController,
    viewModel: LibraryArtistsViewModel = hiltViewModel(),
) {
    val (sortType, onSortTypeChange) = rememberEnumPreference(ArtistSortTypeKey, ArtistSortType.CREATE_DATE)
    val (sortDescending, onSortDescendingChange) = rememberPreference(ArtistSortDescendingKey, true)

    val artists by viewModel.allArtists.collectAsState()

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
                            ArtistSortType.CREATE_DATE -> R.string.sort_by_create_date
                            ArtistSortType.NAME -> R.string.sort_by_name
                            ArtistSortType.SONG_COUNT -> R.string.sort_by_song_count
                            ArtistSortType.PLAY_TIME -> R.string.sort_by_play_time
                        }
                    },
                    trailingText = pluralStringResource(R.plurals.n_artist, artists.size, artists.size)
                )
            }

            items(
                items = artists,
                key = { it.id },
                contentType = { CONTENT_TYPE_ARTIST }
            ) { artist ->
                ArtistListItem(
                    artist = artist,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("artist/${artist.id}")
                        }
                        .animateItemPlacement()
                )
            }
        }
    }
}
