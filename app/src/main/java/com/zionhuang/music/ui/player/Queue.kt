package com.zionhuang.music.ui.player

import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachReversed
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.source.ShuffleOrder.DefaultShuffleOrder
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.ListItemHeight
import com.zionhuang.music.constants.LockQueueKey
import com.zionhuang.music.constants.ShowLyricsKey
import com.zionhuang.music.extensions.metadata
import com.zionhuang.music.extensions.move
import com.zionhuang.music.extensions.togglePlayPause
import com.zionhuang.music.playback.QueueEntry
import com.zionhuang.music.playback.computeMoveSequence
import com.zionhuang.music.playback.moveQueueEntry
import com.zionhuang.music.ui.component.BottomSheet
import com.zionhuang.music.ui.component.BottomSheetState
import com.zionhuang.music.ui.component.LocalMenuState
import com.zionhuang.music.ui.component.MediaMetadataListItem
import com.zionhuang.music.ui.menu.MediaMetadataMenu
import com.zionhuang.music.ui.menu.PlayerMenu
import com.zionhuang.music.ui.menu.QueueSelectionMenu
import com.zionhuang.music.utils.joinByBullet
import com.zionhuang.music.utils.makeTimeString
import com.zionhuang.music.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * A single LazyColumn item in the queue list, joining [QueueEntry] (the logical grouping
 * structure) back with the actual [Timeline.Window] data needed to render it. Purely a
 * per-composition derived view -- not a new source of truth -- recomputed from [QueueEntry]
 * and queueWindows. A [Group] holds ALL of its member windows so the whole group renders (and
 * is dragged) as one atomic LazyColumn item -- there are no separate top-level rows for
 * individual group members.
 */
private sealed interface QueueRow {
    data class Song(val window: Timeline.Window) : QueueRow
    data class Group(val groupId: String, val title: String, val windows: List<Timeline.Window>) : QueueRow
}

// Group keys include the first member's flat index, not just groupId: the same groupId can
// legitimately appear as two separate QueueEntry.Group instances (an interrupted/repeated
// group id, see QueueEntry's grouping rules), which would otherwise produce two rows with an
// identical "groupId"-only key and crash LazyColumn's duplicate-key check.
private fun QueueRow.key(): Any = when (this) {
    is QueueRow.Song -> window.firstPeriodIndex
    is QueueRow.Group -> "$groupId:${windows.first().firstPeriodIndex}"
}

@Composable
private fun QueueGroupHeader(
    title: String,
    songCount: Int,
    modifier: Modifier = Modifier,
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(ListItemHeight)
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.album),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pluralStringResource(R.plurals.n_song, songCount, songCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        trailingContent()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Queue(
    state: BottomSheetState,
    playerBottomSheetState: BottomSheetState,
    backgroundColor: Color,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val menuState = LocalMenuState.current

    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()

    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val currentSong by playerConnection.currentSong.collectAsState(initial = null)

    var showLyrics by rememberPreference(ShowLyricsKey, defaultValue = false)
    var lockQueue by rememberPreference(LockQueueKey, defaultValue = false)

    var inSelectMode by remember {
        mutableStateOf(false)
    }
    val selection = remember { mutableStateListOf<Int>() }
    val onExitSelectionMode = {
        inSelectMode = false
        selection.clear()
    }
    if (inSelectMode) {
        BackHandler(onBack = onExitSelectionMode)
    }

    val sleepTimerEnabled = remember(playerConnection.service.sleepTimer.triggerTime, playerConnection.service.sleepTimer.pauseWhenSongEnd) {
        playerConnection.service.sleepTimer.isActive
    }

    var sleepTimerTimeLeft by remember {
        mutableLongStateOf(0L)
    }

    LaunchedEffect(sleepTimerEnabled) {
        if (sleepTimerEnabled) {
            while (isActive) {
                sleepTimerTimeLeft = if (playerConnection.service.sleepTimer.pauseWhenSongEnd) {
                    playerConnection.player.duration - playerConnection.player.currentPosition
                } else {
                    playerConnection.service.sleepTimer.triggerTime - System.currentTimeMillis()
                }
                delay(1000L)
            }
        }
    }

    var showSleepTimerDialog by remember { mutableStateOf(false) }
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            onDismiss = { showSleepTimerDialog = false }
        )
    }

    var showDetailsDialog by remember { mutableStateOf(false) }
    if (showDetailsDialog) {
        DetailsDialog(
            onDismiss = { showDetailsDialog = false }
        )
    }

    BottomSheet(
        state = state,
        backgroundColor = backgroundColor,
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
                        painter = painterResource(R.drawable.queue_music),
                        contentDescription = null
                    )
                }

                IconButton(onClick = { showLyrics = !showLyrics }) {
                    Icon(
                        painter = painterResource(R.drawable.lyrics),
                        contentDescription = null,
                        modifier = Modifier.alpha(if (showLyrics) 1f else 0.5f)
                    )
                }

                AnimatedContent(
                    label = "sleepTimer",
                    targetState = sleepTimerEnabled
                ) { sleepTimerEnabled ->
                    if (sleepTimerEnabled) {
                        Text(
                            text = makeTimeString(sleepTimerTimeLeft),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = playerConnection.service.sleepTimer::clear)
                                .padding(8.dp)
                        )
                    } else {
                        IconButton(onClick = { showSleepTimerDialog = true }) {
                            Icon(
                                painter = painterResource(R.drawable.bedtime),
                                contentDescription = null
                            )
                        }
                    }
                }

                IconButton(onClick = playerConnection::toggleLibrary) {
                    Icon(
                        painter = painterResource(if (currentSong?.song?.inLibrary != null) R.drawable.library_add_check else R.drawable.library_add),
                        contentDescription = null
                    )
                }

                IconButton(
                    onClick = {
                        menuState.show {
                            PlayerMenu(
                                mediaMetadata = mediaMetadata,
                                navController = navController,
                                bottomSheetState = playerBottomSheetState,
                                onShowDetailsDialog = { showDetailsDialog = true },
                                onDismiss = menuState::dismiss
                            )
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more_horiz),
                        contentDescription = null
                    )
                }
            }
        }
    ) {
        val queueTitle by playerConnection.queueTitle.collectAsState()
        val queueWindows by playerConnection.queueWindows.collectAsState()
        val queueEntries by playerConnection.queueEntries.collectAsState()
        val currentMediaItemIndex by playerConnection.currentMediaItemIndex.collectAsState()
        val mutableRows = remember { mutableStateListOf<QueueRow>() }
        val queueLength = remember(queueWindows) {
            queueWindows.sumOf { it.mediaItem.metadata!!.duration }
        }

        // Derived rows: exactly one QueueRow per QueueEntry (Stage 4's single source of truth
        // for grouping), joined back with the actual Timeline.Window data, keyed by flat index
        // (firstPeriodIndex) -- never by position in queueWindows, which is display/shuffle
        // ordered. A Group holds ALL of its member windows so it renders -- and, via the
        // reorderable library, drags -- as one atomic LazyColumn item (Stage 7). An entry whose
        // flat index(es) briefly have no matching window (e.g. mid-update) is dropped rather
        // than crashing; it reappears once queueWindows/queueEntries settle back in sync.
        val rows = remember(queueEntries, queueWindows) {
            val windowByFlatIndex = queueWindows.associateBy { it.firstPeriodIndex }
            queueEntries.mapNotNull { entry ->
                when (entry) {
                    is QueueEntry.SingleSong -> windowByFlatIndex[entry.flatIndex]?.let { QueueRow.Song(it) }
                    is QueueEntry.Group -> {
                        val windows = entry.flatIndices.mapNotNull { windowByFlatIndex[it] }
                        windows.takeIf { it.isNotEmpty() }?.let { QueueRow.Group(entry.groupId, entry.title, it) }
                    }
                }
            }
        }

        val coroutineScope = rememberCoroutineScope()
        val lazyListState = rememberLazyListState()
        var dragInfo by remember {
            mutableStateOf<Pair<Int, Int>?>(null)
        }
        val reorderableState = rememberReorderableLazyListState(
            lazyListState = lazyListState,
            scrollThresholdPadding = WindowInsets.systemBars.add(
                WindowInsets(
                    top = ListItemHeight,
                    bottom = ListItemHeight
                )
            ).asPaddingValues()
        ) { from, to ->
            val currentDragInfo = dragInfo
            dragInfo = if (currentDragInfo == null) {
                from.index to to.index
            } else {
                currentDragInfo.first to to.index
            }

            mutableRows.move(from.index, to.index)
        }

        // Entry-level move commit. For a queue with no groups at all, this is byte-identical to
        // the pre-Stage-7 algorithm: row position == entry index == flat index, so from/to are
        // used directly, unchanged. Once any group exists anywhere in the queue, an entry-index
        // position no longer equals a flat index (a group "eats" several flat-index slots for
        // one entry position), so the move is computed at the QueueEntry level (moveQueueEntry,
        // which moves whichever entry from/to refer to as one block, preserving every group's
        // internal order) and only then translated into real flat-index operations: either a
        // physical moveMediaItem sequence (unshuffled -- computeMoveSequence derives it so only
        // the already-relied-on single-item Player.moveMediaItem API is used, not the less
        // certain range-based moveMediaItems) or a rebuilt DefaultShuffleOrder (shuffled),
        // exactly mirroring the two mechanisms the flat algorithm already used.
        LaunchedEffect(reorderableState.isAnyItemDragging) {
            if (!reorderableState.isAnyItemDragging) {
                dragInfo?.let { (from, to) ->
                    if (queueEntries.none { it is QueueEntry.Group }) {
                        if (!playerConnection.player.shuffleModeEnabled) {
                            playerConnection.player.moveMediaItem(from, to)
                        } else {
                            playerConnection.player.setShuffleOrder(
                                DefaultShuffleOrder(
                                    queueWindows.map { it.firstPeriodIndex }.toMutableList().move(from, to).toIntArray(),
                                    System.currentTimeMillis()
                                )
                            )
                        }
                    } else {
                        val newFlatOrder = moveQueueEntry(queueEntries, from, to)
                        if (!playerConnection.player.shuffleModeEnabled) {
                            computeMoveSequence(newFlatOrder).forEach { (moveFrom, moveTo) ->
                                playerConnection.player.moveMediaItem(moveFrom, moveTo)
                            }
                        } else {
                            playerConnection.player.setShuffleOrder(
                                DefaultShuffleOrder(newFlatOrder, System.currentTimeMillis())
                            )
                        }
                    }
                    dragInfo = null
                }
            }
        }

        LaunchedEffect(rows) {
            mutableRows.apply {
                clear()
                addAll(rows)
            }
            selection.fastForEachReversed { uidHash ->
                if (queueWindows.find { it.uid.hashCode() == uidHash } == null) {
                    selection.remove(uidHash)
                }
            }
        }

        LaunchedEffect(mutableRows) {
            val targetIndex = mutableRows.indexOfFirst { row ->
                when (row) {
                    is QueueRow.Song -> row.window.firstPeriodIndex == currentMediaItemIndex
                    is QueueRow.Group -> row.windows.any { it.firstPeriodIndex == currentMediaItemIndex }
                }
            }
            if (targetIndex != -1) {
                lazyListState.scrollToItem(targetIndex)
            }
        }

        // Shared song-row content, used both for a standalone QueueRow.Song and for each member
        // window inside a QueueRow.Group's Column -- identical tap/highlight/swipe/menu behavior
        // either way. dragHandle defaults to none for group members: only the group's own header
        // (rendered separately, see below) is draggable, so a member's internal position within
        // its group can never change via drag. dragHandle is a lambda parameter (not a Boolean
        // gating an internal Modifier.draggableHandle() call) because draggableHandle() requires
        // the ReorderableCollectionItemScope implicit receiver from the enclosing ReorderableItem
        // block; SongRow is a plain local function and doesn't have it, but a lambda literal
        // supplied by each call site -- still lexically inside that block -- does.
        @Composable
        fun SongRow(window: Timeline.Window, dragHandle: @Composable () -> Unit = {}) {
            val isActive = window.firstPeriodIndex == currentMediaItemIndex
            val currentItem by rememberUpdatedState(window)
            val dismissState = rememberSwipeToDismissBoxState(
                positionalThreshold = { totalDistance -> totalDistance },
                confirmValueChange = { dismissValue ->
                    if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                        playerConnection.player.removeMediaItem(currentItem.firstPeriodIndex)
                    }
                    true
                }
            )

            val onCheckedChange: (Boolean) -> Unit = {
                if (it) {
                    selection.add(window.uid.hashCode())
                } else {
                    selection.remove(window.uid.hashCode())
                }
            }

            val content = @Composable {
                MediaMetadataListItem(
                    mediaMetadata = window.mediaItem.metadata!!,
                    isActive = isActive,
                    isPlaying = isPlaying,
                    trailingContent = {
                        if (inSelectMode) {
                            Checkbox(
                                checked = window.uid.hashCode() in selection,
                                onCheckedChange = onCheckedChange
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    menuState.show {
                                        MediaMetadataMenu(
                                            mediaMetadata = window.mediaItem.metadata!!,
                                            navController = navController,
                                            bottomSheetState = state,
                                            onDismiss = menuState::dismiss,
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.more_vert),
                                    contentDescription = null
                                )
                            }

                            if (!lockQueue) {
                                dragHandle()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                if (inSelectMode) {
                                    onCheckedChange(window.uid.hashCode() !in selection)
                                } else {
                                    coroutineScope.launch(Dispatchers.Main) {
                                        if (isActive) {
                                            playerConnection.player.togglePlayPause()
                                        } else {
                                            playerConnection.player.seekToDefaultPosition(window.firstPeriodIndex)
                                            playerConnection.player.playWhenReady = true
                                        }
                                    }
                                }
                            },
                            onLongClick = {
                                if (!inSelectMode) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    inSelectMode = true
                                    onCheckedChange(true)
                                }
                            }
                        )
                )
            }

            if (!lockQueue && !inSelectMode) {
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {},
                    content = { content() }
                )
            } else {
                content()
            }
        }

        LazyColumn(
            state = lazyListState,
            contentPadding = WindowInsets.systemBars
                .add(
                    WindowInsets(
                        top = ListItemHeight,
                        bottom = ListItemHeight
                    )
                )
                .asPaddingValues(),
            modifier = Modifier.nestedScroll(state.preUpPostDownNestedScrollConnection)
        ) {
            itemsIndexed(
                items = mutableRows,
                key = { _, row -> row.key() }
            ) { _, row ->
                ReorderableItem(
                    state = reorderableState,
                    key = row.key()
                ) {
                    when (row) {
                        is QueueRow.Song -> SongRow(
                            window = row.window,
                            dragHandle = {
                                IconButton(
                                    onClick = { },
                                    modifier = Modifier.draggableHandle()
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.drag_handle),
                                        contentDescription = null
                                    )
                                }
                            }
                        )

                        is QueueRow.Group -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                QueueGroupHeader(
                                    title = row.title,
                                    songCount = row.windows.size,
                                    trailingContent = {
                                        if (!lockQueue && !inSelectMode) {
                                            IconButton(
                                                onClick = { },
                                                modifier = Modifier.draggableHandle()
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.drag_handle),
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    }
                                )
                                row.windows.forEach { window ->
                                    key(window.uid.hashCode()) {
                                        SongRow(window = window)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f)
                )
                .windowInsetsPadding(
                    WindowInsets.systemBars
                        .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(ListItemHeight)
                    .padding(horizontal = 6.dp)
            ) {
                if (inSelectMode) {
                    IconButton(onClick = onExitSelectionMode) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = null,
                        )
                    }
                    Text(
                        text = pluralStringResource(R.plurals.n_selected, selection.size, selection.size),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Checkbox(
                        checked = queueWindows.size == selection.size,
                        onCheckedChange = {
                            if (queueWindows.size == selection.size) {
                                selection.clear()
                            } else {
                                selection.clear()
                                selection.addAll(queueWindows.map { it.uid.hashCode() })
                            }
                        }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .weight(1f)
                    ) {
                        if (!queueTitle.isNullOrEmpty()) {
                            Text(
                                text = queueTitle.orEmpty(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = joinByBullet(pluralStringResource(R.plurals.n_song, queueWindows.size, queueWindows.size), makeTimeString(queueLength * 1000L)),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        val shuffleModeEnabled by playerConnection.shuffleModeEnabled.collectAsState()

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f))
                .fillMaxWidth()
                .height(
                    ListItemHeight +
                            WindowInsets.systemBars
                                .asPaddingValues()
                                .calculateBottomPadding()
                )
                .align(Alignment.BottomCenter)
                .clickable {
                    onExitSelectionMode()
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
                    coroutineScope.launch {
                        // Anticipates the post-toggle scroll target from current state (same
                        // approach as before Stage 5). With groups present, this can be off by
                        // the number of group headers preceding the target row -- an accepted,
                        // cosmetic-only approximation; exact group-aware positioning is Stage 6+.
                        lazyListState.animateScrollToItem(
                            (if (playerConnection.player.shuffleModeEnabled) playerConnection.player.currentMediaItemIndex else 0)
                                .coerceIn(0, (mutableRows.size - 1).coerceAtLeast(0))
                        )
                    }.invokeOnCompletion {
                        playerConnection.player.shuffleModeEnabled = !playerConnection.player.shuffleModeEnabled
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.shuffle),
                    contentDescription = null,
                    modifier = Modifier.alpha(if (shuffleModeEnabled) 1f else 0.5f)
                )
            }

            Icon(
                painter = painterResource(R.drawable.expand_more),
                contentDescription = null,
                modifier = Modifier.align(Alignment.Center)
            )

            if (inSelectMode) {
                IconButton(
                    enabled = selection.size > 0,
                    onClick = {
                        menuState.show {
                            QueueSelectionMenu(
                                selection = selection.mapNotNull { uidHash ->
                                    mutableRows.firstNotNullOfOrNull { row ->
                                        when (row) {
                                            is QueueRow.Song -> row.window.takeIf { it.uid.hashCode() == uidHash }
                                            is QueueRow.Group -> row.windows.firstOrNull { it.uid.hashCode() == uidHash }
                                        }
                                    }
                                },
                                onExitSelectionMode = onExitSelectionMode,
                                onDismiss = menuState::dismiss,
                            )
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert),
                        contentDescription = null,
                    )
                }
            } else {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = {
                        lockQueue = !lockQueue
                    }
                ) {
                    Icon(
                        painter = if (lockQueue) painterResource(R.drawable.lock) else painterResource(R.drawable.lock_open),
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
fun SleepTimerDialog(
    onDismiss: () -> Unit,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    var sleepTimerValue by remember {
        mutableFloatStateOf(30f)
    }

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss,
        icon = { Icon(painter = painterResource(R.drawable.bedtime), contentDescription = null) },
        title = { Text(stringResource(R.string.sleep_timer)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    playerConnection.service.sleepTimer.start(sleepTimerValue.roundToInt())
                }
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val pluralString = pluralStringResource(R.plurals.minute, sleepTimerValue.roundToInt(), sleepTimerValue.roundToInt())
                val endTimeString = SimpleDateFormat
                    .getTimeInstance(SimpleDateFormat.SHORT, Locale.getDefault())
                    .format(Date(System.currentTimeMillis() + (sleepTimerValue.roundToInt() * 60 * 1000).toLong()))

                Text(
                    text = "$pluralString\n$endTimeString",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Slider(
                    value = sleepTimerValue,
                    onValueChange = { sleepTimerValue = it },
                    valueRange = 5f..120f,
                )

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        playerConnection.service.sleepTimer.start(-1)
                    }
                ) {
                    Text(stringResource(R.string.end_of_song))
                }
            }
        }
    )
}

@Composable
fun DetailsDialog(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val currentFormat by playerConnection.currentFormat.collectAsState(initial = null)

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.info),
                contentDescription = null
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
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