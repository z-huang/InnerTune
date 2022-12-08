package com.zionhuang.music.compose.utils

import androidx.compose.foundation.layout.PaddingValues

operator fun PaddingValues.plus(other: PaddingValues) = PaddingValues(
    start = calculateTopPadding() + other.calculateTopPadding(),
    end = calculateBottomPadding() + other.calculateBottomPadding(),
    top = calculateTopPadding() + other.calculateTopPadding(),
    bottom = calculateBottomPadding() + other.calculateBottomPadding()
)
