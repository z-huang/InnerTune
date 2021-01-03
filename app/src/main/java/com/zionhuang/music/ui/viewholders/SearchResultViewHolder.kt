package com.zionhuang.music.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.google.api.services.youtube.model.SearchResult
import com.zionhuang.music.databinding.ItemSearchResultBinding

class SearchResultViewHolder(private val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: SearchResult) {
        binding.item = item
    }
}