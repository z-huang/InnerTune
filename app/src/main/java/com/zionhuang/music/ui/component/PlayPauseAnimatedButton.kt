package com.zionhuang.music.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player.STATE_ENDED
import com.zionhuang.music.R

@Composable
fun PlayPauseAnimatedButton(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    playbackState: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val radius = if (isPlaying || isPressed.value) {
        25.dp
    } else {
        15.dp
    }
    val cornerRadius = animateDpAsState(targetValue = radius, label = "Animated button shape")

    Surface(
        tonalElevation = 10.dp,
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.value))
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.onSecondary)
                .size(72.dp)
                .clip(RoundedCornerShape(cornerRadius.value))
                .clickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple()
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = Pair(playbackState, isPlaying), label = "") { (currentState, isPlaying) ->
                val imageVector = when {
                    currentState == STATE_ENDED -> ImageVector.vectorResource(R.drawable.replay)
                    isPlaying -> ImageVector.vectorResource(R.drawable.pause)
                    else -> Icons.Default.PlayArrow
                }

                Icon(
                    imageVector = imageVector,
                    contentDescription = "Actual player state button",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}