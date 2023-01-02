package com.zionhuang.music.compose.component

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.shimmer.ShimmerHost
import com.zionhuang.music.compose.component.shimmer.TextPlaceholder
import com.zionhuang.music.compose.screens.settings.LyricsPosition
import com.zionhuang.music.compose.utils.fadingEdge
import com.zionhuang.music.constants.LYRICS_TEXT_POSITION
import com.zionhuang.music.db.entities.LyricsEntity
import com.zionhuang.music.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.zionhuang.music.extensions.mutablePreferenceState
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.utils.lyrics.LyricsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun Lyrics(
    sliderPositionProvider: () -> Long?,
    mediaMetadataProvider: () -> MediaMetadata,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val lyricsTextPosition by mutablePreferenceState(LYRICS_TEXT_POSITION, LyricsPosition.CENTER)

    val lyricsEntity by playerConnection.currentLyrics.collectAsState(initial = null)
    val lyrics = remember(lyricsEntity) {
        lyricsEntity?.lyrics
    }

    val lines = remember(lyrics) {
        if (lyrics == null || lyrics == LYRICS_NOT_FOUND) emptyList()
        else if (lyrics.startsWith("[")) listOf(LyricsEntry(0L, "")) + parseLyrics(lyrics)
        else lyrics.lines().mapIndexed { index, line -> LyricsEntry(index * 100L, line) }
    }
    val isSynced = remember(lyrics) {
        !lyrics.isNullOrEmpty() && lyrics.startsWith("[")
    }

    var currentLineIndex by remember {
        mutableStateOf(-1)
    }
    // Because LaunchedEffect has delay, which leads to inconsistent with current line color and scroll animation,
    // we use deferredCurrentLineIndex when user is scrolling
    var deferredCurrentLineIndex by rememberSaveable {
        mutableStateOf(0)
    }

    var lastPreviewTime by rememberSaveable {
        mutableStateOf(0L)
    }
    var isSeeking by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(lyrics) {
        if (lyrics.isNullOrEmpty() || !lyrics.startsWith("[")) {
            currentLineIndex = -1
            return@LaunchedEffect
        }
        while (isActive) {
            delay(50)
            val sliderPosition = sliderPositionProvider()
            isSeeking = sliderPosition != null
            currentLineIndex = findCurrentLineIndex(lines, sliderPosition ?: playerConnection.player.currentPosition)
        }
    }

    LaunchedEffect(isSeeking, lastPreviewTime) {
        if (isSeeking) {
            lastPreviewTime = 0L
        } else if (lastPreviewTime != 0L) {
            delay(LyricsPreviewTime)
            lastPreviewTime = 0L
        }
    }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(currentLineIndex, lastPreviewTime) {
        if (!isSynced) return@LaunchedEffect
        if (currentLineIndex != -1) {
            deferredCurrentLineIndex = currentLineIndex
            if (lastPreviewTime == 0L) {
                if (isSeeking) {
                    lazyListState.scrollToItem(currentLineIndex, with(density) { 36.dp.toPx().toInt() })
                } else {
                    lazyListState.animateScrollToItem(currentLineIndex, with(density) { 36.dp.toPx().toInt() })
                }
            }
        }
    }

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 12.dp)
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding = WindowInsets.systemBars
                .only(WindowInsetsSides.Top)
                .add(WindowInsets(top = maxHeight / 2, bottom = maxHeight / 2))
                .asPaddingValues(),
            modifier = Modifier
                .fadingEdge(vertical = 64.dp)
                .nestedScroll(remember {
                    object : NestedScrollConnection {
                        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                            lastPreviewTime = System.currentTimeMillis()
                            return super.onPostScroll(consumed, available, source)
                        }

                        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                            lastPreviewTime = System.currentTimeMillis()
                            return super.onPostFling(consumed, available)
                        }
                    }
                })
        ) {
            val displayedCurrentLineIndex = if (isSeeking) deferredCurrentLineIndex else currentLineIndex
            itemsIndexed(
                items = lines
            ) { index, item ->
                Text(
                    text = item.text,
                    fontSize = 20.sp,
                    color = if (index == displayedCurrentLineIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    textAlign = when (lyricsTextPosition) {
                        LyricsPosition.LEFT -> TextAlign.Left
                        LyricsPosition.CENTER -> TextAlign.Center
                        LyricsPosition.RIGHT -> TextAlign.Right
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isSynced) {
                            playerConnection.player.seekTo(item.time)
                            lastPreviewTime = 0L
                        }
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .alpha(if (!isSynced || index == displayedCurrentLineIndex) 1f else 0.5f)
                )
            }

            if (lyrics == null) {
                item {
                    ShimmerHost {
                        repeat(10) {
                            Box(
                                contentAlignment = when (lyricsTextPosition) {
                                    LyricsPosition.LEFT -> Alignment.CenterStart
                                    LyricsPosition.CENTER -> Alignment.Center
                                    LyricsPosition.RIGHT -> Alignment.CenterEnd
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 4.dp)
                            ) {
                                TextPlaceholder()
                            }
                        }
                    }
                }
            }
        }

        if (lyrics == LYRICS_NOT_FOUND) {
            Text(
                text = stringResource(R.string.lyrics_not_found),
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = when (lyricsTextPosition) {
                    LyricsPosition.LEFT -> TextAlign.Left
                    LyricsPosition.CENTER -> TextAlign.Center
                    LyricsPosition.RIGHT -> TextAlign.Right
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .alpha(0.5f)
            )
        }

        IconButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            onClick = {
                menuState.show {
                    LyricsMenu(
                        lyricsProvider = { lyricsEntity?.lyrics },
                        mediaMetadataProvider = mediaMetadataProvider,
                        coroutineScope = coroutineScope,
                        onDismiss = menuState::dismiss
                    )
                }
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more_horiz),
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsMenu(
    lyricsProvider: () -> String?,
    mediaMetadataProvider: () -> MediaMetadata,
    coroutineScope: CoroutineScope,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    var showEditDialog by rememberSaveable {
        mutableStateOf(false)
    }
    val (textFieldValue, onTextFieldValueChange) = rememberSaveable(showEditDialog, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = lyricsProvider().orEmpty()
            )
        )
    }

    if (showEditDialog) {
        DefaultDialog(
            onDismiss = { showEditDialog = false },
            icon = { Icon(painter = painterResource(R.drawable.ic_edit), contentDescription = null) },
            buttons = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                    }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }

                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            SongRepository(context).upsert(LyricsEntity(
                                id = mediaMetadataProvider().id,
                                lyrics = textFieldValue.text
                            ))
                        }
                        showEditDialog = false
                        onDismiss()
                    }
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            }
        ) {
            TextField(
                value = textFieldValue,
                onValueChange = onTextFieldValueChange,
                maxLines = 10,
                colors = TextFieldDefaults.outlinedTextFieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
                modifier = Modifier
                    .padding(all = 16.dp)
                    .weight(weight = 1f, fill = false)
            )
        }
    }

    var showSearchDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSearchDialog) {
        DefaultDialog(
            onDismiss = { showSearchDialog = false },
            icon = { Icon(painter = painterResource(R.drawable.ic_search), contentDescription = null) }
        ) {

        }
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
            icon = R.drawable.ic_edit,
            title = R.string.menu_edit
        ) {
            showEditDialog = true
        }
        GridMenuItem(
            icon = R.drawable.ic_cached,
            title = R.string.menu_refetch
        ) {
            val mediaMetadata = mediaMetadataProvider()
            coroutineScope.launch {
                SongRepository(context).deleteLyrics(mediaMetadata.id)
                LyricsHelper.loadLyrics(context, mediaMetadata)
            }
            onDismiss()
        }
        GridMenuItem(
            icon = R.drawable.ic_search,
            title = R.string.menu_search,
            enabled = false
        ) {
            showSearchDialog = true
        }
    }
}

private val LINE_REGEX = "((\\[\\d\\d:\\d\\d\\.\\d{2,3}\\])+)(.+)".toRegex()
private val TIME_REGEX = "\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})\\]".toRegex()

fun parseLyrics(lyrics: String): List<LyricsEntry> =
    lyrics.lines()
        .flatMap { line ->
            parseLine(line).orEmpty()
        }.sorted()

private fun parseLine(line: String): List<LyricsEntry>? {
    if (line.isEmpty()) {
        return null
    }
    val matchResult = LINE_REGEX.matchEntire(line.trim()) ?: return null
    val times = matchResult.groupValues[1]
    val text = matchResult.groupValues[3]
    val timeMatchResults = TIME_REGEX.findAll(times)

    return timeMatchResults.map { timeMatchResult ->
        val min = timeMatchResult.groupValues[1].toLong()
        val sec = timeMatchResult.groupValues[2].toLong()
        val milString = timeMatchResult.groupValues[3]
        var mil = milString.toLong()
        if (milString.length == 2) {
            mil *= 10
        }
        val time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil
        LyricsEntry(time, text)
    }.toList()
}

data class LyricsEntry(
    val time: Long,
    val text: String,
) : Comparable<LyricsEntry> {
    override fun compareTo(other: LyricsEntry): Int = (time - other.time).toInt()
}

fun findCurrentLineIndex(lines: List<LyricsEntry>, position: Long): Int {
    for (index in lines.indices) {
        if (lines[index].time >= position + animateScrollDuration) {
            return index - 1
        }
    }
    return lines.lastIndex
}

const val animateScrollDuration = 300L
val LyricsPreviewTime = 4.seconds