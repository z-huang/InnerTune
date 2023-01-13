package com.zionhuang.music.ui.component.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.zionhuang.music.constants.GridThumbnailHeight
import com.zionhuang.music.constants.ThumbnailCornerRadius

@Composable
fun GridItemPlaceHolder(
    modifier: Modifier = Modifier,
    thumbnailShape: Shape = RoundedCornerShape(ThumbnailCornerRadius),
) {
    Column(
        modifier = modifier.padding(12.dp)
    ) {
        Spacer(
            modifier = Modifier
                .size(GridThumbnailHeight)
                .clip(thumbnailShape)
                .background(MaterialTheme.colorScheme.onSurface)
        )

        Spacer(modifier = Modifier.height(6.dp))

        TextPlaceholder()

        TextPlaceholder()
    }
}
