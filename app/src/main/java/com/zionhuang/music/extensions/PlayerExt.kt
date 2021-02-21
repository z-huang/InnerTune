package com.zionhuang.music.extensions

import com.google.android.exoplayer2.Player
import com.zionhuang.music.playback.CustomMetadata

val Player.currentMetadata: CustomMetadata?
    get() = currentMediaItem?.playbackProperties?.tag as? CustomMetadata