package com.zionhuang.music.ui.player

import android.content.Intent
import android.media.audiofx.AudioEffect
import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.ListItemHeight
import com.zionhuang.music.constants.ShowLyricsKey
import com.zionhuang.music.extensions.metadata
import com.zionhuang.music.extensions.togglePlayPause
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.playback.PlayerConnection
import com.zionhuang.music.ui.component.*
import com.zionhuang.music.utils.makeTimeString
import com.zionhuang.music.utils.rememberPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun Queue(
    state: BottomSheetState,
    playerBottomSheetState: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val menuState = LocalMenuState.current

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

    var showLyrics by rememberPreference(ShowLyricsKey, defaultValue = false)

    val sleepTimerEnabled = remember(playerConnection.songPlayer.sleepTimerTriggerTime, playerConnection.songPlayer.pauseWhenSongEnd) {
        playerConnection.songPlayer.sleepTimerTriggerTime != -1L || playerConnection.songPlayer.pauseWhenSongEnd
    }

    var sleepTimerTimeLeft by remember {
        mutableStateOf(0L)
    }

    LaunchedEffect(sleepTimerEnabled) {
        if (sleepTimerEnabled) {
            while (isActive) {
                sleepTimerTimeLeft = if (playerConnection.songPlayer.pauseWhenSongEnd) {
                    playerConnection.player.duration - playerConnection.player.currentPosition
                } else {
                    playerConnection.songPlayer.sleepTimerTriggerTime - System.currentTimeMillis()
                }
                delay(1000L)
            }
        }
    }

    var showSleepTimerDialog by remember {
        mutableStateOf(false)
    }

    var sleepTimerValue by remember {
        mutableStateOf(30f)
    }
    if (showSleepTimerDialog) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { showSleepTimerDialog = false },
            icon = { Icon(painter = painterResource(R.drawable.ic_bedtime), contentDescription = null) },
            title = { Text(stringResource(R.string.sleep_timer)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSleepTimerDialog = false
                        playerConnection.songPlayer.setSleepTimer(sleepTimerValue.roundToInt())
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSleepTimerDialog = false }
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = pluralStringResource(R.plurals.minute, sleepTimerValue.roundToInt(), sleepTimerValue.roundToInt()),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Slider(
                        value = sleepTimerValue,
                        onValueChange = { sleepTimerValue = it },
                        valueRange = 5f..120f,
                        steps = (120 - 5) / 5 - 1,
                    )

                    OutlinedButton(
                        onClick = {
                            showSleepTimerDialog = false
                            playerConnection.songPlayer.setSleepTimer(-1)
                        }
                    ) {
                        Text(stringResource(R.string.end_of_song))
                    }
                }
            }
        )
    }

    var showDetailsDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showDetailsDialog) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { showDetailsDialog = false },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = null
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showDetailsDialog = false }
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
                        stringResource(R.string.bitrate) to currentFormat?.bitrate?.let { "${it / 1000} Kbps" },
                        stringResource(R.string.sample_rate) to currentFormat?.sampleRate?.let { "$it Hz" },
                        stringResource(R.string.loudness) to currentFormat?.loudnessDb?.let { "$it dB" },
                        stringResource(R.string.volume) to "${(playerConnection.player.volume * 100).toInt()}%",
                        stringResource(R.string.file_size) to currentFormat?.contentLength?.let { Formatter.formatShortFileSize(context, it) }
                    ).forEach { (label, text) ->
                        val displayText = text ?: stringResource(R.string.unknown)
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(displayText))
                                    Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
                                }
                            )
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
                    .windowInsetsPadding(
                        WindowInsets.systemBars
                            .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                    )
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
                AnimatedContent(
                    targetState = sleepTimerEnabled
                ) { sleepTimerEnabled ->
                    if (sleepTimerEnabled) {
                        Text(
                            text = makeTimeString(sleepTimerTimeLeft),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = playerConnection.songPlayer::clearSleepTimer)
                                .padding(8.dp)
                        )
                    } else {
                        IconButton(onClick = { showSleepTimerDialog = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_bedtime),
                                contentDescription = null
                            )
                        }
                    }
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
                            PlayerMenu(
                                mediaMetadata = mediaMetadata,
                                navController = navController,
                                playerBottomSheetState = playerBottomSheetState,
                                playerConnection = playerConnection,
                                onShowDetailsDialog = { showDetailsDialog = true },
                                onDismiss = menuState::dismiss
                            )
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
            contentPadding = WindowInsets.systemBars
                .add(WindowInsets(
                    top = ListItemHeight,
                    bottom = ListItemHeight
                ))
                .asPaddingValues(),
            modifier = Modifier.nestedScroll(state.preUpPostDownNestedScrollConnection)
        ) {
            itemsIndexed(
                items = queueItems,
                key = { _, item -> item.uid.hashCode() }
            ) { index, window ->
                MediaMetadataListItem(
                    mediaMetadata = window.mediaItem.metadata!!,
                    isPlaying = index == currentWindowIndex,
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
                .windowInsetsPadding(
                    WindowInsets.systemBars
                        .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Text(
                    text = queueTitle.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = pluralStringResource(R.plurals.song_count, queueItems.size, queueItems.size),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = makeTimeString(queueLength * 1000L),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        val shuffleModeEnabled by playerConnection.shuffleModeEnabled.collectAsState()

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f))
                .fillMaxWidth()
                .height(ListItemHeight +
                        WindowInsets.systemBars
                            .asPaddingValues()
                            .calculateBottomPadding()
                )
                .align(Alignment.BottomCenter)
                .clickable {
                    state.collapseSoft()
                }
                .windowInsetsPadding(
                    WindowInsets.systemBars
                        .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                )
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

@Composable
fun PlayerMenu(
    mediaMetadata: MediaMetadata?,
    navController: NavController,
    playerBottomSheetState: BottomSheetState,
    playerConnection: PlayerConnection,
    onShowDetailsDialog: () -> Unit,
    onDismiss: () -> Unit,
) {
    mediaMetadata ?: return
    val context = LocalContext.current
    val playerVolume = playerConnection.songPlayer.playerVolume.collectAsState()
    val activityResultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    var showSelectArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSelectArtistDialog) {
        ListDialog(
            onDismiss = { showSelectArtistDialog = false }
        ) {
            items(mediaMetadata.artists) { artist ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .height(ListItemHeight)
                        .clickable {
                            navController.navigate("artist/${artist.id}")
                            showSelectArtistDialog = false
                            playerBottomSheetState.collapseSoft()
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

    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 12.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_volume_up),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )

        BigSeekBar(
            progressProvider = playerVolume::value,
            onProgressChange = { playerConnection.songPlayer.playerVolume.value = it },
            modifier = Modifier.weight(1f)
        )
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
            playerConnection.songPlayer.startRadioSeamlessly()
            onDismiss()
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
        GridMenuItem(
            icon = R.drawable.ic_artist,
            title = R.string.menu_view_artist
        ) {
            if (mediaMetadata.artists.size == 1) {
                navController.navigate("artist/${mediaMetadata.artists[0].id}")
                playerBottomSheetState.collapseSoft()
                onDismiss()
            } else {
                showSelectArtistDialog = true
            }
        }
        if (mediaMetadata.album != null) {
            GridMenuItem(
                icon = R.drawable.ic_album,
                title = R.string.menu_view_album
            ) {
                navController.navigate("album/${mediaMetadata.album.id}")
                playerBottomSheetState.collapseSoft()
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
                putExtra(Intent.EXTRA_TEXT, "https://music.youtube.com/watch?v=${mediaMetadata.id}")
            }
            context.startActivity(Intent.createChooser(intent, null))
            onDismiss()
        }
        GridMenuItem(
            icon = R.drawable.ic_info,
            title = R.string.menu_details
        ) {
            onShowDetailsDialog()
            onDismiss()
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
            onDismiss()
        }
    }
}
