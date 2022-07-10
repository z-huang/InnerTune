package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.zionhuang.innertube.models.*
import com.zionhuang.music.R
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.viewholders.*
import com.zionhuang.music.utils.NavigationEndpointHandler

class YouTubeBaseItemAdapter(
    private val itemViewType: Section.ViewType,
    private val forceMatchParent: Boolean,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : ListAdapter<BaseItem, YouTubeItemViewHolder>(ItemComparator()) {
    var onFillQuery: (String) -> Unit = {}
    var onSearch: (String) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YouTubeItemViewHolder = when (viewType) {
        ITEM_SEPARATOR -> YouTubeSeparatorViewHolder(parent.inflateWithBinding(R.layout.item_separator))
        ITEM_NAVIGATION -> when (itemViewType) {
            Section.ViewType.LIST -> YouTubeNavigationItemViewHolder(parent.inflateWithBinding(R.layout.item_youtube_navigation), navigationEndpointHandler)
            Section.ViewType.BLOCK -> YouTubeNavigationButtonViewHolder(parent.inflateWithBinding(R.layout.item_youtube_navigation_btn), navigationEndpointHandler)
        }
        ITEM_SUGGESTION -> YouTubeSuggestionViewHolder(parent.inflateWithBinding(R.layout.item_youtube_suggestion), onFillQuery, onSearch)
        ITEM_OTHER -> when (itemViewType) {
            Section.ViewType.LIST -> YouTubeListItemViewHolder(parent.inflateWithBinding(R.layout.item_youtube_list), navigationEndpointHandler)
            Section.ViewType.BLOCK -> YouTubeSquareItemViewHolder(parent.inflateWithBinding(R.layout.item_youtube_square), navigationEndpointHandler)
        }
        else -> throw IllegalArgumentException("Unknown view type")
    }.apply {
        if (forceMatchParent) {
            binding.root.updateLayoutParams<GridLayoutManager.LayoutParams> {
                width = MATCH_PARENT
            }
        }
    }

    override fun onBindViewHolder(holder: YouTubeItemViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is YouTubeSeparatorViewHolder -> {}
            is YouTubeNavigationItemViewHolder -> holder.bind(item as NavigationItem)
            is YouTubeNavigationButtonViewHolder -> holder.bind(item as NavigationItem)
            is YouTubeSuggestionViewHolder -> holder.bind(item as SuggestionTextItem)
            is YouTubeListItemViewHolder -> holder.bind(item as Item)
            is YouTubeSquareItemViewHolder -> holder.bind(item as Item)
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        Separator -> ITEM_SEPARATOR
        is NavigationItem -> ITEM_NAVIGATION
        is SuggestionTextItem -> ITEM_SUGGESTION
        else -> ITEM_OTHER
    }


    class ItemComparator : DiffUtil.ItemCallback<BaseItem>() {
        override fun areItemsTheSame(oldItem: BaseItem, newItem: BaseItem): Boolean = oldItem::class == newItem::class && oldItem.title == newItem.title
        override fun areContentsTheSame(oldItem: BaseItem, newItem: BaseItem): Boolean = oldItem == newItem
    }

    companion object {
        const val ITEM_SEPARATOR = 0
        const val ITEM_NAVIGATION = 1
        const val ITEM_SUGGESTION = 2
        const val ITEM_OTHER = 3
    }
}