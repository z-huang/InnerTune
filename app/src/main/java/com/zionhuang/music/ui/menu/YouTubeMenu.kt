package com.zionhuang.music.ui.menu

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.ArtistItem
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.constants.ListItemHeight
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.PlayerConnection
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.component.GridMenu
import com.zionhuang.music.ui.component.GridMenuItem
import com.zionhuang.music.ui.component.ListDialog
import com.zionhuang.music.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun YouTubeSongMenu(
    song: SongItem,
    navController: NavController,
    playerConnection: PlayerConnection,
    coroutineScope: CoroutineScope,
    mainViewModel: MainViewModel = viewModel(),
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val songRepository = SongRepository(context)
    val librarySongIds by mainViewModel.librarySongIds.collectAsState()
    val addedToLibrary = remember(librarySongIds) {
        song.id in librarySongIds
    }
    val artists = remember {
        song.artists.mapNotNull {
            it.id?.let { artistId ->
                MediaMetadata.Artist(id = artistId, name = it.name)
            }
        }
    }

    var showSelectArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSelectArtistDialog) {
        ListDialog(
            onDismiss = { showSelectArtistDialog = false }
        ) {
            items(artists) { artist ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(ListItemHeight)
                        .clickable {
                            navController.navigate("artist/${artist.id}")
                            showSelectArtistDialog = false
                            onDismiss()
                        }
                        .padding(horizontal = 12.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .height(ListItemHeight)
                            .clickable {
                                navController.navigate("artist/${artist.id}")
                                showSelectArtistDialog = false
                                onDismiss()
                            }
                            .padding(horizontal = 24.dp),
                    ) {
                        Text(
                            text = artist.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    GridMenu(
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        GridMenuItem(
            icon = R.drawable.ic_radio,
            title = R.string.menu_start_radio
        ) {
            playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = song.id), song.toMediaMetadata()))
            onDismiss()
        }
        GridMenuItem(
            icon = R.drawable.ic_playlist_play,
            title = R.string.menu_play_next
        ) {
            playerConnection.playNext(song.toMediaItem())
            onDismiss()
        }
        GridMenuItem(
            icon = R.drawable.ic_queue_music,
            title = R.string.menu_add_to_queue
        ) {
            playerConnection.addToQueue((song.toMediaItem()))
            onDismiss()
        }
        if (addedToLibrary) {
            GridMenuItem(
                icon = R.drawable.ic_library_add_check,
                title = R.string.action_remove_from_library
            ) {
                coroutineScope.launch {
                    songRepository.deleteSong(song.id)
                }
            }
        } else {
            GridMenuItem(
                icon = R.drawable.ic_library_add,
                title = R.string.action_add_to_library
            ) {
                coroutineScope.launch {
                    songRepository.addSong(song.toMediaMetadata())
                }
            }
        }
        GridMenuItem(
            icon = R.drawable.ic_playlist_add,
            title = R.string.menu_add_to_playlist,
            enabled = false
        ) {

        }
        GridMenuItem(
            icon = R.drawable.ic_file_download,
            title = R.string.menu_download,
            enabled = false
        ) {

        }
        if (artists.isNotEmpty()) {
            GridMenuItem(
                icon = R.drawable.ic_artist,
                title = R.string.menu_view_artist
            ) {
                if (artists.size == 1) {
                    navController.navigate("artist/${artists[0].id}")
                    onDismiss()
                } else {
                    showSelectArtistDialog = true
                }
            }
        }
        song.album?.let { album ->
            GridMenuItem(
                icon = R.drawable.ic_album,
                title = R.string.menu_view_album
            ) {
                navController.navigate("album/${album.id}")
                onDismiss()
            }
        }
        GridMenuItem(
            icon = R.drawable.ic_share,
            title = R.string.menu_share
        ) {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, song.shareLink)
            }
            context.startActivity(Intent.createChooser(intent, null))
            onDismiss()
        }
    }
}

@Composable
fun YouTubeAlbumMenu(
    album: AlbumItem,
    navController: NavController,
    playerConnection: PlayerConnection,
    coroutineScope: CoroutineScope,
    mainViewModel: MainViewModel = viewModel(),
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val songRepository = SongRepository(context)
    val libraryAlbumIds by mainViewModel.libraryAlbumIds.collectAsState()
    val addedToLibrary = remember(libraryAlbumIds) {
        album.id in libraryAlbumIds
    }

    var showSelectArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSelectArtistDialog) {
        ListDialog(
            onDismiss = { showSelectArtistDialog = false }
        ) {
            items(
                items = album.artists.orEmpty(),
                key = { it.id!! }
            ) { artist ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(ListItemHeight)
                        .clickable {
                            navController.navigate("artist/${artist.id}")
                            showSelectArtistDialog = false
                            onDismiss()
                        }
                        .padding(horizontal = 12.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .height(ListItemHeight)
                            .clickable {
                                showSelectArtistDialog = false
                                onDismiss()
                                navController.navigate("artist/${artist.id}")
                            }
                            .padding(horizontal = 24.dp),
                    ) {
                        Text(
                            text = artist.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    GridMenu(
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        GridMenuItem(
            icon = R.drawable.ic_radio,
            title = R.string.menu_start_radio
        ) {
            playerConnection.playQueue(YouTubeQueue(WatchEndpoint(
                playlistId = album.playlistId,
                params = "wAEB"
            )))
            onDismiss()
        }
        GridMenuItem(
            icon = R.drawable.ic_playlist_play,
            title = R.string.menu_play_next,
            enabled = false
        ) {
            onDismiss()
        }
        GridMenuItem(
            icon = R.drawable.ic_queue_music,
            title = R.string.menu_add_to_queue,
            enabled = false
        ) {
            onDismiss()
        }
        if (addedToLibrary) {
            GridMenuItem(
                icon = R.drawable.ic_library_add_check,
                title = R.string.action_remove_from_library
            ) {
                coroutineScope.launch {
                    songRepository.deleteAlbum(album.id)
                }
            }
        } else {
            GridMenuItem(
                icon = R.drawable.ic_library_add,
                title = R.string.action_add_to_library
            ) {
                coroutineScope.launch {
                    songRepository.addAlbum(album)
                }
            }
        }

        GridMenuItem(
            icon = R.drawable.ic_playlist_add,
            title = R.string.menu_add_to_playlist,
            enabled = false
        ) {

        }
        GridMenuItem(
            icon = R.drawable.ic_file_download,
            title = R.string.menu_download,
            enabled = false
        ) {

        }
        album.artists?.let { artists ->
            GridMenuItem(
                icon = R.drawable.ic_artist,
                title = R.string.menu_view_artist
            ) {
                if (artists.size == 1) {
                    navController.navigate("artist/${artists[0].id}")
                    onDismiss()
                } else {
                    showSelectArtistDialog = true
                }
            }
        }
        GridMenuItem(
            icon = R.drawable.ic_share,
            title = R.string.menu_share
        ) {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, album.shareLink)
            }
            context.startActivity(Intent.createChooser(intent, null))
            onDismiss()
        }
    }
}

@Composable
fun YouTubeArtistMenu(
    artist: ArtistItem,
    playerConnection: PlayerConnection,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    GridMenu(
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        artist.radioEndpoint?.let { watchEndpoint ->
            GridMenuItem(
                icon = R.drawable.ic_radio,
                title = R.string.menu_start_radio
            ) {
                playerConnection.playQueue(YouTubeQueue(watchEndpoint))
                onDismiss()
            }
        }
        artist.shuffleEndpoint?.let { watchEndpoint ->
            GridMenuItem(
                icon = R.drawable.ic_shuffle,
                title = R.string.btn_shuffle
            ) {
                playerConnection.playQueue(YouTubeQueue(watchEndpoint))
                onDismiss()
            }
        }
        GridMenuItem(
            icon = R.drawable.ic_share,
            title = R.string.menu_share
        ) {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, artist.shareLink)
            }
            context.startActivity(Intent.createChooser(intent, null))
            onDismiss()
        }
    }
}
