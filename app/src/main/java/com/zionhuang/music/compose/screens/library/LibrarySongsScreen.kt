package com.zionhuang.music.compose.screens.library

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerAwareWindowInsets
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.*
import com.zionhuang.music.compose.utils.rememberPreference
import com.zionhuang.music.constants.*
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.models.sortInfo.SongSortType
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun LibrarySongsScreen(
    navController: NavController,
    viewModel: SongsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val songRepository = SongRepository(context)
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState(initial = false)
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState(initial = null)

    var sortType by rememberPreference(SONG_SORT_TYPE, SongSortType.CREATE_DATE)
    var sortDescending by rememberPreference(SONG_SORT_DESCENDING, true)
    val items by viewModel.allSongsFlow.collectAsState(initial = emptyList())

    val queueTitle = stringResource(R.string.queue_all_songs)
    var sortTypeMenuExpanded by remember { mutableStateOf(false) }

    val onShowSongMenu = remember {
        { song: Song ->
            menuState.show {
                GridMenu(
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SongListItem(
                            song = song,
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            songRepository.toggleLiked(song)
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(if (song.song.liked) R.drawable.ic_favorite else R.drawable.ic_favorite_border),
                                        tint = MaterialTheme.colorScheme.error,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Divider(Modifier.padding(top = 6.dp, bottom = 8.dp))
                    }

                    GridMenuItem(
                        icon = R.drawable.ic_radio,
                        title = R.string.menu_start_radio
                    ) {
                        playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = song.id), song.toMediaMetadata()))
                        menuState.dismiss()
                    }
                    GridMenuItem(
                        icon = R.drawable.ic_playlist_play,
                        title = R.string.menu_play_next
                    ) {
                        playerConnection.playNext(song.toMediaItem())
                        menuState.dismiss()
                    }
                    GridMenuItem(
                        icon = R.drawable.ic_queue_music,
                        title = R.string.menu_add_to_queue
                    ) {
                        playerConnection.addToQueue((song.toMediaItem()))
                        menuState.dismiss()
                    }
                    GridMenuItem(
                        icon = R.drawable.ic_edit,
                        title = R.string.menu_edit
                    ) {

                    }
                    GridMenuItem(
                        icon = R.drawable.ic_playlist_add,
                        title = R.string.menu_add_to_playlist
                    ) {

                    }
                    GridMenuItem(
                        icon = R.drawable.ic_file_download,
                        title = R.string.menu_download
                    ) {

                    }
                    GridMenuItem(
                        icon = R.drawable.ic_artist,
                        title = R.string.menu_view_artist
                    ) {

                    }
                    if (song.song.albumId != null) {
                        GridMenuItem(
                            icon = R.drawable.ic_album,
                            title = R.string.menu_view_album
                        ) {
                            navController.navigate("album/${song.song.albumId}")
                            menuState.dismiss()
                        }
                    }
                    GridMenuItem(
                        icon = R.drawable.ic_share,
                        title = R.string.menu_share
                    ) {
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "https://music.youtube.com/watch?v=${song.id}")
                        }
                        context.startActivity(Intent.createChooser(intent, null))
                        menuState.dismiss()
                    }
                    GridMenuItem(
                        icon = R.drawable.ic_cached,
                        title = R.string.menu_refetch
                    ) {

                    }
                    GridMenuItem(
                        icon = R.drawable.ic_delete,
                        title = R.string.menu_delete
                    ) {

                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = WindowInsets.systemBars
                .add(WindowInsets(
                    top = AppBarHeight.dp,
                    bottom = NavigationBarHeight.dp + MiniPlayerHeight.dp
                ))
                .asPaddingValues()
        ) {
            item(
                key = "header",
                contentType = CONTENT_TYPE_HEADER
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(when (sortType) {
                            SongSortType.CREATE_DATE -> R.string.sort_by_create_date
                            SongSortType.NAME -> R.string.sort_by_name
                            SongSortType.ARTIST -> R.string.sort_by_artist
                            SongSortType.PLAY_TIME -> R.string.sort_by_play_time
                        }),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(bounded = false)
                            ) {
                                sortTypeMenuExpanded = !sortTypeMenuExpanded
                            }
                            .padding(8.dp)
                    )

                    DropdownMenu(
                        expanded = sortTypeMenuExpanded,
                        onDismissRequest = { sortTypeMenuExpanded = false },
                        modifier = Modifier.widthIn(min = 172.dp)
                    ) {
                        listOf(
                            SongSortType.CREATE_DATE to R.string.sort_by_create_date,
                            SongSortType.NAME to R.string.sort_by_name,
                            SongSortType.ARTIST to R.string.sort_by_artist,
                            SongSortType.PLAY_TIME to R.string.sort_by_play_time
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
                                    sortTypeMenuExpanded = false
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
                        text = pluralStringResource(R.plurals.song_count, items.size, items.size),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            itemsIndexed(
                items = items,
                key = { _, item -> item.id },
                contentType = { _, _ -> CONTENT_TYPE_SONG }
            ) { index, song ->
                SongListItem(
                    song = song,
                    playingIndicator = song.id == mediaMetadata?.id,
                    playWhenReady = playWhenReady,
                    onShowMenu = { onShowSongMenu(song) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable {
                            playerConnection.playQueue(ListQueue(
                                title = queueTitle,
                                items = items.map { it.toMediaItem() },
                                startIndex = index
                            ))
                        }
                        .animateItemPlacement()
                )
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                    .asPaddingValues())
                .padding(16.dp),
            onClick = {
                playerConnection.playQueue(ListQueue(
                    title = queueTitle,
                    items = items.shuffled().map { it.toMediaItem() },
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
