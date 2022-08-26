package com.zionhuang.music.ui.adapters.selection

import androidx.recyclerview.selection.ItemKeyProvider
import com.zionhuang.music.ui.adapters.LocalItemPagingAdapter

class LocalItemKeyProvider(
    private val adapter: LocalItemPagingAdapter,
) : ItemKeyProvider<String>(SCOPE_CACHED) {
    override fun getKey(position: Int): String? = adapter.getItemAt(position)?.id
    override fun getPosition(key: String): Int = adapter.snapshot().items.indexOfFirst { it.id == key }
}