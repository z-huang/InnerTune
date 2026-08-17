package com.zionhuang.music.utils.potoken

/**
 * @param playerRequestPoToken the session-bound token sent in the player() request body
 * (`serviceIntegrityDimensions.poToken`)
 * @param streamingDataPoToken the video-bound token appended to the resolved stream URL (`pot=`)
 */
data class PoTokenResult(
    val playerRequestPoToken: String,
    val streamingDataPoToken: String,
)
