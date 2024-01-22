package com.zionhuang.music.ui.menu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.Event
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.ui.component.BottomSheetState
import com.zionhuang.music.ui.component.GridMenu
import com.zionhuang.music.ui.component.GridMenuItem

@Composable
fun LibrarySongMenu(
    mediaMetadata: MediaMetadata,
    navController: NavController,
    playerBottomSheetState: BottomSheetState? = null,
    event: Event? = null,
    onShowDetailsDialog: () -> Unit = {},
    onDismiss: () -> Unit
) {
    GenericSongMenu(
        mediaMetadata = mediaMetadata,
        navController = navController,
        playerBottomSheetState = playerBottomSheetState,
        event = event,
        onDismiss = onDismiss,
        topContent = { showExpanded: Boolean,
                       onToggle: (Boolean) -> Unit
            ->
            LibrarySongMenuTopContent(
                mediaMetadata = mediaMetadata,
                onDismiss = onDismiss
            )
        },
        topContentPane = {}
    )
}

@Composable
fun LibrarySongMenuTopContent(
    mediaMetadata: MediaMetadata,
    onDismiss: () -> Unit
) {
    val playerConnection = LocalPlayerConnection.current ?: return

    GridMenu(
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp
        )
    ) {
        GridMenuItem(
            icon = R.drawable.playlist_play,
            title = R.string.play_next
        ) {
            onDismiss()
            playerConnection.playNext(mediaMetadata.toMediaItem())
        }
        GridMenuItem(
            icon = R.drawable.queue_music,
            title = R.string.add_to_queue
        ) {
            onDismiss()
            playerConnection.addToQueue((mediaMetadata.toMediaItem()))
        }
        GridMenuItem(
            icon = R.drawable.radio,
            title = R.string.start_radio
        ) {
            onDismiss()
            playerConnection.playQueue(
                YouTubeQueue(
                    WatchEndpoint(videoId = mediaMetadata.id),
                    mediaMetadata
                )
            )
        }
    }
}
