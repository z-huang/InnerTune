package com.zionhuang.music.ui.screens.playlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastSumBy
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.AlbumThumbnailSize
import com.zionhuang.music.constants.ThumbnailCornerRadius
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.ui.component.*
import com.zionhuang.music.ui.menu.SongMenu
import com.zionhuang.music.utils.makeTimeString
import com.zionhuang.music.viewmodels.LocalPlaylistViewModel

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun LocalPlaylistScreen(
    appBarConfig: AppBarConfig,
    navController: NavController,
    viewModel: LocalPlaylistViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val playlist by viewModel.playlist.collectAsState()
    val songs by viewModel.playlistSongs.collectAsState()
    val playlistLength = remember(songs) {
        songs.fastSumBy { it.song.duration }
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(playlist) {
        appBarConfig.title = {
            Text(
                text = playlist?.playlist?.name.orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }

    LazyColumn(
        contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
    ) {
        playlist?.let { playlist ->
            item {
                if (playlist.songCount == 0) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_music_note),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.playlist_empty),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (playlist.thumbnails.size == 1) {
                                AsyncImage(
                                    model = playlist.thumbnails[0],
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(AlbumThumbnailSize)
                                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                )
                            } else if (playlist.thumbnails.size > 1) {
                                Box(
                                    modifier = Modifier
                                        .size(AlbumThumbnailSize)
                                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                ) {
                                    listOf(
                                        Alignment.TopStart,
                                        Alignment.TopEnd,
                                        Alignment.BottomStart,
                                        Alignment.BottomEnd
                                    ).fastForEachIndexed { index, alignment ->
                                        AsyncImage(
                                            model = playlist.thumbnails.getOrNull(index),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .align(alignment)
                                                .size(AlbumThumbnailSize / 2)
                                        )
                                    }
                                }
                            }

                            Column(
                                verticalArrangement = Arrangement.Center,
                            ) {
                                AutoResizeText(
                                    text = playlist.playlist.name,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSizeRange = FontSizeRange(16.sp, 22.sp)
                                )

                                Text(
                                    text = pluralStringResource(R.plurals.song_count, playlist.songCount, playlist.songCount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Normal
                                )

                                Text(
                                    text = makeTimeString(playlistLength * 1000L),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    playerConnection.playQueue(ListQueue(
                                        title = playlist.playlist.name,
                                        items = songs.map(Song::toMediaItem)
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
                                Text(stringResource(R.string.btn_play))
                            }

                            OutlinedButton(
                                onClick = {
                                    playerConnection.playQueue(ListQueue(
                                        title = playlist.playlist.name,
                                        items = songs.shuffled().map(Song::toMediaItem)
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
            }
        }

        itemsIndexed(
            items = songs
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
                            title = playlist!!.playlist.name,
                            items = songs.map { it.toMediaItem() },
                            startIndex = index
                        ))
                    }
                    .animateItemPlacement()
            )
        }
    }
}