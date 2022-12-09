package com.zionhuang.music.compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.*
import com.zionhuang.music.compose.LocalPlayerAwareWindowInsets
import com.zionhuang.music.compose.component.YouTubeGridItem
import com.zionhuang.music.compose.component.YouTubeListItem
import com.zionhuang.music.compose.utils.rememberPreference
import com.zionhuang.music.constants.GridItemWidth
import com.zionhuang.music.constants.SONG_SORT_DESCENDING
import com.zionhuang.music.constants.SONG_SORT_TYPE
import com.zionhuang.music.extensions.getApplication
import com.zionhuang.music.models.sortInfo.SongSortType
import com.zionhuang.music.viewmodels.YouTubeBrowseViewModel
import com.zionhuang.music.viewmodels.YouTubeBrowseViewModelFactory

@Composable
fun HomeScreen(
    viewModel: YouTubeBrowseViewModel = viewModel(factory = YouTubeBrowseViewModelFactory(getApplication(), BrowseEndpoint(browseId = YouTube.HOME_BROWSE_ID))),
) {
    val sortType by rememberPreference(SONG_SORT_TYPE, SongSortType.CREATE_DATE)
    val sortDescending by rememberPreference(SONG_SORT_DESCENDING, true)
    val items = viewModel.pagingData.collectAsLazyPagingItems()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
        ) {
            items(
                items = items
            ) { ytBaseItem ->
                when (ytBaseItem) {
                    is AlbumOrPlaylistHeader -> {}
                    is ArtistHeader -> {}
                    is CarouselSection -> {
                        LazyHorizontalGrid(
                            rows = GridCells.Fixed(ytBaseItem.numItemsPerColumn),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        ) {
                            items(
                                items = ytBaseItem.items,
                                key = { it.id }
                            ) { item ->
                                when (item) {
                                    is YTItem -> if (ytBaseItem.itemViewType == YTBaseItem.ViewType.BLOCK) {
                                        YouTubeGridItem(
                                            item = item,
                                            modifier = Modifier.width(GridItemWidth)
                                        )
                                    } else {
                                        YouTubeListItem(
                                            item = item,
                                            modifier = Modifier.width(maxWidth * 0.9f)
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                    is DescriptionSection -> {
                        Text(
                            text = ytBaseItem.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    is GridSection -> {}
                    is Header -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = ytBaseItem.moreNavigationEndpoint != null) {

                                }
                                .padding(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = ytBaseItem.title,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                if (ytBaseItem.subtitle != null) {
                                    Text(
                                        text = ytBaseItem.subtitle!!,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                            if (ytBaseItem.moreNavigationEndpoint != null) {
                                Image(
                                    painter = painterResource(com.zionhuang.music.R.drawable.ic_navigate_next),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                    is NavigationItem -> {}
                    Separator -> {}
                    is SuggestionTextItem -> {}
                    is YTItem -> YouTubeListItem(
                        item = ytBaseItem,
                        modifier = Modifier.fillMaxWidth()
                    )

                    null -> {}
                }
            }
            when (items.loadState.append) {
                is LoadState.NotLoading -> Unit
                LoadState.Loading -> {
                    item {
                        Text("Loading")
                    }
                }
                is LoadState.Error -> {
                    item {
                        Text((items.loadState.append as LoadState.Error).error.message.toString())
                    }
                }
            }
        }
    }
}