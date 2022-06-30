package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.zionhuang.innertube.models.BaseItem
import com.zionhuang.innertube.models.Item
import com.zionhuang.innertube.models.NavigationItem
import com.zionhuang.innertube.models.Section
import com.zionhuang.music.R
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.viewholders.*
import com.zionhuang.music.utils.NavigationEndpointHandler

class YouTubeItemAdapter(
    private val itemViewType: Section.ViewType,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : ListAdapter<BaseItem, YouTubeItemViewHolder>(ItemComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YouTubeItemViewHolder = when (viewType) {
        ITEM_NAVIGATION -> when (itemViewType) {
            Section.ViewType.LIST -> YouTubeNavigationItemViewHolder(parent.inflateWithBinding(R.layout.item_youtube_navigation), navigationEndpointHandler)
            Section.ViewType.BLOCK -> YouTubeNavigationButtonViewHolder(parent.inflateWithBinding(R.layout.item_youtube_navigation_btn), navigationEndpointHandler)
        }
        ITEM_OTHER -> when (itemViewType) {
            Section.ViewType.LIST -> YouTubeListItemViewHolder(parent.inflateWithBinding(R.layout.item_youtube_list), navigationEndpointHandler)
            Section.ViewType.BLOCK -> YouTubeSquareItemViewHolder(parent.inflateWithBinding(R.layout.item_youtube_square), navigationEndpointHandler)
        }
        else -> throw IllegalArgumentException("Unknown view type")
    }

    override fun onBindViewHolder(holder: YouTubeItemViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is YouTubeNavigationItemViewHolder -> holder.bind(item as NavigationItem)
            is YouTubeNavigationButtonViewHolder -> holder.bind(item as NavigationItem)
            is YouTubeListItemViewHolder -> holder.bind(item as Item)
            is YouTubeSquareItemViewHolder -> holder.bind(item as Item)
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is NavigationItem -> ITEM_NAVIGATION
        else -> ITEM_OTHER
    }


    class ItemComparator : DiffUtil.ItemCallback<BaseItem>() {
        override fun areItemsTheSame(oldItem: BaseItem, newItem: BaseItem): Boolean = oldItem::class == newItem::class && oldItem.title == newItem.title
        override fun areContentsTheSame(oldItem: BaseItem, newItem: BaseItem): Boolean = oldItem == newItem
    }

    companion object {
        const val ITEM_NAVIGATION = 0
        const val ITEM_OTHER = 1
    }
}