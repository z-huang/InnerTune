package com.zionhuang.music.compose.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zionhuang.innertube.models.ArtistItem
import com.zionhuang.innertube.models.YTItem
import com.zionhuang.music.R
import com.zionhuang.music.constants.ListItemHeight
import com.zionhuang.music.constants.ListThumbnailSize
import com.zionhuang.music.constants.ThumbnailCornerRadius
import com.zionhuang.music.db.entities.Album
import com.zionhuang.music.db.entities.Artist
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.utils.joinByBullet
import com.zionhuang.music.utils.makeTimeString

@Composable
fun ListItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    thumbnailUrl: String? = null,
    thumbnailShape: Shape = CircleShape,
    @DrawableRes thumbnailPlaceHolder: Int? = null,
    playingIndicator: Boolean = false,
    playWhenReady: Boolean = false,
) = ListItem(
    title = title,
    subtitle = subtitle,
    thumbnailContent = {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = null,
            placeholder = thumbnailPlaceHolder?.let { painterResource(it) },
            error = thumbnailPlaceHolder?.let { painterResource(it) },
            modifier = Modifier
                .size(ListThumbnailSize.dp)
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
                    .size(ListThumbnailSize.dp)
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
)

@Composable
fun ListItem(
    title: String,
    subtitle: String,
    thumbnailContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(ListItemHeight.dp)
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

            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = null
            )
        }
    }
}

@Composable
fun GridItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    thumbnailUrl: String? = null,
    thumbnailShape: Shape = CircleShape,
) = GridItem(
    title = title,
    subtitle = subtitle,
    thumbnailContent = {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .clip(thumbnailShape)
        )
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
        modifier = modifier.padding(12.dp)
    ) {
        thumbnailContent()

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
fun SongListItem(
    song: Song,
    modifier: Modifier = Modifier,
    playingIndicator: Boolean = false,
    playWhenReady: Boolean = false,
) = ListItem(
    title = song.song.title,
    subtitle = listOf(
        song.artists.joinToString(),
        song.album?.title,
        makeTimeString(song.song.duration * 1000L)
    ).joinByBullet(),
    thumbnailUrl = song.song.thumbnailUrl,
    thumbnailShape = RoundedCornerShape(ThumbnailCornerRadius.dp),
    thumbnailPlaceHolder = R.drawable.ic_music_note,
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
    playingIndicator: Boolean = false,
    playWhenReady: Boolean = false,
) = ListItem(
    title = playlist.playlist.name,
    subtitle = pluralStringResource(R.plurals.song_count, playlist.songCount, playlist.songCount),
    thumbnailUrl = playlist.playlist.thumbnailUrl,
    thumbnailShape = RoundedCornerShape(ThumbnailCornerRadius.dp),
    thumbnailPlaceHolder = R.drawable.ic_queue_music,
    playingIndicator = playingIndicator,
    playWhenReady = playWhenReady,
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
    thumbnailShape = RoundedCornerShape(ThumbnailCornerRadius.dp),
    modifier = Modifier
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
    thumbnailShape = RoundedCornerShape(ThumbnailCornerRadius.dp),
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
    thumbnailShape = RoundedCornerShape(ThumbnailCornerRadius.dp),
    playingIndicator = playingIndicator,
    playWhenReady = playWhenReady,
    modifier = modifier
)

@Composable
fun YouTubeListItem(
    item: YTItem,
    modifier: Modifier = Modifier,
    playingIndicator: Boolean = false,
    playWhenReady: Boolean = false,
) = ListItem(
    title = item.title,
    subtitle = item.subtitle.orEmpty(),
    thumbnailUrl = item.thumbnails.lastOrNull()?.url,
    thumbnailShape = if (item is ArtistItem) CircleShape else RoundedCornerShape(ThumbnailCornerRadius.dp),
    playingIndicator = playingIndicator,
    playWhenReady = playWhenReady,
    modifier = modifier
)

@Composable
fun YouTubeGridItem(
    item: YTItem,
    modifier: Modifier = Modifier,
) = GridItem(
    title = item.title,
    subtitle = item.subtitle.orEmpty(),
    thumbnailUrl = item.thumbnails.lastOrNull()?.url,
    thumbnailShape = if (item is ArtistItem) CircleShape else RoundedCornerShape(ThumbnailCornerRadius.dp),
    modifier = modifier
)