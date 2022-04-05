package com.zionhuang.music.update

import com.google.gson.annotations.SerializedName

data class Release(
    val id: Int,
    val url: String,
    @SerializedName("node_id")
    val nodeId: String,
    @SerializedName("tag_name")
    val tagName: String,
    val name: String,
    @SerializedName("prerelease")
    val preRelease: Boolean,
    val body: String,
    val assets: List<Asset>,
) {
    val version: Version get() = Version.parse(name)
}
