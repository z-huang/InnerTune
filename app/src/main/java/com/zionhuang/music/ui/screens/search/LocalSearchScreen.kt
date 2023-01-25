package com.zionhuang.music.ui.screens.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.CONTENT_TYPE_LIST
import com.zionhuang.music.constants.ListItemHeight
import com.zionhuang.music.db.entities.Album
import com.zionhuang.music.db.entities.Artist
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.ui.component.*
import com.zionhuang.music.ui.menu.SongMenu
import com.zionhuang.music.viewmodels.LocalFilter
import com.zionhuang.music.viewmodels.LocalSearchViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LocalSearchScreen(
    query: String,
    navController: NavController,
    viewModel: LocalSearchViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val coroutineScope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val searchFilter by viewModel.filter.collectAsState()
    val result by viewModel.result.collectAsState()

    LaunchedEffect(query) {
        viewModel.query.value = query
    }

    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            listOf(
                LocalFilter.ALL to R.string.search_filter_all,
                LocalFilter.SONG to R.string.search_filter_songs,
                LocalFilter.ALBUM to R.string.search_filter_albums,
                LocalFilter.ARTIST to R.string.search_filter_artists,
                LocalFilter.PLAYLIST to R.string.search_filter_playlists
            ).forEach { (filter, label) ->
                FilterChip(
                    label = { Text(stringResource(label)) },
                    selected = searchFilter == filter,
                    colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.background),
                    onClick = { viewModel.filter.value = filter }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            result.map.forEach { (filter, items) ->
                if (result.filter == LocalFilter.ALL) {
                    item(
                        key = filter
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ListItemHeight)
                                .clickable { viewModel.filter.value = filter }
                                .padding(start = 12.dp, end = 18.dp)
                        ) {
                            Text(
                                text = stringResource(when (filter) {
                                    LocalFilter.SONG -> R.string.search_filter_songs
                                    LocalFilter.ALBUM -> R.string.search_filter_albums
                                    LocalFilter.ARTIST -> R.string.search_filter_artists
                                    LocalFilter.PLAYLIST -> R.string.search_filter_playlists
                                    LocalFilter.ALL -> error("")
                                }),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f)
                            )

                            Icon(
                                painter = painterResource(R.drawable.ic_navigate_next),
                                contentDescription = null
                            )
                        }
                    }
                }

                items(
                    items = items,
                    key = { it.id },
                    contentType = { CONTENT_TYPE_LIST }
                ) { item ->
                    when (item) {
                        is Song -> SongListItem(
                            song = item,
                            isPlaying = item.id == mediaMetadata?.id,
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        menuState.show {
                                            SongMenu(
                                                originalSong = item,
                                                navController = navController,
                                                playerConnection = playerConnection,
                                                coroutineScope = coroutineScope,
                                                onDismiss = menuState::dismiss
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_more_vert),
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier
                                .clickable {
                                    val songs = result.map
                                        .getOrDefault(LocalFilter.SONG, emptyList())
                                        .filterIsInstance<Song>()
                                        .map { it.toMediaItem() }
                                    playerConnection.playQueue(ListQueue(
                                        title = context.getString(R.string.queue_searched_songs),
                                        items = songs,
                                        startIndex = songs.indexOfFirst { it.mediaId == item.id }
                                    ))
                                }
                                .animateItemPlacement()
                        )
                        is Album -> AlbumListItem(
                            album = item,
                            isPlaying = item.id == mediaMetadata?.album?.id,
                            playWhenReady = playWhenReady,
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("album/${item.id}")
                                }
                                .animateItemPlacement()
                        )
                        is Artist -> ArtistListItem(
                            artist = item,
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("artist/${item.id}")
                                }
                                .animateItemPlacement()
                        )
                        is Playlist -> PlaylistListItem(
                            playlist = item,
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("playlist/${item.id}")
                                }
                                .animateItemPlacement()
                        )
                    }
                }
            }

            if (result.query.isNotEmpty() && result.map.isEmpty()) {
                item(
                    key = "no_result"
                ) {
                    NoResultFound()
                }
            }
        }
    }
}