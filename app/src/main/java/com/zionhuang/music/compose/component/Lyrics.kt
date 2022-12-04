package com.zionhuang.music.compose.component

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.shimmer.ShimmerHost
import com.zionhuang.music.compose.component.shimmer.TextPlaceholder
import com.zionhuang.music.compose.utils.fadingEdge
import com.zionhuang.music.compose.utils.rememberPreference
import com.zionhuang.music.constants.LRC_TEXT_POS
import com.zionhuang.music.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun Lyrics(
    lyrics: String?,
    playerPosition: Long,
    sliderPosition: Long?,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val lines = remember(lyrics) {
        if (lyrics == null || lyrics == LYRICS_NOT_FOUND) emptyList()
        else if (lyrics.startsWith("[")) listOf(LyricsEntry(0L, "")) + parseLyrics(lyrics)
        else lyrics.lines().mapIndexed { index, line -> LyricsEntry(index * 100L, line) }
    }
    val isSynced = remember(lyrics) {
        !lyrics.isNullOrEmpty() && lyrics.startsWith("[")
    }
    val lyricsTextPosition by rememberPreference(LRC_TEXT_POS, 1)

    val position = sliderPosition ?: playerPosition
    val currentLineIndex = rememberSaveable(position, lyrics) {
        if (lyrics.isNullOrEmpty() || !lyrics.startsWith("[")) {
            return@rememberSaveable -1
        }
        lines.forEachIndexed { index, line ->
            if (line.time >= position + animateScrollDuration) return@rememberSaveable index - 1
        }
        lines.lastIndex
    }
    // Because LaunchedEffect has delay, which leads to inconsistent with current line color and scroll animation,
    // we use deferredCurrentLineIndex when user is scrolling
    var deferredCurrentLineIndex by rememberSaveable {
        mutableStateOf(0)
    }

    val density = LocalDensity.current
    val lazyListState = rememberLazyListState()

    var lastPreviewTime by rememberSaveable {
        mutableStateOf(0L)
    }

    if (sliderPosition != null) {
        lastPreviewTime = 0L
    }

    LaunchedEffect(lastPreviewTime) {
        if (lastPreviewTime != 0L) {
            delay(LyricsPreviewTime)
            lastPreviewTime = 0L
        }
    }

    LaunchedEffect(currentLineIndex, lastPreviewTime) {
        if (!isSynced) return@LaunchedEffect
        if (currentLineIndex != -1) {
            deferredCurrentLineIndex = currentLineIndex
            if (lastPreviewTime == 0L) {
                if (sliderPosition != null) {
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
            .fadingEdge(vertical = 64.dp)
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding = WindowInsets.systemBars
                .only(WindowInsetsSides.Top)
                .add(WindowInsets(top = maxHeight / 2, bottom = maxHeight / 2))
                .asPaddingValues(),
            modifier = Modifier.nestedScroll(remember {
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
            val displayedCurrentLineIndex = if (sliderPosition != null) deferredCurrentLineIndex else currentLineIndex
            itemsIndexed(
                items = lines
            ) { index, item ->
                Text(
                    text = item.text,
                    fontSize = 20.sp,
                    color = if (index == displayedCurrentLineIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    textAlign = when (lyricsTextPosition) {
                        0 -> TextAlign.Left
                        1 -> TextAlign.Center
                        2 -> TextAlign.Right
                        else -> TextAlign.Center
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
                                    0 -> Alignment.CenterStart
                                    1 -> Alignment.Center
                                    2 -> Alignment.CenterEnd
                                    else -> Alignment.Center
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
                    0 -> TextAlign.Left
                    1 -> TextAlign.Center
                    2 -> TextAlign.Right
                    else -> TextAlign.Center
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .alpha(0.5f)
            )
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

const val animateScrollDuration = 300L
val LyricsPreviewTime = 4.seconds