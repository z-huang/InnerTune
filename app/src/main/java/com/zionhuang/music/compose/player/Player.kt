package com.zionhuang.music.compose.player

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.exoplayer2.Player.*
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.BottomSheet
import com.zionhuang.music.compose.component.BottomSheetState
import com.zionhuang.music.compose.component.IconButton
import com.zionhuang.music.compose.component.rememberBottomSheetState
import com.zionhuang.music.constants.QueuePeekHeight
import com.zionhuang.music.extensions.togglePlayPause
import com.zionhuang.music.utils.makeTimeString
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun BottomSheetPlayer(
    bottomSheetState: BottomSheetState,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current

    val playbackState by playerConnection.playbackState.collectAsState(initial = STATE_IDLE)
    val playWhenReady by playerConnection.playWhenReady.collectAsState(initial = false)
    val repeatMode by playerConnection.repeatMode.collectAsState(initial = REPEAT_MODE_OFF)
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState(initial = null)
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)

    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState(initial = true)
    val canSkipNext by playerConnection.canSkipNext.collectAsState(initial = true)

    var position by rememberSaveable(playbackState) {
        mutableStateOf(playerConnection.player?.currentPosition ?: 0L)
    }
    var sliderPosition by remember {
        mutableStateOf<Long?>(null)
    }

    LaunchedEffect(playbackState) {
        if (playbackState == STATE_READY) {
            while (isActive) {
                delay(100)
                position = playerConnection.player?.currentPosition ?: 0L
            }
        }
    }

    val queueSheetState = rememberBottomSheetState(
        dismissedBound = QueuePeekHeight.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        expandedBound = bottomSheetState.expandedBound,
    )

    BottomSheet(
        state = bottomSheetState,
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation),
        onDismiss = {
            playerConnection.player?.stop()
            playerConnection.player?.clearMediaItems()
        },
        collapsedContent = {
            MiniPlayer(
                mediaMetadata = mediaMetadata,
                playbackState = playbackState,
                playWhenReady = playWhenReady,
                position = position
            )
        }
    ) {
        val controlsContent: @Composable ColumnScope.() -> Unit = {
            Text(
                text = mediaMetadata?.title.orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = mediaMetadata?.artists?.joinToString { it.name }.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            Slider(
                value = (sliderPosition ?: position).toFloat(),
                valueRange = 0f..(mediaMetadata?.duration?.times(1000f) ?: 0f),
                enabled = mediaMetadata?.duration != null,
                onValueChange = {
                    sliderPosition = it.toLong()
                },
                onValueChangeFinished = {
                    sliderPosition?.let {
                        playerConnection.player?.seekTo(it)
                        position = it
                    }
                    sliderPosition = null
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = makeTimeString(sliderPosition ?: position),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                BasicText(
                    text = mediaMetadata?.duration?.times(1000L)?.let { makeTimeString(it) }.orEmpty(),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    IconButton(
                        icon = if (currentSong?.song?.liked == true) R.drawable.ic_favorite else R.drawable.ic_favorite_border,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                            .align(Alignment.Center),
                        onClick = {
                            playerConnection.toggleLike()
                        }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    IconButton(
                        icon = R.drawable.ic_skip_previous,
                        enabled = canSkipPrevious,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center),
                        onClick = {
                            playerConnection.player?.seekToPrevious()
                        }
                    )
                }

                Spacer(Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable {
                            playerConnection.player?.togglePlayPause()
                        }
                ) {
                    Image(
                        painter = painterResource(if (playWhenReady) R.drawable.ic_pause else R.drawable.ic_play),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    IconButton(
                        icon = R.drawable.ic_skip_next,
                        enabled = canSkipNext,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center),
                        onClick = {
                            playerConnection.player?.seekToNext()
                        }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    IconButton(
                        icon = when (repeatMode) {
                            REPEAT_MODE_OFF, REPEAT_MODE_ALL -> R.drawable.ic_repeat
                            REPEAT_MODE_ONE -> R.drawable.ic_repeat_one
                            else -> throw IllegalStateException()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                            .align(Alignment.Center)
                            .alpha(if (repeatMode == REPEAT_MODE_OFF) 0.5f else 1f),
                        onClick = {
                            playerConnection.toggleRepeatMode()
                        }
                    )
                }
            }
        }

        when (LocalConfiguration.current.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                Row(
                    modifier = Modifier
                        .padding(WindowInsets.systemBars
                            .only(WindowInsetsSides.Horizontal)
                            .add(WindowInsets(bottom = queueSheetState.collapsedBound))
                            .asPaddingValues())
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Thumbnail(
                            position = position,
                            sliderPosition = sliderPosition,
                            modifier = Modifier.nestedScroll(bottomSheetState.preUpPostDownNestedScrollConnection)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(WindowInsets.systemBars
                                .only(WindowInsetsSides.Top)
                                .asPaddingValues())
                    ) {
                        Spacer(Modifier.weight(1f))

                        controlsContent()

                        Spacer(Modifier.weight(1f))
                    }
                }
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(WindowInsets.systemBars
                            .only(WindowInsetsSides.Horizontal)
                            .add(WindowInsets(bottom = queueSheetState.collapsedBound))
                            .asPaddingValues())
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Thumbnail(
                            position = position,
                            sliderPosition = sliderPosition,
                            modifier = Modifier.nestedScroll(bottomSheetState.preUpPostDownNestedScrollConnection)
                        )
                    }

                    controlsContent()

                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        Queue(
            sheetState = queueSheetState
        )
    }
}
