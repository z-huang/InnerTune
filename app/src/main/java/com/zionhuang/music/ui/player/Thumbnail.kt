package com.zionhuang.music.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zionhuang.music.constants.SHOW_LYRICS
import com.zionhuang.music.constants.ThumbnailCornerRadius
import com.zionhuang.music.extensions.preferenceState
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.ui.component.Lyrics

@Composable
fun Thumbnail(
    mediaMetadata: MediaMetadata?,
    sliderPositionProvider: () -> Long?,
    modifier: Modifier = Modifier,
) {
    mediaMetadata ?: return
    val showLyrics by preferenceState(SHOW_LYRICS, false)

    val currentView = LocalView.current

    DisposableEffect(showLyrics) {
        currentView.keepScreenOn = showLyrics
        onDispose {
            currentView.keepScreenOn = false
        }
    }

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = !showLyrics,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.systemBars
                        .only(WindowInsetsSides.Top)
                        .add(WindowInsets(left = 16.dp, right = 16.dp))
                        .asPaddingValues())
            ) {
                AsyncImage(
                    model = mediaMetadata.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                        .fillMaxWidth()
                        .align(Alignment.Center)
                )
            }
        }

        AnimatedVisibility(
            visible = showLyrics,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Lyrics(
                    sliderPositionProvider = sliderPositionProvider,
                    mediaMetadataProvider = { mediaMetadata }
                )
            }
        }
    }
}
