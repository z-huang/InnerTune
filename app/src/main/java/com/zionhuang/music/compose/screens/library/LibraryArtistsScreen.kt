package com.zionhuang.music.compose.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zionhuang.music.R
import com.zionhuang.music.compose.component.ArtistListItem
import com.zionhuang.music.compose.LocalPlayerAwareWindowInsets
import com.zionhuang.music.compose.utils.rememberPreference
import com.zionhuang.music.constants.ARTIST_SORT_DESCENDING
import com.zionhuang.music.constants.ARTIST_SORT_TYPE
import com.zionhuang.music.constants.CONTENT_TYPE_ARTIST
import com.zionhuang.music.constants.CONTENT_TYPE_HEADER
import com.zionhuang.music.models.sortInfo.ArtistSortType
import com.zionhuang.music.viewmodels.SongsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryArtistsScreen(
    paddingModifier: PaddingValues,
    viewModel: SongsViewModel = viewModel(),
) {
    val sortType by rememberPreference(ARTIST_SORT_TYPE, ArtistSortType.CREATE_DATE)
    val sortDescending by rememberPreference(ARTIST_SORT_DESCENDING, true)
    val items by viewModel.allArtistsFlow.collectAsState(emptyList())

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues()
        ) {
            item(
                key = "header",
                contentType = CONTENT_TYPE_HEADER
            ) {
                TextButton(onClick = {}) {
                    Text(stringResource(when (sortType) {
                        ArtistSortType.CREATE_DATE -> R.string.sort_by_create_date
                        ArtistSortType.NAME -> R.string.sort_by_name
                        ArtistSortType.SONG_COUNT -> R.string.sort_by_song_count
                    }))
                }
            }

            itemsIndexed(
                items = items,
                key = { _, item -> item.id },
                contentType = { _, _ -> CONTENT_TYPE_ARTIST }
            ) { index, artist ->
                ArtistListItem(
                    artist = artist,
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