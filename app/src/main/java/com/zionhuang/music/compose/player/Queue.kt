package com.zionhuang.music.compose.player

import android.content.Intent
import android.media.audiofx.AudioEffect
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
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
    if (mediaMetadata == null) return
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val currentFormat by playerConnection.currentFormat.collectAsState(initial = null)

    var showLyrics by rememberPreference(SHOW_LYRICS, defaultValue = false)

    var openDetailsDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (openDetailsDialog) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { openDetailsDialog = false },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = null
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { openDetailsDialog = false }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .sizeIn(minWidth = 280.dp, maxWidth = 560.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    listOf(
                        stringResource(R.string.song_title) to mediaMetadata?.title,
                        stringResource(R.string.song_artists) to mediaMetadata?.artists?.joinToString { it.name },
                        stringResource(R.string.media_id) to mediaMetadata?.id,
                        "Itag" to currentFormat?.itag?.toString(),
                        stringResource(R.string.mime_type) to currentFormat?.mimeType,
                        stringResource(R.string.codecs) to currentFormat?.codecs,
                        stringResource(R.string.bitrate) to currentFormat?.bitrate?.let { "$it Kbps" },
                        stringResource(R.string.sample_rate) to currentFormat?.sampleRate?.let { "$it Hz" },
                        stringResource(R.string.loudness) to currentFormat?.loudnessDb?.let { "$it dB" },
                        stringResource(R.string.volume) to "${(playerConnection.player.volume * 100).toInt()}%",
                        stringResource(R.string.file_size) to currentFormat?.contentLength?.let { Formatter.formatShortFileSize(context, it) }
                    ).forEach { (label, text) ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = text ?: stringResource(R.string.unknown),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                    }
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
                            GridMenu(
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                GridMenuItem(
                                    icon = R.drawable.ic_radio,
                                    title = R.string.menu_start_radio
                                ) {
                                    playerConnection.songPlayer.startRadioSeamlessly()
                                    menuState.dismiss()
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
                                if (mediaMetadata?.album != null) {
                                    GridMenuItem(
                                        icon = R.drawable.ic_album,
                                        title = R.string.menu_view_album
                                    ) {
                                        navController.navigate("album/${mediaMetadata!!.album!!.id}")
                                        playerBottomSheetState.collapseSoft()
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
                                        putExtra(Intent.EXTRA_TEXT, "https://music.youtube.com/watch?v=${mediaMetadata!!.id}")
                                    }
                                    context.startActivity(Intent.createChooser(intent, null))
                                    menuState.dismiss()
                                }
                                GridMenuItem(
                                    icon = R.drawable.ic_info,
                                    title = R.string.menu_details
                                ) {
                                    openDetailsDialog = true
                                    menuState.dismiss()
                                }
                                GridMenuItem(
                                    icon = R.drawable.ic_equalizer,
                                    title = R.string.pref_equalizer_title
                                ) {
                                    val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                        putExtra(AudioEffect.EXTRA_AUDIO_SESSION, playerConnection.player.audioSessionId)
                                        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                                        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                                    }
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        activityResultLauncher.launch(intent)
                                    }
                                    menuState.dismiss()
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