package com.zionhuang.music.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.constants.*
import com.zionhuang.music.ui.component.ArtistListItem
import com.zionhuang.music.ui.component.ResizableIconButton
import com.zionhuang.music.utils.rememberEnumPreference
import com.zionhuang.music.utils.rememberPreference
import com.zionhuang.music.viewmodels.LibraryArtistsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryArtistsScreen(
    navController: NavController,
    viewModel: LibraryArtistsViewModel = hiltViewModel(),
) {
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
                ArtistHeader(artists.size)
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ArtistHeader(
    itemCount: Int,
) {
    var sortType by rememberEnumPreference(ArtistSortTypeKey, ArtistSortType.CREATE_DATE)
    var sortDescending by rememberPreference(ArtistSortDescendingKey, true)
    val (menuExpanded, onMenuExpandedChange) = remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(when (sortType) {
                ArtistSortType.CREATE_DATE -> R.string.sort_by_create_date
                ArtistSortType.NAME -> R.string.sort_by_name
                ArtistSortType.SONG_COUNT -> R.string.sort_by_song_count
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
                ArtistSortType.CREATE_DATE to R.string.sort_by_create_date,
                ArtistSortType.NAME to R.string.sort_by_name,
                ArtistSortType.SONG_COUNT to R.string.sort_by_song_count
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
                        sortType = type
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
            onClick = { sortDescending = !sortDescending }
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = pluralStringResource(R.plurals.artist_count, itemCount, itemCount),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}