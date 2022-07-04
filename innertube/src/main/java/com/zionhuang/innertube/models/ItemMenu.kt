package com.zionhuang.innertube.models

data class ItemMenu(
    val playEndpoint: NavigationEndpoint?,
    val shuffleEndpoint: NavigationEndpoint?,
    val radioEndpoint: NavigationEndpoint?,
    val artistEndpoint: NavigationEndpoint?,
    val albumEndpoint: NavigationEndpoint?,
    val shareEndpoint: NavigationEndpoint?,
)