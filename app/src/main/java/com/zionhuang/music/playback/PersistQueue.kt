package com.zionhuang.music.playback

import com.zionhuang.music.models.MediaMetadata
import java.io.Serializable

data class PersistQueue(
    val title: String?,
    val items: List<MediaMetadata>,
    val mediaItemIndex: Int,
    val position: Long,
) : Serializable
