package com.zionhuang.music.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.google.android.exoplayer2.Player

val Player.shouldBePlaying: Boolean
    get() = !(playbackState == Player.STATE_ENDED || !playWhenReady)

@Composable
inline fun Player.DisposableListener(crossinline listenerProvider: () -> Player.Listener) {
    DisposableEffect(this) {
        val listener = listenerProvider()
        addListener(listener)
        onDispose { removeListener(listener) }
    }
}