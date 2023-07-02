package com.zionhuang.music.ui.screens.playlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastSumBy
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.DownloadedSongSortDescendingKey
import com.zionhuang.music.constants.DownloadedSongSortType
import com.zionhuang.music.constants.DownloadedSongSortTypeKey
import com.zionhuang.music.constants.SongSortDescendingKey
import com.zionhuang.music.constants.SongSortType
import com.zionhuang.music.constants.SongSortTypeKey
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.DOWNLOADED_PLAYLIST_ID
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.LIKED_PLAYLIST_ID
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.extensions.togglePlayPause
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.ui.component.LocalMenuState
import com.zionhuang.music.ui.component.SongListItem
import com.zionhuang.music.ui.component.SortHeader
import com.zionhuang.music.ui.menu.SongMenu
import com.zionhuang.music.utils.joinByBullet
import com.zionhuang.music.utils.makeTimeString
import com.zionhuang.music.utils.rememberEnumPreference
import com.zionhuang.music.utils.rememberPreference
import com.zionhuang.music.viewmodels.BuiltInPlaylistViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BuiltInPlaylistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: BuiltInPlaylistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val (sortType, onSortTypeChange) = rememberEnumPreference(SongSortTypeKey, SongSortType.CREATE_DATE)
    val (sortDescending, onSortDescendingChange) = rememberPreference(SongSortDescendingKey, true)
    val (dlSortType, onDlSortTypeChange) = rememberEnumPreference(DownloadedSongSortTypeKey, DownloadedSongSortType.CREATE_DATE)
    val (dlSortDescending, onDlSortDescendingChange) = rememberPreference(DownloadedSongSortDescendingKey, true)

    val songs by viewModel.songs.collectAsState()
    val playlistLength = remember(songs) {
        songs.fastSumBy { it.song.duration }
    }
    val playlistName = remember {
        context.getString(
            when (viewModel.playlistId) {
                LIKED_PLAYLIST_ID -> R.string.liked_songs
                DOWNLOADED_PLAYLIST_ID -> R.string.downloaded_songs
                else -> error("Unknown playlist id")
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
        ) {
            item {
                if (viewModel.playlistId == LIKED_PLAYLIST_ID) {
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
                            pluralStringResource(R.plurals.n_song, songs.size, songs.size)
                        )
                    )
                } else {
                    SortHeader(
                        sortType = dlSortType,
                        sortDescending = dlSortDescending,
                        onSortTypeChange = onDlSortTypeChange,
                        onSortDescendingChange = onDlSortDescendingChange,
                        sortTypeText = { sortType ->
                            when (sortType) {
                                DownloadedSongSortType.CREATE_DATE -> R.string.sort_by_create_date
                                DownloadedSongSortType.NAME -> R.string.sort_by_name
                                DownloadedSongSortType.ARTIST -> R.string.sort_by_artist
                            }
                        },
                        trailingText = joinByBullet(
                            makeTimeString(playlistLength * 1000L),
                            pluralStringResource(R.plurals.n_song, songs.size, songs.size)
                        )
                    )
                }
            }

            itemsIndexed(
                items = songs,
                key = { _, song -> song.id }
            ) { index, song ->
                SongListItem(
                    song = song,
                    showLikedIcon = viewModel.playlistId != LIKED_PLAYLIST_ID,
                    showInLibraryIcon = true,
                    showDownloadIcon = viewModel.playlistId != DOWNLOADED_PLAYLIST_ID,
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
                            if (song.id == mediaMetadata?.id) {
                                playerConnection.player.togglePlayPause()
                            } else {
                                playerConnection.playQueue(
                                    ListQueue(
                                        title = playlistName,
                                        items = songs.map { it.toMediaItem() },
                                        startIndex = index
                                    )
                                )
                            }
                        }
                        .animateItemPlacement()
                )
            }
        }

        TopAppBar(
            title = { Text(playlistName) },
            navigationIcon = {
                IconButton(onClick = navController::navigateUp) {
                    Icon(
                        painterResource(R.drawable.arrow_back),
                        contentDescription = null
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )

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
                    playerConnection.playQueue(
                        ListQueue(
                            title = playlistName,
                            items = songs.shuffled().map { it.toMediaItem() },
                        )
                    )
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.shuffle),
                    contentDescription = null
                )
            }
        }
    }
}
