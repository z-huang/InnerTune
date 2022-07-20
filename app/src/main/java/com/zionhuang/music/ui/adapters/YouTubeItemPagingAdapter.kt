package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.view.updateLayoutParams
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.zionhuang.innertube.models.*
import com.zionhuang.music.R
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.viewholders.*
import com.zionhuang.music.utils.NavigationEndpointHandler

/**
 * Same as [YouTubeItemAdapter], but extends [PagingDataAdapter]
 */
class YouTubeItemPagingAdapter(
    private val navigationEndpointHandler: NavigationEndpointHandler,
    private val itemViewType: BaseItem.ViewType = BaseItem.ViewType.LIST,
    private val forceMatchParent: Boolean = false,
) : PagingDataAdapter<BaseItem, YouTubeViewHolder>(ItemComparator()) {
    var onFillQuery: (String) -> Unit = {}
    var onSearch: (String) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YouTubeViewHolder = when (viewType) {
        BASE_ITEM_HEADER -> YouTubeHeaderViewHolder(parent.inflateWithBinding(R.layout.item_youtube_header), navigationEndpointHandler)
        BASE_ITEM_HEADER_ARTIST -> YouTubeArtistHeaderViewHolder(parent.inflateWithBinding(R.layout.item_youtube_header_artist), navigationEndpointHandler)
        BASE_ITEM_HEADER_ALBUM_OR_PLAYLIST -> YouTubeAlbumOrPlaylistHeaderViewHolder(parent.inflateWithBinding(R.layout.item_youtube_header_album), navigationEndpointHandler)
        BASE_ITEM_CAROUSEL, BASE_ITEM_GRID -> YouTubeItemContainerViewHolder(parent.inflateWithBinding(R.layout.item_recyclerview), navigationEndpointHandler)
        BASE_ITEM_DESCRIPTION -> YouTubeDescriptionViewHolder(parent.inflateWithBinding(R.layout.item_youtube_description))
        BASE_ITEM_SEPARATOR -> YouTubeSeparatorViewHolder(parent.inflateWithBinding(R.layout.item_separator))
        BASE_ITEM_NAVIGATION -> when (itemViewType) {
            BaseItem.ViewType.LIST -> YouTubeNavigationItemViewHolder(parent.inflateWithBinding(R.layout.item_youtube_navigation), navigationEndpointHandler)
            BaseItem.ViewType.BLOCK -> YouTubeNavigationButtonViewHolder(parent.inflateWithBinding(R.layout.item_youtube_navigation_btn), navigationEndpointHandler)
        }
        BASE_ITEM_SUGGESTION -> YouTubeSuggestionViewHolder(parent.inflateWithBinding(R.layout.item_youtube_suggestion), onFillQuery, onSearch)
        ITEM -> when (itemViewType) {
            BaseItem.ViewType.LIST -> YouTubeListItemViewHolder(parent.inflateWithBinding(R.layout.item_youtube_list), navigationEndpointHandler)
            BaseItem.ViewType.BLOCK -> YouTubeSquareItemViewHolder(parent.inflateWithBinding(R.layout.item_youtube_square), navigationEndpointHandler)
        }
        else -> throw IllegalArgumentException("Unknown view type")
    }.apply {
        if (forceMatchParent) {
            binding.root.updateLayoutParams<GridLayoutManager.LayoutParams> {
                width = MATCH_PARENT
            }
        }
    }

    override fun onBindViewHolder(holder: YouTubeViewHolder, position: Int) {
        getItem(position)?.let { item ->
            when (holder) {
                is YouTubeHeaderViewHolder -> holder.bind(item as Header)
                is YouTubeArtistHeaderViewHolder -> holder.bind(item as ArtistHeader)
                is YouTubeAlbumOrPlaylistHeaderViewHolder -> holder.bind(item as AlbumOrPlaylistHeader)
                is YouTubeItemContainerViewHolder -> holder.bind(item)
                is YouTubeDescriptionViewHolder -> holder.bind(item as DescriptionSection)
                is YouTubeSeparatorViewHolder -> {}
                is YouTubeNavigationItemViewHolder -> holder.bind(item as NavigationItem)
                is YouTubeNavigationButtonViewHolder -> holder.bind(item as NavigationItem)
                is YouTubeSuggestionViewHolder -> holder.bind(item as SuggestionTextItem)
                is YouTubeListItemViewHolder -> holder.bind(item as Item)
                is YouTubeSquareItemViewHolder -> holder.bind(item as Item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is Header -> BASE_ITEM_HEADER
        is ArtistHeader -> BASE_ITEM_HEADER_ARTIST
        is AlbumOrPlaylistHeader -> BASE_ITEM_HEADER_ALBUM_OR_PLAYLIST
        is CarouselSection -> BASE_ITEM_CAROUSEL
        is GridSection -> BASE_ITEM_GRID
        is DescriptionSection -> BASE_ITEM_DESCRIPTION
        Separator -> BASE_ITEM_SEPARATOR
        is NavigationItem -> BASE_ITEM_NAVIGATION
        is SuggestionTextItem -> BASE_ITEM_SUGGESTION
        else -> ITEM
    }


    class ItemComparator : DiffUtil.ItemCallback<BaseItem>() {
        override fun areItemsTheSame(oldItem: BaseItem, newItem: BaseItem): Boolean = oldItem::class == newItem::class && oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BaseItem, newItem: BaseItem): Boolean = oldItem == newItem
    }

    companion object {
        const val BASE_ITEM_HEADER = 1
        const val BASE_ITEM_HEADER_ARTIST = 2
        const val BASE_ITEM_HEADER_ALBUM_OR_PLAYLIST = 3
        const val BASE_ITEM_CAROUSEL = 4
        const val BASE_ITEM_GRID = 5
        const val BASE_ITEM_DESCRIPTION = 6
        const val BASE_ITEM_SEPARATOR = 7
        const val BASE_ITEM_NAVIGATION = 8
        const val BASE_ITEM_SUGGESTION = 9
        const val ITEM = 10
    }
}