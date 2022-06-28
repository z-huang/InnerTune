package com.zionhuang.music.ui.viewholders

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.innertube.models.Item
import com.zionhuang.music.databinding.ItemYoutubeListBinding
import com.zionhuang.music.databinding.ItemYoutubeNavigationBinding
import com.zionhuang.music.databinding.ItemYoutubeSquareBinding

sealed class YouTubeItemViewHolder(open val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(item: Item)
}

class YouTubeListItemViewHolder(override val binding: ItemYoutubeListBinding) : YouTubeItemViewHolder(binding) {
    override fun bind(item: Item) {
        binding.item = item
    }
}

class YouTubeSquareItemViewHolder(override val binding: ItemYoutubeSquareBinding) : YouTubeItemViewHolder(binding) {
    override fun bind(item: Item) {
        binding.item = item
    }
}

class YouTubeNavigationItemViewHolder(override val binding: ItemYoutubeNavigationBinding) : YouTubeItemViewHolder(binding) {
    override fun bind(item: Item) {
        binding.title.text = item.title
    }
}