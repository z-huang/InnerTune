package com.zionhuang.music.compose.screens

import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.util.fastSumBy
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zionhuang.music.compose.LocalPlayerAwareWindowInsets
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.SongListItem
import com.zionhuang.music.viewmodels.LocalPlaylistViewModel

@Composable
fun LocalPlaylistScreen(
    playlistId: String,
    viewModel: LocalPlaylistViewModel = viewModel(factory = LocalPlaylistViewModel.Factory(
        context = LocalContext.current,
        playlistId = playlistId
    )),
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val playlist by viewModel.playlist.collectAsState()
    val songs by viewModel.playlistSongs.collectAsState()
    val songCount = remember(songs) {
        songs.size
    }
    val playlistLength = remember(songs) {
        songs.fastSumBy { it.song.duration }
    }

    LazyColumn(
        contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
    ) {
        items(
            items = songs
        ) { song ->
            SongListItem(
                song = song,
                isPlaying = song.id == mediaMetadata?.id,
                playWhenReady = playWhenReady
            )
        }
    }
}