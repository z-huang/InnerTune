package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.view.updateLayoutParams
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.SelectionTracker.SELECTION_CHANGED_MARKER
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.zionhuang.innertube.models.*
import com.zionhuang.music.ui.viewholders.*
import com.zionhuang.music.utils.NavigationEndpointHandler

/**
 * Same as [YouTubeItemAdapter], but extends [PagingDataAdapter]
 */
class YouTubeItemPagingAdapter(
    private val navigationEndpointHandler: NavigationEndpointHandler,
    private val itemViewType: YTBaseItem.ViewType = YTBaseItem.ViewType.LIST,
    private val forceMatchParent: Boolean = false,
) : PagingDataAdapter<YTBaseItem, YouTubeViewHolder<*>>(ItemComparator()) {
    var tracker: SelectionTracker<String>? = null
    var onFillQuery: (String) -> Unit = {}
    var onSearch: (String) -> Unit = {}
    var onRefreshSuggestions: () -> Unit = {}
    var onPlayAlbum: (() -> Unit)? = null
    var onShuffleAlbum: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YouTubeViewHolder<*> = when (viewType) {
        BASE_ITEM_HEADER -> YouTubeHeaderViewHolder(parent, navigationEndpointHandler)
        BASE_ITEM_HEADER_ARTIST -> YouTubeArtistHeaderViewHolder(parent, navigationEndpointHandler)
        BASE_ITEM_HEADER_ALBUM_OR_PLAYLIST -> YouTubeAlbumOrPlaylistHeaderViewHolder(parent, navigationEndpointHandler, onPlayAlbum, onShuffleAlbum)
        BASE_ITEM_CAROUSEL, BASE_ITEM_GRID -> YouTubeItemContainerViewHolder(parent, navigationEndpointHandler)
        BASE_ITEM_DESCRIPTION -> YouTubeDescriptionViewHolder(parent)
        BASE_ITEM_SEPARATOR -> YouTubeSeparatorViewHolder(parent)
        BASE_ITEM_NAVIGATION -> when (itemViewType) {
            YTBaseItem.ViewType.LIST -> YouTubeNavigationItemViewHolder(parent, navigationEndpointHandler)
            YTBaseItem.ViewType.BLOCK -> YouTubeNavigationTileViewHolder(parent, navigationEndpointHandler)
        }
        BASE_ITEM_SUGGESTION -> YouTubeSuggestionViewHolder(parent, onFillQuery, onSearch, onRefreshSuggestions)
        ITEM -> when (itemViewType) {
            YTBaseItem.ViewType.LIST -> YouTubeListItemViewHolder(parent, navigationEndpointHandler)
            YTBaseItem.ViewType.BLOCK -> YouTubeSquareItemViewHolder(parent, navigationEndpointHandler)
        }
        else -> throw IllegalArgumentException("Unknown view type")
    }.apply {
        if (forceMatchParent) {
            binding.root.updateLayoutParams<GridLayoutManager.LayoutParams> {
                width = MATCH_PARENT
            }
        }
    }

    override fun onBindViewHolder(holder: YouTubeViewHolder<*>, position: Int) {
        getItem(position)?.let { item ->
            when (holder) {
                is YouTubeHeaderViewHolder -> holder.bind(item as Header)
                is YouTubeArtistHeaderViewHolder -> holder.bind(item as ArtistHeader)
                is YouTubeAlbumOrPlaylistHeaderViewHolder -> holder.bind(item as AlbumOrPlaylistHeader)
                is YouTubeItemContainerViewHolder -> holder.bind(item)
                is YouTubeDescriptionViewHolder -> holder.bind(item as DescriptionSection)
                is YouTubeSeparatorViewHolder -> {}
                is YouTubeNavigationItemViewHolder -> holder.bind(item as NavigationItem)
                is YouTubeNavigationTileViewHolder -> holder.bind(item as NavigationItem)
                is YouTubeSuggestionViewHolder -> holder.bind(item as SuggestionTextItem)
                is YouTubeListItemViewHolder -> holder.bind(item as YTItem, tracker?.isSelected(item.id) ?: false)
                is YouTubeSquareItemViewHolder -> holder.bind(item as YTItem)
            }
        }
    }

    override fun onBindViewHolder(holder: YouTubeViewHolder<*>, position: Int, payloads: MutableList<Any>) {
        val payload = payloads.firstOrNull()
        when {
            payload == SELECTION_CHANGED_MARKER && holder is YouTubeListItemViewHolder -> holder.onSelectionChanged(tracker?.isSelected(getItem(position)!!.id) ?: false)
            else -> onBindViewHolder(holder, position)
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

    fun getItemAt(position: Int) = getItem(position)

    class ItemComparator : DiffUtil.ItemCallback<YTBaseItem>() {
        override fun areItemsTheSame(oldItem: YTBaseItem, newItem: YTBaseItem) = oldItem::class == newItem::class && oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: YTBaseItem, newItem: YTBaseItem) = oldItem == newItem
        override fun getChangePayload(oldItem: YTBaseItem, newItem: YTBaseItem) = newItem
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