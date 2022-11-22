package com.zionhuang.music.compose.screens

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerAwareWindowInsets
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.SongListItem
import com.zionhuang.music.db.entities.AlbumWithSongs
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.utils.joinByBullet
import com.zionhuang.music.utils.makeTimeString

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun AlbumScreen(
    albumId: String,
) {
    val context = LocalContext.current
    val playerConnection = LocalPlayerConnection.current
    val playWhenReady by playerConnection.playWhenReady.collectAsState(initial = false)
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState(initial = null)

    var albumWithSongs: AlbumWithSongs? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        albumWithSongs = SongRepository(context).getAlbumWithSongs(albumId)
    }

    albumWithSongs?.let { albumWithSongs ->
        when (LocalConfiguration.current.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {}
            else -> {
                LazyColumn(
                    contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
                ) {
                    item {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = albumWithSongs.album.thumbnailUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(144.dp)
                                )

                                Spacer(Modifier.width(16.dp))

                                Column(
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = albumWithSongs.album.title,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(
                                        text = listOf(
                                            albumWithSongs.artists.joinToString { it.name },
                                            albumWithSongs.album.year.toString()
                                        ).joinByBullet(),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = listOf(
                                            pluralStringResource(R.plurals.song_count, albumWithSongs.album.songCount, albumWithSongs.album.songCount),
                                            makeTimeString(albumWithSongs.album.duration * 1000L)
                                        ).joinByBullet(),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Row {
                                        IconButton(onClick = { /*TODO*/ }) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_library_add_check),
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Row {
                                Button(
                                    onClick = {
                                        playerConnection.playQueue(ListQueue(
                                            title = albumWithSongs.album.title,
                                            items = albumWithSongs.songs.map { it.toMediaItem() }
                                        ))
                                    },
                                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_play),
                                        contentDescription = null,
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(
                                        text = stringResource(R.string.btn_play)
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                OutlinedButton(
                                    onClick = {
                                        playerConnection.playQueue(ListQueue(
                                            title = albumWithSongs.album.title,
                                            items = albumWithSongs.songs.shuffled().map { it.toMediaItem() }
                                        ))
                                    },
                                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_shuffle),
                                        contentDescription = null,
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(stringResource(R.string.btn_shuffle))
                                }
                            }
                        }
                    }

                    itemsIndexed(
                        items = albumWithSongs.songs,
                        key = { _, song -> song.id }
                    ) { index, song ->
                        SongListItem(
                            song = song,
                            playingIndicator = song.id == mediaMetadata?.id,
                            playWhenReady = playWhenReady,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable {
                                    playerConnection.playQueue(ListQueue(
                                        title = albumWithSongs.album.title,
                                        items = albumWithSongs.songs.map { it.toMediaItem() },
                                        startIndex = index
                                    ))
                                }
                                .animateItemPlacement()
                        )
                    }
                }
            }
        }
    }
}