package com.zionhuang.music.compose.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerAwareWindowInsets
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.SongListItem
import com.zionhuang.music.compose.component.shimmer.ListItemPlaceHolder
import com.zionhuang.music.compose.component.shimmer.ShimmerHost
import com.zionhuang.music.compose.component.shimmer.TextPlaceholder
import com.zionhuang.music.constants.AlbumThumbnailSize
import com.zionhuang.music.constants.CONTENT_TYPE_SHIMMER
import com.zionhuang.music.constants.CONTENT_TYPE_SONG
import com.zionhuang.music.constants.ThumbnailCornerRadius
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.utils.joinByBullet
import com.zionhuang.music.utils.makeTimeString
import com.zionhuang.music.viewmodels.AlbumViewModel
import com.zionhuang.music.viewmodels.AlbumViewModelFactory

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun AlbumScreen(
    albumId: String,
    playlistId: String?,
    viewModel: AlbumViewModel = viewModel(factory = AlbumViewModelFactory(
        context = LocalContext.current,
        albumId = albumId,
        playlistId = playlistId
    )),
) {
    val playerConnection = LocalPlayerConnection.current
    val playWhenReady by playerConnection.playWhenReady.collectAsState(initial = false)
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState(initial = null)

    val albumWithSongsState = viewModel.albumWithSongs.observeAsState()
    val albumWithSongs = remember(albumWithSongsState.value) {
        albumWithSongsState.value
    }

    LazyColumn(
        contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
    ) {
        if (albumWithSongs != null) {
            item(
                key = "header"
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .animateItemPlacement()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = albumWithSongs.album.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(AlbumThumbnailSize.dp)
                                .clip(RoundedCornerShape(ThumbnailCornerRadius.dp))
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
                key = { _, song -> song.id },
                contentType = { _, _ -> CONTENT_TYPE_SONG }
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
        } else {
            item(
                key = "shimmer",
                contentType = CONTENT_TYPE_SHIMMER
            ) {
                ShimmerHost(
                    modifier = Modifier.animateItemPlacement()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(12.dp)
                            .animateItemPlacement()
                    ) {
                        Spacer(
                            modifier = Modifier
                                .size(AlbumThumbnailSize.dp)
                                .clip(RoundedCornerShape(ThumbnailCornerRadius.dp))
                                .background(MaterialTheme.colorScheme.onSurface)
                        )

                        Spacer(Modifier.width(16.dp))

                        Column(
                            verticalArrangement = Arrangement.Center,
                        ) {
                            TextPlaceholder()
                            TextPlaceholder()
                            TextPlaceholder()
                        }
                    }

                    repeat(6) {
                        ListItemPlaceHolder()
                    }
                }
            }
        }
    }
}