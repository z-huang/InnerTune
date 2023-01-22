package com.zionhuang.music.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zionhuang.innertube.models.*
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.ui.component.LocalMenuState
import com.zionhuang.music.ui.component.YouTubeGridItem
import com.zionhuang.music.ui.menu.YouTubeAlbumMenu
import com.zionhuang.music.viewmodels.HomeViewModel
import com.zionhuang.music.viewmodels.MainViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val libraryAlbumIds by mainViewModel.libraryAlbumIds.collectAsState()

    val newReleaseAlbums by viewModel.newReleaseAlbums.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .widthIn(min = 84.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp))
                        .clickable {
                            navController.navigate("settings")
                        }
                        .padding(12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = null
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.title_settings),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            if (newReleaseAlbums.isNotEmpty()) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("new_release")
                            }
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.new_release_albums),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }

                        Icon(
                            painter = painterResource(R.drawable.ic_navigate_next),
                            contentDescription = null
                        )
                    }
                }

                item {
                    LazyRow {
                        items(
                            items = newReleaseAlbums,
                            key = { it.id }
                        ) { album ->
                            YouTubeGridItem(
                                item = album,
                                badges = {
                                    if (album.id in libraryAlbumIds) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_library_add_check),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .padding(end = 2.dp)
                                        )
                                    }
                                    if (album.explicit) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_explicit),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .padding(end = 2.dp)
                                        )
                                    }
                                },
                                isPlaying = mediaMetadata?.id == album.id,
                                playWhenReady = playWhenReady,
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            navController.navigate("album/${album.id}")
                                        },
                                        onLongClick = {
                                            menuState.show {
                                                YouTubeAlbumMenu(
                                                    album = album,
                                                    navController = navController,
                                                    playerConnection = playerConnection,
                                                    coroutineScope = coroutineScope,
                                                    onDismiss = menuState::dismiss
                                                )
                                            }
                                        }
                                    )
                                    .animateItemPlacement()
                            )
                        }
                    }
                }
            }
        }
    }
}
