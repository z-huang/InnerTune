package com.zionhuang.music.compose.menu

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
import androidx.navigation.NavController
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.ArtistItem
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.compose.component.GridMenu
import com.zionhuang.music.compose.component.GridMenuItem
import com.zionhuang.music.compose.component.ListDialog
import com.zionhuang.music.constants.ListItemHeight
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.PlayerConnection
import com.zionhuang.music.playback.queues.YouTubeQueue

@Composable
fun YouTubeSongMenu(
    song: SongItem,
    navController: NavController,
    playerConnection: PlayerConnection,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val artists = remember {
        song.artists.mapNotNull {
            it.navigationEndpoint?.browseEndpoint?.browseId?.let { artistId ->
                MediaMetadata.Artist(id = artistId, name = it.text)
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
            items(
                items = artists,
                key = { it.id }
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
        GridMenuItem(
            icon = R.drawable.ic_library_add,
            title = R.string.action_add_to_library,
            enabled = false
        ) {

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
                navController.navigate("album/${album.navigationEndpoint.browseId}")
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
        album.menu.radioEndpoint?.watchPlaylistEndpoint?.toWatchEndpoint()?.let { watchEndpoint ->
            GridMenuItem(
                icon = R.drawable.ic_radio,
                title = R.string.menu_start_radio
            ) {
                playerConnection.playQueue(YouTubeQueue(watchEndpoint))
                onDismiss()
            }
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
        GridMenuItem(
            icon = R.drawable.ic_library_add,
            title = R.string.action_add_to_library,
            enabled = false
        ) {

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
        album.menu.artistEndpoint?.browseEndpoint?.let { browseEndpoint ->
            GridMenuItem(
                icon = R.drawable.ic_artist,
                title = R.string.menu_view_artist
            ) {
                navController.navigate("artist/${browseEndpoint.browseId}")
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
        artist.menu.radioEndpoint?.watchPlaylistEndpoint?.toWatchEndpoint()?.let { watchEndpoint ->
            GridMenuItem(
                icon = R.drawable.ic_radio,
                title = R.string.menu_start_radio
            ) {
                playerConnection.playQueue(YouTubeQueue(watchEndpoint))
                onDismiss()
            }
        }
        artist.menu.shuffleEndpoint?.watchPlaylistEndpoint?.toWatchEndpoint()?.let { watchEndpoint ->
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