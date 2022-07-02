package com.zionhuang.music.ui.viewholders

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.innertube.models.Item
import com.zionhuang.innertube.models.NavigationItem
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemYoutubeListBinding
import com.zionhuang.music.databinding.ItemYoutubeNavigationBinding
import com.zionhuang.music.databinding.ItemYoutubeNavigationBtnBinding
import com.zionhuang.music.databinding.ItemYoutubeSquareBinding
import com.zionhuang.music.utils.NavigationEndpointHandler

sealed class YouTubeItemViewHolder(open val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

class YouTubeListItemViewHolder(
    override val binding: ItemYoutubeListBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeItemViewHolder(binding) {
    fun bind(item: Item) {
        binding.item = item
        if (item is SongItem && item.index != null) {
            binding.index.text = item.index
        }
        binding.root.setOnClickListener {
            navigationEndpointHandler.handle(item.navigationEndpoint)
        }
        binding.executePendingBindings()
    }
}

class YouTubeSquareItemViewHolder(
    override val binding: ItemYoutubeSquareBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeItemViewHolder(binding) {
    fun bind(item: Item) {
        binding.item = item
        binding.root.setOnClickListener {
            navigationEndpointHandler.handle(item.navigationEndpoint)
        }
        binding.executePendingBindings()
    }
}

class YouTubeNavigationItemViewHolder(
    override val binding: ItemYoutubeNavigationBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeItemViewHolder(binding) {
    fun bind(item: NavigationItem) {
        binding.item = item
        when (item.icon) {
            "MUSIC_NEW_RELEASE" -> R.drawable.ic_new_releases
            "TRENDING_UP" -> R.drawable.ic_trending_up
            "STICKER_EMOTICON" -> R.drawable.ic_sentiment_satisfied
            else -> null
        }?.let {
            binding.icon.setImageResource(it)
        }
        binding.container.setOnClickListener {
            navigationEndpointHandler.handle(item.navigationEndpoint)
        }
        binding.executePendingBindings()
    }
}

class YouTubeNavigationButtonViewHolder(
    override val binding: ItemYoutubeNavigationBtnBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeItemViewHolder(binding) {
    fun bind(item: NavigationItem) {
        binding.item = item
        binding.card.setOnClickListener {
            navigationEndpointHandler.handle(item.navigationEndpoint)
        }
        binding.executePendingBindings()
    }
}