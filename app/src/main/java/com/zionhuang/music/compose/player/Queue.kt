package com.zionhuang.music.compose.player

import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.*
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
    state: BottomSheetState,
    playerBottomSheetState: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val activityResultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState()
    val queueTitle by playerConnection.queueTitle.collectAsState()
    val queueItems by playerConnection.queueItems.collectAsState()
    val queueLength = remember(queueItems) {
        queueItems.sumOf { it.mediaItem.metadata!!.duration }
    }
    val currentWindowIndex by playerConnection.currentWindowIndex.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)

    var showLyrics by rememberPreference(SHOW_LYRICS, defaultValue = false)

    val menuItems = remember(mediaMetadata) {
        listOf(
            MenuItem(R.string.menu_start_radio, R.drawable.ic_radio) {

            },
            MenuItem(R.string.menu_add_to_playlist, R.drawable.ic_playlist_add) {

            },
            MenuItem(R.string.menu_download, R.drawable.ic_file_download) {

            },
            MenuItem(R.string.menu_view_artist, R.drawable.ic_artist) {

            },
            MenuItem(R.string.menu_view_album, R.drawable.ic_album, enabled = mediaMetadata?.album != null) {
                mediaMetadata?.album?.id?.let { albumId ->
                    navController.navigate("album/$albumId")
                    playerBottomSheetState.collapseSoft()
                    menuState.hide()
                }
            },
            MenuItem(R.string.menu_share, R.drawable.ic_share, enabled = mediaMetadata != null) {
                mediaMetadata?.id?.let { mediaId ->
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "https://music.youtube.com/watch?v=${mediaId}")
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                    menuState.hide()
                }
            },
            MenuItem(R.string.menu_details, R.drawable.ic_info) {

            },
            MenuItem(R.string.pref_equalizer_title, R.drawable.ic_equalizer) {
                val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                    putExtra(AudioEffect.EXTRA_AUDIO_SESSION, playerConnection.player.audioSessionId)
                    putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                    putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    activityResultLauncher.launch(intent)
                }
            }
        )
    }

    BottomSheet(
        state = state,
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
                IconButton(onClick = { state.expandSoft() }) {
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
                IconButton(onClick = playerConnection::toggleLibrary) {
                    Icon(
                        painter = painterResource(if (currentSong != null) R.drawable.ic_library_add_check else R.drawable.ic_library_add),
                        contentDescription = null
                    )
                }
                IconButton(
                    onClick = {
                        menuState.show {
                            menuItems.filter { it.enabled }.chunked(3).forEach { row ->
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .height(96.dp)
                                        .clip(ShapeDefaults.Large)
                                        .padding(horizontal = 8.dp)
                                ) {
                                    row.forEach { item ->
                                        Column(
                                            modifier = Modifier
                                                .clip(ShapeDefaults.Large)
                                                .weight(1f)
                                                .clickable(onClick = item.onClick)
                                                .padding(12.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f)
                                            ) {
                                                Icon(
                                                    painter = painterResource(item.icon),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Text(
                                                text = stringResource(item.title),
                                                style = MaterialTheme.typography.labelLarge,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }

                                    repeat(3 - row.size) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                ) {
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
            modifier = Modifier.nestedScroll(state.preUpPostDownNestedScrollConnection)
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
                                playerConnection.player.togglePlayPause()
                            } else {
                                playerConnection.player.seekToDefaultPosition(window.firstPeriodIndex)
                                playerConnection.player.playWhenReady = true
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
                    .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
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
                    state.collapseSoft()
                }
                .padding(WindowInsets.systemBars
                    .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                    .asPaddingValues())
                .padding(12.dp)
        ) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = {
                    playerConnection.player.shuffleModeEnabled = !playerConnection.player.shuffleModeEnabled
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
