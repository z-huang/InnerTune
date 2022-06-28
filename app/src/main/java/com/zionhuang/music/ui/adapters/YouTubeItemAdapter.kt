package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.zionhuang.innertube.models.Item
import com.zionhuang.innertube.models.NavigationItem
import com.zionhuang.music.R
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.viewholders.YouTubeItemViewHolder
import com.zionhuang.music.ui.viewholders.YouTubeListItemViewHolder
import com.zionhuang.music.ui.viewholders.YouTubeSquareItemViewHolder

class YouTubeItemAdapter(private val itemStyle: ItemStyle) : ListAdapter<Item, YouTubeItemViewHolder>(ItemComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YouTubeItemViewHolder = when (itemStyle) {
        ItemStyle.LIST -> YouTubeListItemViewHolder(parent.inflateWithBinding(R.layout.item_youtube_list))
        ItemStyle.SQUARE -> YouTubeSquareItemViewHolder(parent.inflateWithBinding(R.layout.item_youtube_square))
    }

    override fun onBindViewHolder(holder: YouTubeItemViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is NavigationItem -> ITEM_NAVIGATION
        else -> ITEM_OTHER
    }

    class ItemComparator : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem === newItem
        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem == newItem
    }

    enum class ItemStyle {
        LIST,
        SQUARE
    }

    companion object {
        const val ITEM_NAVIGATION = 0
        const val ITEM_OTHER = 1
    }
}