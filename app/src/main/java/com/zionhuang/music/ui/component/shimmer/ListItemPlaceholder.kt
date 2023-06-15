package com.zionhuang.music.ui.component.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.zionhuang.music.constants.ListItemHeight
import com.zionhuang.music.constants.ListThumbnailSize
import com.zionhuang.music.constants.ThumbnailCornerRadius

@Composable
fun ListItemPlaceHolder(
    modifier: Modifier = Modifier,
    thumbnailShape: Shape = RoundedCornerShape(ThumbnailCornerRadius),
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(ListItemHeight)
            .padding(horizontal = 6.dp),
    ) {
        Spacer(
            modifier = Modifier
                .padding(6.dp)
                .size(ListThumbnailSize)
                .clip(thumbnailShape)
                .background(MaterialTheme.colorScheme.onSurface)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        ) {
            TextPlaceholder()
            TextPlaceholder()
        }
    }
}