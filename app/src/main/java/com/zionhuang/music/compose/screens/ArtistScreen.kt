package com.zionhuang.music.compose.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zionhuang.music.viewmodels.ArtistViewModel
import com.zionhuang.music.viewmodels.ArtistViewModelFactory

@Composable
fun ArtistScreen(
    artistId: String,
    viewModel: ArtistViewModel = viewModel(factory = ArtistViewModelFactory(
        context = LocalContext.current,
        artistId = artistId
    )),
) {
    val artistHeaderState = viewModel.artistHeader.observeAsState()
    val artistHeader = remember(artistHeaderState.value) {
        artistHeaderState.value
    }
    val content = viewModel.content.observeAsState()

    LazyColumn {
        if (artistHeader!=null) {
            item {

            }
        }
    }
}