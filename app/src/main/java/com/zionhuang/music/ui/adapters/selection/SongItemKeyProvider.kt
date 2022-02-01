package com.zionhuang.music.ui.adapters.selection

import androidx.recyclerview.selection.ItemKeyProvider
import com.zionhuang.music.ui.adapters.SongsAdapter

class SongItemKeyProvider(
    private val adapter: SongsAdapter
) : ItemKeyProvider<String>(SCOPE_CACHED) {
    override fun getKey(position: Int): String? =
        if (position == 0) null else adapter.snapshot()[position]?.id

    override fun getPosition(key: String): Int =
        adapter.snapshot().items.indexOfFirst { it.id == key }

}