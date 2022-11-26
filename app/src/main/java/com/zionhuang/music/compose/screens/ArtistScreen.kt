package com.zionhuang.music.compose.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.zionhuang.innertube.models.*
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.YouTubeGridItem
import com.zionhuang.music.compose.component.YouTubeListItem
import com.zionhuang.music.constants.AppBarHeight
import com.zionhuang.music.constants.MiniPlayerHeight
import com.zionhuang.music.constants.NavigationBarHeight
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.viewmodels.ArtistViewModel
import com.zionhuang.music.viewmodels.ArtistViewModelFactory

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistScreen(
    navController: NavController,
    artistId: String,
    viewModel: ArtistViewModel = viewModel(factory = ArtistViewModelFactory(
        context = LocalContext.current,
        artistId = artistId
    )),
) {
    val playerConnection = LocalPlayerConnection.current
    val playWhenReady by playerConnection.playWhenReady.collectAsState(initial = false)
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState(initial = null)

    val artistHeaderState = viewModel.artistHeader.observeAsState()
    val artistHeader = remember(artistHeaderState.value) {
        artistHeaderState.value
    }
    val content by viewModel.content.observeAsState()

    LazyColumn(
        contentPadding = WindowInsets.systemBars
            .add(WindowInsets(
                top = AppBarHeight.dp,
                bottom = NavigationBarHeight.dp + MiniPlayerHeight.dp
            ))
            .asPaddingValues()
    ) {
        if (artistHeader != null) {
            item(key = "header") {
                Box {
                    AsyncImage(
                        model = artistHeader.bannerThumbnails?.lastOrNull()?.url,
                        contentDescription = null
                    )
                    Text(
                        text = artistHeader.name,
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        items(
            items = content!!,
            key = { it.id }
        ) { item ->
            when (item) {
                is Header -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = item.moreNavigationEndpoint != null) {

                            }
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            if (item.subtitle != null) {
                                Text(
                                    text = item.subtitle!!,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                        if (item.moreNavigationEndpoint != null) {
                            Image(
                                painter = painterResource(R.drawable.ic_navigate_next),
                                contentDescription = null
                            )
                        }
                    }
                }
                is YTItem -> {
                    YouTubeListItem(
                        item = item,
                        playingIndicator = when (item) {
                            is SongItem -> mediaMetadata?.id == item.id
                            is AlbumItem -> mediaMetadata?.album?.id == item.id
                            else -> false
                        },
                        playWhenReady = playWhenReady,
                        modifier = Modifier
                            .clickable {
                                when (item) {
                                    is SongItem -> playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = item.id)))
                                    is AlbumItem -> navController.navigate("album/${item.id}?playlistId=${item.playlistId}")
                                    is ArtistItem -> navController.navigate("artist/${item.id}")
                                    is PlaylistItem -> {}
                                }
                            }
                    )
                }
                is CarouselSection -> {
                    LazyRow {
                        items(item.items) { item ->
                            if (item is YTItem) {
                                YouTubeGridItem(
                                    item = item,
                                    playingIndicator = when (item) {
                                        is SongItem -> mediaMetadata?.id == item.id
                                        is AlbumItem -> mediaMetadata?.album?.id == item.id
                                        else -> false
                                    },
                                    playWhenReady = playWhenReady,
                                    modifier = Modifier
                                        .clickable {
                                            when (item) {
                                                is SongItem -> playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = item.id)))
                                                is AlbumItem -> navController.navigate("album/${item.id}?playlistId=${item.playlistId}")
                                                is ArtistItem -> navController.navigate("artist/${item.id}")
                                                is PlaylistItem -> {}
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
                is DescriptionSection -> {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                else -> {}
            }
        }
    }
}