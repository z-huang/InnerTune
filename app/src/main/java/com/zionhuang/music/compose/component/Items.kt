package com.zionhuang.music.compose.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
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

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (subtitle != null) {
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
    subtitle: String,
    crossinline badges: @Composable RowScope.() -> Unit = {},
    thumbnailContent: @Composable () -> Unit,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
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
    trailingContent = trailingContent,
    modifier = modifier
)

@Composable
fun GridItem(
    title: String,
    subtitle: String,
    thumbnailContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
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

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun GridItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    thumbnailUrl: String? = null,
    thumbnailRatio: Float = 1f,
    thumbnailShape: Shape = CircleShape,
    playingIndicator: Boolean = false,
    playWhenReady: Boolean = false,
) = GridItem(
    title = title,
    subtitle = subtitle,
    thumbnailContent = {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .width(GridThumbnailSize * thumbnailRatio)
                .height(GridThumbnailSize)
                .clip(thumbnailShape)
        )
        AnimatedVisibility(
            visible = playingIndicator,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(500))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(GridThumbnailSize * thumbnailRatio)
                    .height(GridThumbnailSize)
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
    },
    modifier = modifier
        .padding(12.dp)
        .width(GridThumbnailSize * thumbnailRatio)
)

@Composable
inline fun SongListItem(
    song: Song,
    modifier: Modifier = Modifier,
    showBadges: Boolean = true,
    isPlaying: Boolean = false,
    playWhenReady: Boolean = false,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = song.song.title,
    subtitle = listOf(
        song.artists.joinToString(),
        song.album?.title,
        makeTimeString(song.song.duration * 1000L)
    ).joinByBullet(),
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
        AsyncImage(
            model = song.song.thumbnailUrl,
            contentDescription = null,
            placeholder = painterResource(R.drawable.ic_music_note),
            error = painterResource(R.drawable.ic_music_note),
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
            placeholder = painterResource(R.drawable.ic_artist),
            error = painterResource(R.drawable.ic_artist),
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
    subtitle = listOf(
        album.artists.joinToString(),
        pluralStringResource(R.plurals.song_count, album.album.songCount, album.album.songCount),
        album.album.year?.toString()
    ).joinByBullet(),
    thumbnailContent = {
        AsyncImage(
            model = album.album.thumbnailUrl,
            contentDescription = null,
            placeholder = painterResource(R.drawable.ic_album),
            error = painterResource(R.drawable.ic_album),
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
            Image(
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
    subtitle = listOf(
        mediaMetadata.artists.joinToString { it.name },
        makeTimeString(mediaMetadata.duration * 1000L)
    ).joinByBullet(),
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
    crossinline badges: @Composable RowScope.() -> Unit = {},
    isPlaying: Boolean = false,
    playWhenReady: Boolean = false,
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = item.title,
    subtitle = item.subtitle.orEmpty(),
    badges = badges,
    thumbnailContent = {
        val thumbnailShape = if (item is ArtistItem) CircleShape else RoundedCornerShape(ThumbnailCornerRadius)
        AsyncImage(
            model = item.thumbnails.lastOrNull()?.url,
            contentDescription = null,
            modifier = Modifier
                .size(ListThumbnailSize)
                .clip(thumbnailShape)
        )

        PlayingIndicatorBox(
            isPlaying = isPlaying,
            playWhenReady = playWhenReady,
            modifier = Modifier
                .size(ListThumbnailSize)
                .background(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = thumbnailShape
                )
        )
    },
    trailingContent = trailingContent,
    modifier = modifier
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ArtistGridItem(
    artist: Artist,
    modifier: Modifier = Modifier,
) = GridItem(
    title = artist.artist.name,
    subtitle = pluralStringResource(R.plurals.song_count, artist.songCount, artist.songCount),
    thumbnailUrl = artist.artist.thumbnailUrl,
    thumbnailShape = CircleShape,
    modifier = modifier
)


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AlbumGridItem(
    album: Album,
    modifier: Modifier = Modifier,
) = GridItem(
    title = album.album.title,
    subtitle = listOf(
        album.artists.joinToString(),
        pluralStringResource(R.plurals.song_count, album.album.songCount, album.album.songCount),
        album.album.year?.toString()
    ).joinByBullet(),
    thumbnailUrl = album.album.thumbnailUrl,
    thumbnailShape = RoundedCornerShape(6.dp),
    modifier = modifier
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlaylistGridItem(
    playlist: Playlist,
    modifier: Modifier = Modifier,
) = GridItem(
    title = playlist.playlist.name,
    subtitle = pluralStringResource(R.plurals.song_count, playlist.songCount, playlist.songCount),
    thumbnailUrl = playlist.playlist.thumbnailUrl,
    thumbnailShape = RoundedCornerShape(ThumbnailCornerRadius),
    modifier = modifier
)

@Composable
fun YouTubeGridItem(
    item: YTItem,
    modifier: Modifier = Modifier,
    playingIndicator: Boolean = false,
    playWhenReady: Boolean = false,
) = GridItem(
    title = item.title,
    subtitle = item.subtitle.orEmpty(),
    thumbnailUrl = item.thumbnails.lastOrNull()?.url,
    thumbnailRatio = item.thumbnails.lastOrNull()?.let {
        if (it.width != null && it.height != null) it.width!!.toFloat() / it.height!!
        else 1f
    } ?: 1f,
    thumbnailShape = if (item is ArtistItem) CircleShape else RoundedCornerShape(ThumbnailCornerRadius),
    playingIndicator = playingIndicator,
    playWhenReady = playWhenReady,
    modifier = modifier
)