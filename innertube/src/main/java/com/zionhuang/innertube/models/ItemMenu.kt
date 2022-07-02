package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoint.BrowseEndpoint
import com.zionhuang.innertube.models.endpoint.ShareEntityEndpoint

data class ItemMenu(
    val playEndpoint: NavigationEndpoint?,
    val shuffleEndpoint: NavigationEndpoint?,
    val radioEndpoint: NavigationEndpoint?,
    val artistEndpoint: BrowseEndpoint?,
    val albumEndpoint: BrowseEndpoint?,
    val shareEndpoint: ShareEntityEndpoint?,
)