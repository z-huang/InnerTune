package com.zionhuang.music.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.pages.AlbumPage
import com.zionhuang.music.LocalDatabase
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.AlbumThumbnailSize
import com.zionhuang.music.constants.CONTENT_TYPE_SONG
import com.zionhuang.music.constants.ThumbnailCornerRadius
import com.zionhuang.music.db.entities.AlbumWithSongs
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.ui.component.AutoResizeText
import com.zionhuang.music.ui.component.FontSizeRange
import com.zionhuang.music.ui.component.SongListItem
import com.zionhuang.music.ui.component.YouTubeListItem
import com.zionhuang.music.ui.component.shimmer.ButtonPlaceholder
import com.zionhuang.music.ui.component.shimmer.ListItemPlaceHolder
import com.zionhuang.music.ui.component.shimmer.ShimmerHost
import com.zionhuang.music.ui.component.shimmer.TextPlaceholder
import com.zionhuang.music.viewmodels.AlbumViewModel
import com.zionhuang.music.viewmodels.AlbumViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumScreen(
    navController: NavController,
    viewModel: AlbumViewModel = hiltViewModel(),
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val playWhenReady by playerConnection.playWhenReady.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val viewState by viewModel.viewState.collectAsState()
    val inLibrary by viewModel.inLibrary.collectAsState()

    LazyColumn(
        contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
    ) {
        viewState.let { viewState ->
            when (viewState) {
                is AlbumViewState.Local -> {
                    item {
                        LocalAlbumHeader(
                            albumWithSongs = viewState.albumWithSongs,
                            inLibrary = inLibrary,
                            navController = navController
                        )
                    }

                    itemsIndexed(
                        items = viewState.albumWithSongs.songs,
                        key = { _, song -> song.id }
                    ) { index, song ->
                        SongListItem(
                            song = song,
                            albumIndex = index + 1,
                            isPlaying = song.id == mediaMetadata?.id,
                            playWhenReady = playWhenReady,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable {
                                    playerConnection.playQueue(ListQueue(
                                        title = viewState.albumWithSongs.album.title,
                                        items = viewState.albumWithSongs.songs.map { it.toMediaItem() },
                                        startIndex = index
                                    ))
                                }
                        )
                    }
                }
                is AlbumViewState.Remote -> {
                    item {
                        RemoteAlbumHeader(
                            albumPage = viewState.albumPage,
                            inLibrary = inLibrary,
                            navController = navController
                        )
                    }

                    itemsIndexed(
                        items = viewState.albumPage.songs,
                        key = { _, song -> song.id },
                        contentType = { _, _ -> CONTENT_TYPE_SONG }
                    ) { index, song ->
                        YouTubeListItem(
                            item = song,
                            albumIndex = index + 1,
                            badges = {
                                if (song.explicit) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_explicit),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(end = 2.dp)
                                    )
                                }
                            },
                            isPlaying = song.id == mediaMetadata?.id,
                            playWhenReady = playWhenReady,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable {
                                    playerConnection.playQueue(ListQueue(
                                        title = viewState.albumPage.album.title,
                                        items = viewState.albumPage.songs.map { it.toMediaItem() },
                                        startIndex = index
                                    ))
                                }
                        )
                    }
                }
                null -> {
                    item {
                        ShimmerHost {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(
                                        modifier = Modifier
                                            .size(AlbumThumbnailSize)
                                            .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                            .background(MaterialTheme.colorScheme.onSurface)
                                    )

                                    Spacer(Modifier.width(16.dp))

                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        TextPlaceholder()
                                        TextPlaceholder()
                                        TextPlaceholder()
                                    }
                                }

                                Spacer(Modifier.padding(8.dp))

                                Row {
                                    ButtonPlaceholder(Modifier.weight(1f))

                                    Spacer(Modifier.width(12.dp))

                                    ButtonPlaceholder(Modifier.weight(1f))
                                }
                            }

                            repeat(6) {
                                ListItemPlaceHolder()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocalAlbumHeader(
    albumWithSongs: AlbumWithSongs,
    inLibrary: Boolean,
    navController: NavController,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val database = LocalDatabase.current

    Column(
        modifier = Modifier.padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = albumWithSongs.album.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(AlbumThumbnailSize)
                    .clip(RoundedCornerShape(ThumbnailCornerRadius))
            )

            Spacer(Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.Center,
            ) {
                AutoResizeText(
                    text = albumWithSongs.album.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSizeRange = FontSizeRange(16.sp, 22.sp)
                )

                val annotatedString = buildAnnotatedString {
                    withStyle(
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onBackground
                        ).toSpanStyle()
                    ) {
                        albumWithSongs.artists.fastForEachIndexed { index, artist ->
                            pushStringAnnotation(artist.id, artist.name)
                            append(artist.name)
                            pop()
                            if (index != albumWithSongs.artists.lastIndex) {
                                append(", ")
                            }
                        }
                    }
                }
                ClickableText(annotatedString) { offset ->
                    annotatedString.getStringAnnotations(offset, offset).firstOrNull()?.let { range ->
                        navController.navigate("artist/${range.tag}")
                    }
                }

                if (albumWithSongs.album.year != null) {
                    Text(
                        text = albumWithSongs.album.year.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            database.query {
                                if (inLibrary) {
                                    delete(albumWithSongs)
                                } else {
                                    insert(albumWithSongs)
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(if (inLibrary) R.drawable.ic_library_add_check else R.drawable.ic_library_add),
                            contentDescription = null
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    playerConnection.playQueue(ListQueue(
                        title = albumWithSongs.album.title,
                        items = albumWithSongs.songs.map(Song::toMediaItem)
                    ))
                },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play),
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    text = stringResource(R.string.btn_play)
                )
            }

            OutlinedButton(
                onClick = {
                    playerConnection.playQueue(ListQueue(
                        title = albumWithSongs.album.title,
                        items = albumWithSongs.songs.shuffled().map(Song::toMediaItem)
                    ))
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
                Text(stringResource(R.string.btn_shuffle))
            }
        }
    }
}

@Composable
fun RemoteAlbumHeader(
    albumPage: AlbumPage,
    inLibrary: Boolean,
    navController: NavController,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val database = LocalDatabase.current

    Column(
        modifier = Modifier.padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = albumPage.album.thumbnail,
                contentDescription = null,
                modifier = Modifier
                    .size(AlbumThumbnailSize)
                    .clip(RoundedCornerShape(ThumbnailCornerRadius))
            )

            Spacer(Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.Center,
            ) {
                AutoResizeText(
                    text = albumPage.album.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSizeRange = FontSizeRange(16.sp, 22.sp)
                )

                val annotatedString = buildAnnotatedString {
                    withStyle(
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onBackground
                        ).toSpanStyle()
                    ) {
                        albumPage.album.artists?.fastForEachIndexed { index, artist ->
                            if (artist.id != null) {
                                pushStringAnnotation(artist.id!!, artist.name)
                                append(artist.name)
                                pop()
                            } else {
                                append(artist.name)
                            }
                            if (index != albumPage.album.artists?.lastIndex) {
                                append(", ")
                            }
                        }
                    }
                }
                ClickableText(annotatedString) { offset ->
                    annotatedString.getStringAnnotations(offset, offset).firstOrNull()?.let { range ->
                        navController.navigate("artist/${range.tag}")
                    }
                }

                if (albumPage.album.year != null) {
                    Text(
                        text = albumPage.album.year.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            database.query {
                                if (inLibrary) {
                                    runBlocking(Dispatchers.IO) {
                                        albumWithSongs(albumPage.album.browseId).first()
                                    }?.let {
                                        delete(it)
                                    }
                                } else {
                                    insert(albumPage)
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(if (inLibrary) R.drawable.ic_library_add_check else R.drawable.ic_library_add),
                            contentDescription = null
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    playerConnection.playQueue(ListQueue(
                        title = albumPage.album.title,
                        items = albumPage.songs.map(SongItem::toMediaItem)
                    ))
                },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play),
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    text = stringResource(R.string.btn_play)
                )
            }

            OutlinedButton(
                onClick = {
                    playerConnection.playQueue(ListQueue(
                        title = albumPage.album.title,
                        items = albumPage.songs.shuffled().map(SongItem::toMediaItem)
                    ))
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
                Text(stringResource(R.string.btn_shuffle))
            }
        }
    }
}
