package com.zionhuang.music.compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer
import com.zionhuang.innertube.models.*
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerConnection
import com.zionhuang.music.compose.component.*
import com.zionhuang.music.compose.component.shimmer.ButtonPlaceholder
import com.zionhuang.music.compose.component.shimmer.ListItemPlaceHolder
import com.zionhuang.music.compose.component.shimmer.ShimmerHost
import com.zionhuang.music.compose.component.shimmer.TextPlaceholder
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
        contentPadding = WindowInsets.systemBars
            .only(WindowInsetsSides.Bottom)
            .add(WindowInsets(
                bottom = NavigationBarHeight.dp + MiniPlayerHeight.dp
            ))
            .asPaddingValues()
    ) {
        if (artistHeader != null) {
            item(key = "header") {
                Column {
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
                                bottom = 64.dp
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

                    Row(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        artistHeader.shuffleEndpoint?.watchEndpoint?.let { shuffleEndpoint ->
                            Button(
                                onClick = {
                                    playerConnection.playQueue(YouTubeQueue(shuffleEndpoint))
                                },
                                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_shuffle),
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text(
                                    text = stringResource(R.string.btn_shuffle)
                                )
                            }
                        }

                        if (artistHeader.shuffleEndpoint != null && artistHeader.radioEndpoint != null) {
                            Spacer(Modifier.width(12.dp))
                        }

                        artistHeader.radioEndpoint?.watchEndpoint?.let { radioEndpoint ->
                            OutlinedButton(
                                onClick = {
                                    playerConnection.playQueue(YouTubeQueue(radioEndpoint))
                                },
                                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_radio),
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text(stringResource(R.string.btn_radio))
                            }
                        }
                    }
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

        if (artistHeader == null) {
            item(key = "shimmer") {
                ShimmerHost {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3)
                    ) {
                        Spacer(
                            modifier = Modifier
                                .shimmer()
                                .background(MaterialTheme.colorScheme.onSurface)
                                .fadingEdge(
                                    top = WindowInsets.systemBars
                                        .asPaddingValues()
                                        .calculateTopPadding() + AppBarHeight.dp,
                                    bottom = 108.dp
                                )
                        )
                        TextPlaceholder(
                            height = 56.dp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 48.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        ButtonPlaceholder(Modifier.weight(1f))

                        Spacer(Modifier.width(12.dp))

                        ButtonPlaceholder(Modifier.weight(1f))
                    }

                    repeat(6) {
                        ListItemPlaceHolder()
                    }
                }
            }
        }
    }
}