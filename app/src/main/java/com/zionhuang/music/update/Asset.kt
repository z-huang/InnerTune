package com.zionhuang.music.update

import com.google.gson.annotations.SerializedName

data class Asset(
    val id: String,
    @SerializedName("node_id")
    val nodeId: String,
    val name: String,
    @SerializedName("content_type")
    val contentType: String,
    val size: Long,
    @SerializedName("browser_download_url")
    val downloadUrl: String,
)
