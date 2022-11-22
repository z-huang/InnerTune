package com.zionhuang.music.compose.component

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun LinearProgressIndicator(
    indeterminate: Boolean,
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.linearColor,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
) = if (indeterminate) {
    LinearProgressIndicator(modifier, color, trackColor)
} else {
    LinearProgressIndicator(progress, modifier, color, trackColor)
}