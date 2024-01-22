package com.zionhuang.music.ui.menu

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.zionhuang.music.LocalDatabase
import com.zionhuang.music.LocalDownloadUtil
import com.zionhuang.music.R
import com.zionhuang.music.constants.ListItemHeight
import com.zionhuang.music.constants.ListThumbnailSize
import com.zionhuang.music.constants.ThumbnailCornerRadius
import com.zionhuang.music.db.entities.Event
import com.zionhuang.music.db.entities.PlaylistSongMap
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.playback.ExoDownloadService
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

enum class ContentPanes {
    Main, Overflow, Topmenu
}

@Composable
fun GenericSongMenu(
    mediaMetadata: MediaMetadata,
    navController: NavController,
    playerBottomSheetState: BottomSheetState? = null,
    event: Event? = null,
    onDismiss: () -> Unit,
    topContent: @Composable (showExpanded: Boolean, onToggle: (Boolean) -> Unit) -> Unit,
    topContentPane: @Composable () -> Unit
) {
    val database = LocalDatabase.current
    val librarySong by database.song(mediaMetadata.id).collectAsState(initial = null)

    var expandedPane by remember {
        mutableStateOf(ContentPanes.Main)
    }

    val localDensity = LocalDensity.current
    var minHeightConstraint by remember { mutableStateOf(0.dp) }

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

    Column(
        modifier = Modifier
            .onGloballyPositioned { coords ->
                minHeightConstraint = with(localDensity) { coords.size.height.toDp() }
            }
            .defaultMinSize(minHeight = minHeightConstraint)
    )
    {
        topContent(
            showExpanded = expandedPane == ContentPanes.Topmenu,
            onToggle = { expanded ->
                expandedPane = if (expanded) ContentPanes.Topmenu else ContentPanes.Main
            }
        )

        when (expandedPane) {
            ContentPanes.Main -> SongMenuCommonContent(
                mediaMetadata = mediaMetadata,
                navController = navController,
                playerBottomSheetState = playerBottomSheetState,
                onDismiss = onDismiss
            )

            ContentPanes.Overflow -> SongMenuCommonOverflow(
                mediaMetadata = mediaMetadata,
                event = event,
                onDismiss = onDismiss
            )

            ContentPanes.Topmenu -> topContentPane()
        }
    }

    Divider()

    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                expandedPane =
                    if (expandedPane == ContentPanes.Overflow) ContentPanes.Main
                    else ContentPanes.Overflow
            }
            .padding(horizontal = 24.dp)
            .padding(
                top = 16.dp,
                bottom = 8.dp + WindowInsets.systemBars
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
    ) {
        Icon(
            painter = painterResource(R.drawable.more_vert),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = "More",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            painter = painterResource(
                if (expandedPane == ContentPanes.Overflow) R.drawable.expand_more
                else R.drawable.expand_less
            ),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun SongMenuCommonContent(
    mediaMetadata: MediaMetadata,
    navController: NavController,
    playerBottomSheetState: BottomSheetState? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val librarySong by database.song(mediaMetadata.id).collectAsState(initial = null)
    val download by LocalDownloadUtil.current.getDownload(mediaMetadata.id)
        .collectAsState(initial = null)

    val artists = remember {
        mediaMetadata.artists.mapNotNull {
            it.id?.let { artistId ->
                MediaMetadata.Artist(id = artistId, name = it.name)
            }
        }
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

    GridMenu(
        contentPadding = PaddingValues(
            start = 8.dp,
            end = 8.dp,
            bottom = 16.dp
        )
    ) {
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
    }
}

@Composable
fun SongMenuCommonOverflow(
    mediaMetadata: MediaMetadata?,
    event: Event? = null,
    onDismiss: () -> Unit
) {
    mediaMetadata ?: return
    val context = LocalContext.current
    val database = LocalDatabase.current
    val librarySong by database.song(mediaMetadata.id).collectAsState(initial = null)

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

    GridMenu(
        contentPadding = PaddingValues(
            start = 8.dp,
            end = 8.dp,
        )
    ) {
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