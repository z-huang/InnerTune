package com.zionhuang.music.compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.zionhuang.innertube.models.*
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.*
import com.zionhuang.music.compose.utils.fadingEdge
import com.zionhuang.music.compose.utils.resize
import com.zionhuang.music.constants.AppBarHeight
import com.zionhuang.music.constants.MiniPlayerHeight
import com.zionhuang.music.constants.NavigationBarHeight
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.viewmodels.ArtistViewModel
import com.zionhuang.music.viewmodels.ArtistViewModelFactory

@Composable
fun ArtistScreen(
    artistId: String,
    navController: NavController,
    appBarConfig: AppBarConfig,
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

    val lazyListState = rememberLazyListState()

    val transparentAppBar by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0
        }
    }
    LaunchedEffect(transparentAppBar) {
        appBarConfig.transparentBackground = transparentAppBar
    }
    LaunchedEffect(artistHeader) {
        appBarConfig.title = {
            Text(
                text = if (!transparentAppBar) artistHeader?.name.orEmpty() else "",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(
            bottom = NavigationBarHeight.dp + MiniPlayerHeight.dp
        )
    ) {
        if (artistHeader != null) {
            item(key = "header") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3)
                ) {
                    AsyncImage(
                        model = artistHeader.bannerThumbnails?.lastOrNull()?.url?.resize(1200, 900),
                        contentDescription = null,
                        modifier = Modifier.fadingEdge(
                            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + AppBarHeight.dp,
                            bottom = 108.dp
                        )
                    )
                    AutoResizeText(
                        text = artistHeader.name,
                        style = MaterialTheme.typography.displayLarge,
                        fontSizeRange = FontSizeRange(36.sp, 58.sp),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 48.dp)
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
                        modifier = Modifier
                            .padding(12.dp)
                    )
                }
                else -> {}
            }
        }
    }
}