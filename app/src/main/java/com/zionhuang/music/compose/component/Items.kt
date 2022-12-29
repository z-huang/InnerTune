package com.zionhuang.music.compose.component

import androidx.annotation.DrawableRes
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
import androidx.compose.material3.IconButton
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
import com.zionhuang.innertube.models.ArtistItem
import com.zionhuang.innertube.models.YTItem
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
fun ListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String = "",
    badges: (@Composable RowScope.() -> Unit)? = null,
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

            if (badges != null || subtitle.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (badges != null) {
                        badges()
                    }

                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        trailingContent()
    }
}

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String = "",
    badges: (@Composable RowScope.() -> Unit)? = null,
    thumbnailContent: (@Composable () -> Unit)? = null,
    @DrawableRes thumbnailDrawable: Int? = null,
    thumbnailUrl: String? = null,
    thumbnailShape: Shape = CircleShape,
    @DrawableRes thumbnailPlaceHolder: Int? = null,
    showMenuButton: Boolean = true,
    onShowMenu: () -> Unit = {},
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
    playingIndicator: Boolean = false,
    playWhenReady: Boolean = false,
) = ListItem(
    title = title,
    subtitle = subtitle,
    badges = badges,
    thumbnailContent = {
        if (thumbnailContent != null) {
            thumbnailContent()
        } else {
            if (thumbnailDrawable != null) {
                Image(
                    painter = painterResource(thumbnailDrawable),
                    contentDescription = null,
                    modifier = Modifier.size(ListThumbnailSize)
                )
            } else {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = null,
                    placeholder = thumbnailPlaceHolder?.let { painterResource(it) },
                    error = thumbnailPlaceHolder?.let { painterResource(it) },
                    modifier = Modifier
                        .size(ListThumbnailSize)
                        .clip(thumbnailShape)
                )
            }
            AnimatedVisibility(
                visible = playingIndicator,
                enter = fadeIn(tween(500)),
                exit = fadeOut(tween(500))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(ListThumbnailSize)
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
    },
    trailingContent = {
        if (showMenuButton) {
            IconButton(onClick = onShowMenu) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert),
                    contentDescription = null
                )
            }
        }

        if (trailingContent != null) {
            trailingContent()
        }
    },
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
fun SongListItem(
    song: Song,
    modifier: Modifier = Modifier,
    showBadges: Boolean = true,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
    showMenuButton: Boolean = true,
    onShowMenu: () -> Unit = {},
    playingIndicator: Boolean = false,
    playWhenReady: Boolean = false,
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
    thumbnailUrl = song.song.thumbnailUrl,
    thumbnailShape = RoundedCornerShape(ThumbnailCornerRadius),
    thumbnailPlaceHolder = R.drawable.ic_music_note,
    trailingContent = trailingContent,
    showMenuButton = showMenuButton,
    onShowMenu = onShowMenu,
    playingIndicator = playingIndicator,
    playWhenReady = playWhenReady,
    modifier = modifier
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ArtistListItem(
    artist: Artist,
    modifier: Modifier = Modifier,
    playingIndicator: Boolean = false,
    playWhenReady: Boolean = false,
) = ListItem(
    title = artist.artist.name,
    subtitle = pluralStringResource(R.plurals.song_count, artist.songCount, artist.songCount),
    thumbnailUrl = artist.artist.thumbnailUrl,
    thumbnailShape = CircleShape,
    thumbnailPlaceHolder = R.drawable.ic_artist,
    playingIndicator = playingIndicator,
    playWhenReady = playWhenReady,
    modifier = modifier
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AlbumListItem(
    album: Album,
    modifier: Modifier = Modifier,
    playingIndicator: Boolean = false,
    playWhenReady: Boolean = false,
) = ListItem(
    title = album.album.title,
    subtitle = listOf(
        album.artists.joinToString(),
        pluralStringResource(R.plurals.song_count, album.album.songCount, album.album.songCount),
        album.album.year?.toString()
    ).joinByBullet(),
    thumbnailUrl = album.album.thumbnailUrl,
    thumbnailShape = RoundedCornerShape(6.dp),
    thumbnailPlaceHolder = R.drawable.ic_album,
    playingIndicator = playingIndicator,
    playWhenReady = playWhenReady,
    modifier = modifier
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlaylistListItem(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    showMenuButton: Boolean = true,
    onShowMenu: () -> Unit = {},
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
    showMenuButton = showMenuButton,
    onShowMenu = onShowMenu,
    modifier = modifier
)

@Composable
fun SongGridItem(
    song: Song,
    modifier: Modifier = Modifier,
) = GridItem(
    title = song.song.title,
    subtitle = song.artists.joinToString { it.name },
    thumbnailUrl = song.song.thumbnailUrl,
    thumbnailShape = RoundedCornerShape(ThumbnailCornerRadius),
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
fun MediaMetadataListItem(
    mediaMetadata: MediaMetadata,
    modifier: Modifier,
    playingIndicator: Boolean = false,
    playWhenReady: Boolean = false,
) = ListItem(
    title = mediaMetadata.title,
    subtitle = mediaMetadata.artists.joinToString { it.name },
    thumbnailUrl = mediaMetadata.thumbnailUrl,
    thumbnailShape = RoundedCornerShape(ThumbnailCornerRadius),
    playingIndicator = playingIndicator,
    playWhenReady = playWhenReady,
    modifier = modifier
)

@Composable
fun YouTubeListItem(
    item: YTItem,
    onShowMenu: () -> Unit = {},
    modifier: Modifier = Modifier,
    playingIndicator: Boolean = false,
    playWhenReady: Boolean = false,
) = ListItem(
    title = item.title,
    subtitle = item.subtitle.orEmpty(),
    thumbnailUrl = item.thumbnails.lastOrNull()?.url,
    thumbnailShape = if (item is ArtistItem) CircleShape else RoundedCornerShape(ThumbnailCornerRadius),
    onShowMenu = onShowMenu,
    playingIndicator = playingIndicator,
    playWhenReady = playWhenReady,
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