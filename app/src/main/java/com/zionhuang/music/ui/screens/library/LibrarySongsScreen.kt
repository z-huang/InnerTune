package com.zionhuang.music.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.*
import com.zionhuang.music.extensions.mutablePreferenceState
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.models.sortInfo.SongSortType
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.ui.component.*
import com.zionhuang.music.ui.menu.SongMenu
import com.zionhuang.music.viewmodels.LibrarySongsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibrarySongsScreen(
    navController: NavController,
    viewModel: LibrarySongsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val items by viewModel.allSongs.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = WindowInsets.systemBars
                .add(WindowInsets(
                    top = AppBarHeight,
                    bottom = NavigationBarHeight + MiniPlayerHeight
                ))
                .asPaddingValues()
        ) {
            item(
                key = "header",
                contentType = CONTENT_TYPE_HEADER
            ) {
                SongHeader(itemCount = items.size)
            }

            itemsIndexed(
                items = items,
                key = { _, item -> item.id },
                contentType = { _, _ -> CONTENT_TYPE_SONG }
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
                                title = context.getString(R.string.queue_all_songs),
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
                    title = context.getString(R.string.queue_all_songs),
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SongHeader(
    itemCount: Int,
) {
    val (sortType, onSortTypeChange) = mutablePreferenceState(SONG_SORT_TYPE, SongSortType.CREATE_DATE)
    val (sortDescending, onSortDescendingChange) = mutablePreferenceState(SONG_SORT_DESCENDING, true)
    val (menuExpanded, onMenuExpandedChange) = remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
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
                        onSortTypeChange(type)
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
            onClick = { onSortDescendingChange(!sortDescending) }
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = pluralStringResource(R.plurals.song_count, itemCount, itemCount),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
