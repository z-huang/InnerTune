package com.zionhuang.music.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.databinding.ItemSuggestionBinding

class SuggestionViewHolder(private val binding: ItemSuggestionBinding, val fillQuery: (query: String) -> Unit) : RecyclerView.ViewHolder(binding.root) {
    fun bind(query: String) {
        binding.query = query
        binding.executePendingBindings()
        binding.fillTextButton.setOnClickListener { fillQuery(binding.query!!) }
    }
}