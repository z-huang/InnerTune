package com.zionhuang.music.extensions

import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.zionhuang.music.playback.CustomMetadata

val MediaItem.metadata: CustomMetadata?
    get() = playbackProperties?.tag as? CustomMetadata

val Player.currentMetadata: CustomMetadata?
    get() = currentMediaItem?.metadata
