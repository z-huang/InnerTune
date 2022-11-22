package com.zionhuang.music.compose.screens.library

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerAwareWindowInsets
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.IconButton
import com.zionhuang.music.compose.component.SongListItem
import com.zionhuang.music.compose.utils.rememberPreference
import com.zionhuang.music.constants.*
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.models.sortInfo.SongSortType
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.viewmodels.SongsViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun LibrarySongsScreen(
    viewModel: SongsViewModel = viewModel(),
) {
    val playerConnection = LocalPlayerConnection.current
    val playWhenReady by playerConnection.playWhenReady.collectAsState(initial = false)
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState(initial = null)

    var sortType by rememberPreference(SONG_SORT_TYPE, SongSortType.CREATE_DATE)
    var sortDescending by rememberPreference(SONG_SORT_DESCENDING, true)
    val items by viewModel.allSongsFlow.collectAsState(initial = emptyList())

    val queueTitle = stringResource(R.string.queue_all_songs)
    var sortTypeMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = WindowInsets.systemBars
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
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

                    IconButton(
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
                    .only(WindowInsetsSides.Bottom + WindowInsetsSides.End)
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
