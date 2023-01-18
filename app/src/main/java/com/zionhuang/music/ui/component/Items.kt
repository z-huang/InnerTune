package com.zionhuang.music.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import coil.compose.AsyncImage
import com.zionhuang.innertube.models.*
import com.zionhuang.music.R
import com.zionhuang.music.constants.*
import com.zionhuang.music.db.entities.Album
import com.zionhuang.music.db.entities.Artist
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.utils.joinByBullet
import com.zionhuang.music.utils.makeTimeString

@Composable
inline fun ListItem(
    modifier: Modifier = Modifier,
    title: String,
    noinline subtitle: (@Composable RowScope.() -> Unit)? = null,
    thumbnailContent: @Composable () -> Unit,
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(ListItemHeight)
            .padding(horizontal = 6.dp),
    ) {
        Box(
            modifier = Modifier.padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            thumbnailContent()
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subtitle != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    subtitle()
                }
            }
        }

        trailingContent()
    }
}

@Composable
inline fun ListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    crossinline badges: @Composable RowScope.() -> Unit = {},
    thumbnailContent: @Composable () -> Unit,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = title,
    subtitle = {
        badges()

        if (!subtitle.isNullOrEmpty()) {
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    },
    thumbnailContent = thumbnailContent,
    trailingContent = trailingContent,
    modifier = modifier
)

@Composable
inline fun GridItem(
    modifier: Modifier = Modifier,
    title: String,
    noinline subtitle: (@Composable RowScope.() -> Unit)? = null,
    thumbnailContent: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.padding(12.dp)
    ) {
        Box {
            thumbnailContent()
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (subtitle != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                subtitle()
            }
        }
    }
}

@Composable
inline fun GridItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    crossinline badges: @Composable RowScope.() -> Unit = {},
    thumbnailContent: @Composable () -> Unit,
) = GridItem(
    modifier = modifier,
    title = title,
    subtitle = {
        badges()

        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    },
    thumbnailContent = thumbnailContent,
)

@Composable
inline fun SongListItem(
    song: Song,
    modifier: Modifier = Modifier,
    albumIndex: Int? = null,
    showBadges: Boolean = true,
    isPlaying: Boolean = false,
    playWhenReady: Boolean = false,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = song.song.title,
    subtitle = joinByBullet(
        song.artists.joinToString(),
        song.album?.title,
        makeTimeString(song.song.duration * 1000L)
    ),
    badges = {
        if (showBadges && song.song.liked) {
            Icon(
                painter = painterResource(R.drawable.ic_favorite),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(18.dp)
                    .padding(end = 2.dp)
            )
        }
    },
    thumbnailContent = {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(ListThumbnailSize)
        ) {
            if (albumIndex != null) {
                AnimatedVisibility(
                    visible = !isPlaying,
                    enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
                    exit = shrinkOut(shrinkTowards = Alignment.Center) + fadeOut()
                ) {
                    Text(
                        text = albumIndex.toString(),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                AsyncImage(
                    model = song.song.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                )
            }

            PlayingIndicatorBox(
                isPlaying = isPlaying,
                playWhenReady = playWhenReady,
                color = if (albumIndex != null) MaterialTheme.colorScheme.onBackground else Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = if (albumIndex != null) Color.Transparent else Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(ThumbnailCornerRadius)
                    )
            )
        }
    },
    trailingContent = trailingContent,
    modifier = modifier
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
inline fun ArtistListItem(
    artist: Artist,
    modifier: Modifier = Modifier,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = artist.artist.name,
    subtitle = pluralStringResource(R.plurals.song_count, artist.songCount, artist.songCount),
    thumbnailContent = {
        AsyncImage(
            model = artist.artist.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .size(ListThumbnailSize)
                .clip(CircleShape)
        )
    },
    trailingContent = trailingContent,
    modifier = modifier
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
inline fun AlbumListItem(
    album: Album,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    playWhenReady: Boolean = false,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = album.album.title,
    subtitle = joinByBullet(
        album.artists.joinToString(),
        pluralStringResource(R.plurals.song_count, album.album.songCount, album.album.songCount),
        album.album.year?.toString()
    ),
    thumbnailContent = {
        AsyncImage(
            model = album.album.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .size(ListThumbnailSize)
                .clip(RoundedCornerShape(ThumbnailCornerRadius))
        )

        PlayingIndicatorBox(
            isPlaying = isPlaying,
            playWhenReady = playWhenReady,
            modifier = Modifier
                .size(ListThumbnailSize)
                .background(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(ThumbnailCornerRadius)
                )
        )
    },
    trailingContent = trailingContent,
    modifier = modifier
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
inline fun PlaylistListItem(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = playlist.playlist.name,
    subtitle = pluralStringResource(R.plurals.song_count, playlist.songCount, playlist.songCount),
    thumbnailContent = {
        if (playlist.thumbnails.isEmpty()) {
            Icon(
                painter = painterResource(R.drawable.ic_queue_music),
                contentDescription = null,
                modifier = Modifier.size(ListThumbnailSize)
            )
        } else if (playlist.thumbnails.size == 1) {
            AsyncImage(
                model = playlist.thumbnails[0],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(ListThumbnailSize)
                    .clip(RoundedCornerShape(ThumbnailCornerRadius))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(ListThumbnailSize)
                    .clip(RoundedCornerShape(ThumbnailCornerRadius))
            ) {
                listOf(
                    Alignment.TopStart,
                    Alignment.TopEnd,
                    Alignment.BottomStart,
                    Alignment.BottomEnd
                ).fastForEachIndexed { index, alignment ->
                    AsyncImage(
                        model = playlist.thumbnails.getOrNull(index),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .align(alignment)
                            .size(ListThumbnailSize / 2)
                    )
                }
            }
        }
    },
    trailingContent = trailingContent,
    modifier = modifier
)

@Composable
inline fun MediaMetadataListItem(
    mediaMetadata: MediaMetadata,
    modifier: Modifier,
    isPlaying: Boolean = false,
    playWhenReady: Boolean = false,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = mediaMetadata.title,
    subtitle = joinByBullet(
        mediaMetadata.artists.joinToString { it.name },
        makeTimeString(mediaMetadata.duration * 1000L)
    ),
    thumbnailContent = {
        AsyncImage(
            model = mediaMetadata.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .size(ListThumbnailSize)
                .clip(RoundedCornerShape(ThumbnailCornerRadius))
        )

        PlayingIndicatorBox(
            isPlaying = isPlaying,
            playWhenReady = playWhenReady,
            modifier = Modifier
                .size(ListThumbnailSize)
                .background(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(ThumbnailCornerRadius)
                )
        )
    },
    trailingContent = trailingContent,
    modifier = modifier
)

@Composable
inline fun YouTubeListItem(
    item: YTItem,
    modifier: Modifier = Modifier,
    albumIndex: Int? = null,
    crossinline badges: @Composable RowScope.() -> Unit = {},
    isPlaying: Boolean = false,
    playWhenReady: Boolean = false,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = item.title,
    subtitle = when (item) {
        is SongItem -> joinByBullet(item.artists.joinToString { it.name }, makeTimeString(item.duration?.times(1000L)))
        is AlbumItem -> joinByBullet(item.artists?.joinToString { it.name }, item.year?.toString())
        is ArtistItem -> null
        is PlaylistItem -> joinByBullet(item.author.name, item.songCountText)
    },
    badges = badges,
    thumbnailContent = {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(ListThumbnailSize)
        ) {
            val thumbnailShape = if (item is ArtistItem) CircleShape else RoundedCornerShape(ThumbnailCornerRadius)
            if (albumIndex != null) {
                AnimatedVisibility(
                    visible = !isPlaying,
                    enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
                    exit = shrinkOut(shrinkTowards = Alignment.Center) + fadeOut()
                ) {
                    Text(
                        text = albumIndex.toString(),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                AsyncImage(
                    model = item.thumbnail,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(thumbnailShape)
                )
            }

            PlayingIndicatorBox(
                isPlaying = isPlaying,
                playWhenReady = playWhenReady,
                color = if (albumIndex != null) MaterialTheme.colorScheme.onBackground else Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = if (albumIndex != null) Color.Transparent else Color.Black.copy(alpha = 0.4f),
                        shape = thumbnailShape
                    )
            )
        }
    },
    trailingContent = trailingContent,
    modifier = modifier
)

@Composable
inline fun YouTubeGridItem(
    item: YTItem,
    modifier: Modifier = Modifier,
    crossinline badges: @Composable RowScope.() -> Unit = {},
    isPlaying: Boolean = false,
    playWhenReady: Boolean = false,
    fillMaxWidth: Boolean = false,
) {
    val thumbnailShape = if (item is ArtistItem) CircleShape else RoundedCornerShape(ThumbnailCornerRadius)
    val thumbnailRatio = if (item is SongItem) 16f / 9 else 1f

    Column(
        modifier = if (fillMaxWidth) {
            modifier
                .padding(12.dp)
                .fillMaxWidth()
        } else {
            modifier
                .padding(12.dp)
                .width(GridThumbnailHeight * thumbnailRatio)
        }
    ) {
        Box(
            modifier = if (fillMaxWidth) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.height(GridThumbnailHeight)
            }
                .aspectRatio(thumbnailRatio)
                .clip(thumbnailShape)
        ) {
            AsyncImage(
                model = item.thumbnail,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = isPlaying,
                enter = fadeIn(tween(500)),
                exit = fadeOut(tween(500))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = thumbnailShape
                        )
                ) {
                    if (playWhenReady) {
                        PlayingIndicator(
                            color = Color.White,
                            modifier = Modifier.height(24.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_play),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (item is ArtistItem) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            badges()

            val subtitle = when (item) {
                is SongItem -> joinByBullet(item.artists.joinToString { it.name }, makeTimeString(item.duration?.times(1000L)))
                is AlbumItem -> joinByBullet(item.artists?.joinToString { it.name }, item.year?.toString())
                is ArtistItem -> null
                is PlaylistItem -> joinByBullet(item.author.name, item.songCountText)
            }

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
