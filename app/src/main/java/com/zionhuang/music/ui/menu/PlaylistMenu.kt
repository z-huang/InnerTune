package com.zionhuang.music.ui.menu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.utils.completed
import com.zionhuang.music.LocalDatabase
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.db.entities.PlaylistSongMap
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.ui.component.GridMenu
import com.zionhuang.music.ui.component.GridMenuItem
import com.zionhuang.music.ui.component.TextFieldDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PlaylistMenu(
    playlist: Playlist,
    coroutineScope: CoroutineScope,
    onDismiss: () -> Unit,
) {
    val database = LocalDatabase.current

    var showEditDialog by remember {
        mutableStateOf(false)
    }

    if (showEditDialog) {
        TextFieldDialog(
            icon = { Icon(painter = painterResource(R.drawable.edit), contentDescription = null) },
            title = { Text(text = stringResource(R.string.edit_playlist)) },
            onDismiss = { showEditDialog = false },
            initialTextFieldValue = TextFieldValue(playlist.playlist.name, TextRange(playlist.playlist.name.length)),
            onDone = { name ->
                onDismiss()
                database.query {
                    update(playlist.playlist.copy(name = name))
                }
            }
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
            icon = R.drawable.edit,
            title = R.string.edit
        ) {
            showEditDialog = true
        }

        if (playlist.playlist.browseId != null) {
            GridMenuItem(
                icon = R.drawable.sync,
                title = R.string.sync
            ) {
                onDismiss()
                coroutineScope.launch(Dispatchers.IO) {
                    val playlistPage = YouTube.playlist(playlist.playlist.browseId).completed().getOrNull() ?: return@launch
                    database.transaction {
                        clearPlaylist(playlist.id)
                        playlistPage.songs
                            .map(SongItem::toMediaMetadata)
                            .onEach(::insert)
                            .mapIndexed { position, song ->
                                PlaylistSongMap(
                                    songId = song.id,
                                    playlistId = playlist.id,
                                    position = position
                                )
                            }
                            .forEach(::insert)
                    }
                }
            }
        }

        GridMenuItem(
            icon = R.drawable.delete,
            title = R.string.delete
        ) {
            onDismiss()
            database.query {
                delete(playlist.playlist)
            }
        }
    }
}
