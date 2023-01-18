package com.zionhuang.music.ui.screens

import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.util.fastSumBy
import androidx.hilt.navigation.compose.hiltViewModel
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.ui.component.SongListItem
import com.zionhuang.music.viewmodels.LocalPlaylistViewModel

@Composable
fun LocalPlaylistScreen(
    viewModel: LocalPlaylistViewModel = hiltViewModel(),
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