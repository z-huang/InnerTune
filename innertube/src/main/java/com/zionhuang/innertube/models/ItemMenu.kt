package com.zionhuang.innertube.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemMenu(
    val playEndpoint: NavigationEndpoint?,
    val shuffleEndpoint: NavigationEndpoint?,
    val radioEndpoint: NavigationEndpoint?,
    val playNextEndpoint: NavigationEndpoint?,
    val addToQueueEndpoint: NavigationEndpoint?,
    val artistEndpoint: NavigationEndpoint?,
    val albumEndpoint: NavigationEndpoint?,
    val shareEndpoint: NavigationEndpoint?,
) : Parcelable