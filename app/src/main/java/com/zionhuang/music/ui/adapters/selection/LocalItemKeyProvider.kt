package com.zionhuang.music.ui.adapters.selection

import androidx.recyclerview.selection.ItemKeyProvider
import com.zionhuang.music.db.entities.LocalItem
import com.zionhuang.music.ui.adapters.DraggableLocalItemAdapter
import com.zionhuang.music.ui.adapters.LocalItemAdapter

class LocalItemKeyProvider(private val adapter: LocalItemAdapter) : ItemKeyProvider<String>(SCOPE_MAPPED) {
    override fun getKey(position: Int): String? = adapter.currentList.getOrNull(position)?.takeIf { it is LocalItem }?.id
    override fun getPosition(key: String): Int = adapter.currentList.indexOfFirst { it.id == key }
}

class DraggableLocalItemKeyProvider(private val adapter: DraggableLocalItemAdapter) : ItemKeyProvider<String>(SCOPE_MAPPED) {
    override fun getKey(position: Int): String? = adapter.currentList.getOrNull(position)?.takeIf { it is LocalItem }?.id
    override fun getPosition(key: String): Int = adapter.currentList.indexOfFirst { it.id == key }
}