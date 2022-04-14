package com.zionhuang.music.update

import com.google.gson.annotations.SerializedName

data class Asset(
    @SerializedName("id") val id: String,
    @SerializedName("node_id") val nodeId: String,
    @SerializedName("name") val name: String,
    @SerializedName("content_type") val contentType: String,
    @SerializedName("size") val size: Long,
    @SerializedName("browser_download_url") val downloadUrl: String,
)
