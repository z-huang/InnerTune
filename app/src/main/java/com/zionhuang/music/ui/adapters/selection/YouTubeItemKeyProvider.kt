package com.zionhuang.music.ui.adapters.selection

import androidx.recyclerview.selection.ItemKeyProvider
import com.zionhuang.innertube.models.ArtistItem
import com.zionhuang.innertube.models.YTItem
import com.zionhuang.music.ui.adapters.YouTubeItemPagingAdapter

class YouTubeItemKeyProvider(private val adapter: YouTubeItemPagingAdapter) : ItemKeyProvider<String>(SCOPE_CACHED) {
    override fun getKey(position: Int): String? = adapter.snapshot().getOrNull(position)?.takeIf { it is YTItem && it !is ArtistItem }?.id
    override fun getPosition(key: String): Int = adapter.snapshot().indexOfFirst { it?.id == key }
}