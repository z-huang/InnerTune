package com.zionhuang.music.ui.viewholders

import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.innertube.models.Item
import com.zionhuang.innertube.models.NavigationItem
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemYoutubeListBinding
import com.zionhuang.music.databinding.ItemYoutubeNavigationBinding
import com.zionhuang.music.databinding.ItemYoutubeNavigationBtnBinding
import com.zionhuang.music.databinding.ItemYoutubeSquareBinding

sealed class YouTubeItemViewHolder(open val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

class YouTubeListItemViewHolder(override val binding: ItemYoutubeListBinding) : YouTubeItemViewHolder(binding) {
    fun bind(item: Item) {
        binding.item = item
    }
}

class YouTubeSquareItemViewHolder(override val binding: ItemYoutubeSquareBinding) : YouTubeItemViewHolder(binding) {
    fun bind(item: Item) {
        binding.item = item
    }
}

class YouTubeNavigationItemViewHolder(override val binding: ItemYoutubeNavigationBinding) : YouTubeItemViewHolder(binding) {
    fun bind(item: NavigationItem) {
        binding.title.text = item.title
        val iconRes = when (item.icon) {
            "MUSIC_NEW_RELEASE" -> R.drawable.ic_new_releases
            "TRENDING_UP" -> R.drawable.ic_trending_up
            "STICKER_EMOTICON" -> R.drawable.ic_sentiment_satisfied
            else -> null
        }
        binding.icon.isVisible = iconRes != null
        iconRes?.let {
            binding.icon.setImageResource(iconRes)
        }
    }
}

class YouTubeNavigationButtonViewHolder(override val binding: ItemYoutubeNavigationBtnBinding) : YouTubeItemViewHolder(binding) {
    fun bind(item: NavigationItem) {
        binding.title.text = item.title
        binding.stripe.isVisible = item.stripeColor != null
        item.stripeColor?.let {
            binding.card.strokeColor = it.toInt()
            binding.stripe.setBackgroundColor(it.toInt())
        }
    }
}