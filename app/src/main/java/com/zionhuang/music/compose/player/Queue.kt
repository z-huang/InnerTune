package com.zionhuang.music.compose.player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.BottomSheet
import com.zionhuang.music.compose.component.BottomSheetState
import com.zionhuang.music.compose.component.MediaMetadataListItem
import com.zionhuang.music.compose.utils.rememberPreference
import com.zionhuang.music.constants.ListItemHeight
import com.zionhuang.music.constants.SHOW_LYRICS
import com.zionhuang.music.extensions.metadata
import com.zionhuang.music.extensions.togglePlayPause
import com.zionhuang.music.utils.joinByBullet
import com.zionhuang.music.utils.makeTimeString

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun Queue(
    sheetState: BottomSheetState,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current
    val playWhenReady by playerConnection.playWhenReady.collectAsState(initial = false)
    val queueTitle by playerConnection.queueTitle.collectAsState(initial = null)
    val queueItems by playerConnection.queueItems.collectAsState(initial = emptyList())
    val queueLength = remember(queueItems) {
        queueItems.sumOf { it.mediaItem.metadata!!.duration }
    }
    val currentWindowIndex by playerConnection.currentWindowIndex.collectAsState(initial = 0)
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)

    var showLyrics by rememberPreference(SHOW_LYRICS, defaultValue = false)

    BottomSheet(
        state = sheetState,
        backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation),
        modifier = modifier,
        collapsedContent = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.systemBars
                        .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                        .asPaddingValues())
            ) {
                IconButton(onClick = { sheetState.expandSoft() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_queue_music),
                        contentDescription = null
                    )
                }
                IconButton(onClick = { showLyrics = !showLyrics }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lyrics),
                        contentDescription = null,
                        modifier = Modifier.alpha(if (showLyrics) 1f else 0.5f)
                    )
                }
                IconButton(onClick = { playerConnection.toggleLibrary() }) {
                    Icon(
                        painter = painterResource(if (currentSong != null) R.drawable.ic_library_add_check else R.drawable.ic_library_add),
                        contentDescription = null
                    )
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_horiz),
                        contentDescription = null
                    )
                }
            }
        }
    ) {
        val lazyListState = rememberLazyListState(
            initialFirstVisibleItemIndex = currentWindowIndex
        )

        LazyColumn(
            state = lazyListState,
            contentPadding = WindowInsets.systemBars.add(WindowInsets(
                top = ListItemHeight.dp,
                bottom = ListItemHeight.dp)
            ).asPaddingValues(),
            modifier = Modifier.nestedScroll(sheetState.preUpPostDownNestedScrollConnection)
        ) {
            itemsIndexed(
                items = queueItems,
                key = { _, item -> item.uid.hashCode() }
            ) { index, window ->
                MediaMetadataListItem(
                    mediaMetadata = window.mediaItem.metadata!!,
                    playingIndicator = index == currentWindowIndex,
                    playWhenReady = playWhenReady,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement()
                        .clickable {
                            if (index == currentWindowIndex) {
                                playerConnection.player?.togglePlayPause()
                            } else {
                                playerConnection.player?.seekToDefaultPosition(window.firstPeriodIndex)
                                playerConnection.player?.playWhenReady = true
                            }
                        }
                )
            }
        }

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme
                    .surfaceColorAtElevation(NavigationBarDefaults.Elevation)
                    .copy(alpha = 0.95f))
                .padding(WindowInsets.systemBars
                    .only(WindowInsetsSides.Top + WindowInsetsSides.End)
                    .asPaddingValues())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(ListItemHeight.dp)
                    .padding(12.dp)
            ) {
                Text(
                    text = queueTitle.orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = listOf(
                        makeTimeString(queueLength * 1000L),
                        pluralStringResource(R.plurals.song_count, queueItems.size, queueItems.size)
                    ).joinByBullet(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        val shuffleModeEnabled by playerConnection.shuffleModeEnabled.collectAsState(initial = false)

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f))
                .fillMaxWidth()
                .height(ListItemHeight.dp + WindowInsets.systemBars
                    .asPaddingValues()
                    .calculateBottomPadding())
                .align(Alignment.BottomCenter)
                .clickable {
                    sheetState.collapseSoft()
                }
                .padding(WindowInsets.systemBars
                    .only(WindowInsetsSides.Bottom + WindowInsetsSides.End)
                    .asPaddingValues())
                .padding(12.dp)
        ) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = {
                    playerConnection.player?.let {
                        it.shuffleModeEnabled = !it.shuffleModeEnabled
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_shuffle),
                    contentDescription = null,
                    modifier = Modifier
                        .alpha(if (shuffleModeEnabled) 1f else 0.5f)
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_expand_more),
                contentDescription = null,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
