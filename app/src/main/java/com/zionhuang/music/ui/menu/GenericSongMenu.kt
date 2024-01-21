package com.zionhuang.music.ui.menu

import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.music.LocalDatabase
import com.zionhuang.music.LocalDownloadUtil
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.ListItemHeight
import com.zionhuang.music.constants.ListThumbnailSize
import com.zionhuang.music.constants.ThumbnailCornerRadius
import com.zionhuang.music.db.entities.Event
import com.zionhuang.music.db.entities.PlaylistSongMap
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.ExoDownloadService
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.ui.component.BigSeekBar
import com.zionhuang.music.ui.component.BottomSheetState
import com.zionhuang.music.ui.component.DownloadGridMenu
import com.zionhuang.music.ui.component.GridMenu
import com.zionhuang.music.ui.component.GridMenuItem
import com.zionhuang.music.ui.component.ListDialog
import com.zionhuang.music.ui.component.ListItem
import com.zionhuang.music.ui.component.TextFieldDialog
import com.zionhuang.music.utils.joinByBullet
import com.zionhuang.music.utils.makeTimeString
import java.time.LocalDateTime
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round

@Composable
fun GenericSongMenu(
    mediaMetadata: MediaMetadata?,
    navController: NavController,
    playerBottomSheetState: BottomSheetState? = null,
    inPlayer: Boolean = false,
    event: Event? = null,
    onShowDetailsDialog: () -> Unit = {},
    onDismiss: () -> Unit
) {
    mediaMetadata ?: return
    val context = LocalContext.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val librarySong by database.song(mediaMetadata.id).collectAsState(initial = null)
    val download by LocalDownloadUtil.current.getDownload(mediaMetadata.id)
        .collectAsState(initial = null)
    val playerVolume = playerConnection.service.playerVolume.collectAsState()
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    val artists = remember {
        mediaMetadata.artists.mapNotNull {
            it.id?.let { artistId ->
                MediaMetadata.Artist(id = artistId, name = it.name)
            }
        }
    }

    val localDensity = LocalDensity.current

    var showEditDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showEditDialog) {
        TextFieldDialog(
            icon = { Icon(painter = painterResource(R.drawable.edit), contentDescription = null) },
            title = { Text(text = stringResource(R.string.edit_song)) },
            onDismiss = { showEditDialog = false },
            initialTextFieldValue = TextFieldValue(
                mediaMetadata.title,
                TextRange(mediaMetadata.title.length)
            ),
            onDone = { title ->
                onDismiss()
                database.query {
                    update(librarySong!!.song.copy(title = title))
                }
            }
        )
    }

    var showChoosePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    AddToPlaylistDialog(
        isVisible = showChoosePlaylistDialog,
        onAdd = { playlist ->
            database.transaction {
                insert(mediaMetadata)
                insert(
                    PlaylistSongMap(
                        songId = mediaMetadata.id,
                        playlistId = playlist.id,
                        position = playlist.songCount
                    )
                )
            }
        },
        onDismiss = {
            showChoosePlaylistDialog = false
        }
    )

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
                            playerBottomSheetState?.collapseSoft()
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

    var showPitchTempoDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showPitchTempoDialog) {
        PitchTempoDialog(
            onDismiss = { showPitchTempoDialog = false }
        )
    }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(start = 2.dp, end = 16.dp),
        title = mediaMetadata.title,
        subtitle = joinByBullet(
            mediaMetadata.artists.joinToString { it.name },
            mediaMetadata.duration?.let { makeTimeString(it * 1000L) }
        ),
        thumbnailContent = {
            AsyncImage(
                model = mediaMetadata.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(ListThumbnailSize)
                    .clip(RoundedCornerShape(ThumbnailCornerRadius))
            )
        },
        trailingContent = {
            IconButton(
                onClick = {
                    database.transaction {
                        librarySong.let { librarySong ->
                            if (librarySong == null) {
                                insert(mediaMetadata, SongEntity::toggleLike)
                            } else {
                                update(librarySong.song.toggleLike())
                            }
                        }
                    }
                }
            ) {
                Icon(
                    painter = painterResource(if (librarySong?.song?.liked == true) R.drawable.favorite else R.drawable.favorite_border),
                    tint = if (librarySong?.song?.liked == true) MaterialTheme.colorScheme.error else LocalContentColor.current,
                    contentDescription = null
                )
            }
        }
    )

    Divider()

    var showPlayerItems by rememberSaveable {
        mutableStateOf(false)
    }

    if (inPlayer) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 6.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.volume_up),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )

            BigSeekBar(
                progressProvider = playerVolume::value,
                onProgressChange = { playerConnection.service.playerVolume.value = it },
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    showPlayerItems = !showPlayerItems
                }
            ) {
                Icon(
                    painter = painterResource(if(showPlayerItems) R.drawable.expand_less else R.drawable.expand_more),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    var minHeightConstraint by remember { mutableStateOf(0.dp) }

    GridMenu(
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        ),
        modifier = Modifier
            .onGloballyPositioned { coords ->
                minHeightConstraint = with(localDensity) { coords.size.height.toDp() }
            }
            .defaultMinSize(minHeight = minHeightConstraint)
    ) {
        if (showPlayerItems) {
            GridMenuItem(
                icon = R.drawable.equalizer,
                title = R.string.equalizer
            ) {
                val intent =
                    Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                        putExtra(
                            AudioEffect.EXTRA_AUDIO_SESSION,
                            playerConnection.player.audioSessionId
                        )
                        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                    }
                if (intent.resolveActivity(context.packageManager) != null) {
                    activityResultLauncher.launch(intent)
                }
                onDismiss()
            }
            GridMenuItem(
                icon = R.drawable.tune,
                title = R.string.advanced
            ) {
                showPitchTempoDialog = true
            }
            GridMenuItem(
                icon = R.drawable.info,
                title = R.string.details
            ) {
                onShowDetailsDialog()
            }
        } else {
            if (!inPlayer) {
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
            GridMenuItem(
                icon = R.drawable.artist,
                title = R.string.view_artist,
                enabled = artists.isNotEmpty()
            ) {
                if (artists.size == 1) {
                    navController.navigate("artist/${artists[0].id}")
                    playerBottomSheetState?.collapseSoft()
                    onDismiss()
                } else {
                    showSelectArtistDialog = true
                }
            }
            if (mediaMetadata.album != null) {
                mediaMetadata.album?.let { album ->
                    GridMenuItem(
                        icon = R.drawable.album,
                        title = R.string.view_album
                    ) {
                        navController.navigate("album/${album.id}")
                        playerBottomSheetState?.collapseSoft()
                        onDismiss()
                    }
                }
            } else {
                GridMenuItem(
                    icon = R.drawable.album,
                    title = R.string.view_album,
                    enabled = false
                ) {}
            }
            GridMenuItem(
                icon = R.drawable.conversion_path,
                title = R.string.related,
                enabled = false
            ) {}
            if (librarySong?.song?.inLibrary != null) {
                GridMenuItem(
                    icon = R.drawable.library_add_check,
                    title = R.string.remove_from_library
                ) {
                    database.query {
                        inLibrary(mediaMetadata.id, null)
                    }
                }
            } else {
                GridMenuItem(
                    icon = R.drawable.library_add,
                    title = R.string.add_to_library
                ) {
                    database.transaction {
                        insert(mediaMetadata)
                        inLibrary(mediaMetadata.id, LocalDateTime.now())
                    }
                }
            }
            GridMenuItem(
                icon = R.drawable.playlist_add,
                title = R.string.add_to_playlist
            ) {
                showChoosePlaylistDialog = true
            }
            DownloadGridMenu(
                state = download?.state,
                onDownload = {
                    database.transaction {
                        insert(mediaMetadata)
                    }
                    val downloadRequest =
                        DownloadRequest.Builder(mediaMetadata.id, mediaMetadata.id.toUri())
                            .setCustomCacheKey(mediaMetadata.id)
                            .setData(mediaMetadata.title.toByteArray())
                            .build()
                    DownloadService.sendAddDownload(
                        context,
                        ExoDownloadService::class.java,
                        downloadRequest,
                        false
                    )
                },
                onRemoveDownload = {
                    DownloadService.sendRemoveDownload(
                        context,
                        ExoDownloadService::class.java,
                        mediaMetadata.id,
                        false
                    )
                }
            )
            GridMenuItem(
                icon = R.drawable.share,
                title = R.string.share
            ) {
                onDismiss()
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "https://music.youtube.com/watch?v=${mediaMetadata.id}"
                    )
                }
                context.startActivity(Intent.createChooser(intent, null))
            }
            if (librarySong?.song?.inLibrary != null) {
                GridMenuItem(
                    icon = R.drawable.edit,
                    title = R.string.edit
                ) {
                    showEditDialog = true
                }
            }
            if (librarySong != null && event != null) {
                GridMenuItem(
                    icon = R.drawable.delete,
                    title = R.string.remove_from_history
                ) {
                    onDismiss()
                    database.query {
                        delete(event)
                    }
                }
            }
        }
    }
}

@Composable
fun PitchTempoDialog(
    onDismiss: () -> Unit,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    var tempo by remember {
        mutableStateOf(playerConnection.player.playbackParameters.speed)
    }
    var transposeValue by remember {
        mutableStateOf(round(12 * log2(playerConnection.player.playbackParameters.pitch)).toInt())
    }
    val updatePlaybackParameters = {
        playerConnection.player.playbackParameters =
            PlaybackParameters(tempo, 2f.pow(transposeValue.toFloat() / 12))
    }

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(
                onClick = {
                    tempo = 1f
                    transposeValue = 0
                    updatePlaybackParameters()
                }
            ) {
                Text(stringResource(R.string.reset))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        text = {
            Column {
                ValueAdjuster(
                    icon = R.drawable.slow_motion_video,
                    currentValue = tempo,
                    values = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f),
                    onValueUpdate = {
                        tempo = it
                        updatePlaybackParameters()
                    },
                    valueText = { "x$it" },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                ValueAdjuster(
                    icon = R.drawable.discover_tune,
                    currentValue = transposeValue,
                    values = (-12..12).toList(),
                    onValueUpdate = {
                        transposeValue = it
                        updatePlaybackParameters()
                    },
                    valueText = { "${if (it > 0) "+" else ""}$it" }
                )
            }
        }
    )
}

@Composable
fun <T> ValueAdjuster(
    @DrawableRes icon: Int,
    currentValue: T,
    values: List<T>,
    onValueUpdate: (T) -> Unit,
    valueText: (T) -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )

        IconButton(
            enabled = currentValue != values.first(),
            onClick = {
                onValueUpdate(values[values.indexOf(currentValue) - 1])
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.remove),
                contentDescription = null
            )
        }

        Text(
            text = valueText(currentValue),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(80.dp)
        )

        IconButton(
            enabled = currentValue != values.last(),
            onClick = {
                onValueUpdate(values[values.indexOf(currentValue) + 1])
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.add),
                contentDescription = null
            )
        }
    }
}
