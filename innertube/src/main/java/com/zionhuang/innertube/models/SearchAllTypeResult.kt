package com.zionhuang.innertube.models

data class SearchAllTypeResult(
    val filters: List<Filter>?,
    val items: List<BaseItem>,
)
