package com.zionhuang.music.ui.screens.playlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastSumBy
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.SongSortDescendingKey
import com.zionhuang.music.constants.SongSortType
import com.zionhuang.music.constants.SongSortTypeKey
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.ui.component.AppBarConfig
import com.zionhuang.music.ui.component.LocalMenuState
import com.zionhuang.music.ui.component.SongListItem
import com.zionhuang.music.ui.component.SortHeader
import com.zionhuang.music.ui.menu.SongMenu
import com.zionhuang.music.utils.joinByBullet
import com.zionhuang.music.utils.makeTimeString
import com.zionhuang.music.utils.rememberEnumPreference
import com.zionhuang.music.utils.rememberPreference
import com.zionhuang.music.viewmodels.BuiltInPlaylistViewModel

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun BuiltInPlaylistScreen(
    appBarConfig: AppBarConfig,
    navController: NavController,
    viewModel: BuiltInPlaylistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val (sortType, onSortTypeChange) = rememberEnumPreference(SongSortTypeKey, SongSortType.CREATE_DATE)
    val (sortDescending, onSortDescendingChange) = rememberPreference(SongSortDescendingKey, true)

    val songs by viewModel.songs.collectAsState()
    val playlistLength = remember(songs) {
        songs.fastSumBy { it.song.duration }
    }
    val playlistName = remember {
        context.getString(
            when (viewModel.playlistId) {
                PlaylistEntity.LIKED_PLAYLIST_ID -> R.string.liked_songs
                PlaylistEntity.DOWNLOADED_PLAYLIST_ID -> R.string.downloaded_songs
                else -> error("Unknown playlist id")
            }
        )
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        appBarConfig.title = {
            Text(
                text = playlistName,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
        ) {
            item {
                SortHeader(
                    sortType = sortType,
                    sortDescending = sortDescending,
                    onSortTypeChange = onSortTypeChange,
                    onSortDescendingChange = onSortDescendingChange,
                    sortTypeText = { sortType ->
                        when (sortType) {
                            SongSortType.CREATE_DATE -> R.string.sort_by_create_date
                            SongSortType.NAME -> R.string.sort_by_name
                            SongSortType.ARTIST -> R.string.sort_by_artist
                        }
                    },
                    trailingText = joinByBullet(
                        makeTimeString(playlistLength * 1000L),
                        pluralStringResource(R.plurals.song_count, songs.size, songs.size)
                    )
                )
            }

            itemsIndexed(
                items = songs,
                key = { _, song -> song.id }
            ) { index, song ->
                SongListItem(
                    song = song,
                    isPlaying = song.id == mediaMetadata?.id,
                    playWhenReady = playWhenReady,
                    trailingContent = {
                        IconButton(
                            onClick = {
                                menuState.show {
                                    SongMenu(
                                        originalSong = song,
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
                        .fillMaxWidth()
                        .combinedClickable {
                            playerConnection.playQueue(ListQueue(
                                title = playlistName,
                                items = songs.map { it.toMediaItem() },
                                startIndex = index
                            ))
                        }
                        .animateItemPlacement()
                )
            }
        }

        if (songs.isNotEmpty()) {
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(
                        LocalPlayerAwareWindowInsets.current
                            .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                    )
                    .padding(16.dp),
                onClick = {
                    playerConnection.playQueue(ListQueue(
                        title = playlistName,
                        items = songs.shuffled().map { it.toMediaItem() },
                    ))
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_shuffle),
                    contentDescription = null
                )
            }
        }
    }
}
