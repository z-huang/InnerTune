package com.zionhuang.music.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.zionhuang.music.constants.ArtistSortDescendingKey
import com.zionhuang.music.constants.ArtistSortType
import com.zionhuang.music.constants.ArtistSortTypeKey
import com.zionhuang.music.constants.ArtistViewType
import com.zionhuang.music.constants.ArtistViewTypeKey
import com.zionhuang.music.constants.CONTENT_TYPE_ARTIST
import com.zionhuang.music.ui.component.ArtistListItem
import com.zionhuang.music.ui.component.SortHeader
import com.zionhuang.music.utils.rememberEnumPreference
import com.zionhuang.music.utils.rememberPreference
import com.zionhuang.music.viewmodels.LibraryArtistsViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryArtistsScreen(
    navController: NavController,
    viewModel: LibraryArtistsViewModel = hiltViewModel(),
) {
    val database = LocalDatabase.current
    var viewType by rememberEnumPreference(ArtistViewTypeKey, ArtistViewType.ALL)
    val (sortType, onSortTypeChange) = rememberEnumPreference(ArtistSortTypeKey, ArtistSortType.CREATE_DATE)
    val (sortDescending, onSortDescendingChange) = rememberPreference(ArtistSortDescendingKey, true)

    val artists by viewModel.allArtists.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
        ) {
            item(key = "viewType") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.width(16.dp))

                    listOf(
                        ArtistViewType.ALL to stringResource(R.string.filter_all),
                        ArtistViewType.BOOKMARKED to stringResource(R.string.filter_bookmarked)
                    ).forEach {
                        FilterChip(
                            label = { Text(it.second) },
                            selected = viewType == it.first,
                            colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.background),
                            onClick = { viewType = it.first }
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }

            item(key = "header") {
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
                    trailingContent = {
                        IconButton(
                            onClick = {
                                database.transaction {
                                    update(
                                        artist.artist.copy(
                                            bookmarkedAt = if (artist.artist.bookmarkedAt != null) null else LocalDateTime.now()
                                        )
                                    )
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(if (artist.artist.bookmarkedAt != null) R.drawable.bookmark_filled else R.drawable.bookmark),
                                tint = if (artist.artist.bookmarkedAt != null) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                                contentDescription = null
                            )
                        }
                    },
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
