package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.zionhuang.innertube.models.*
import com.zionhuang.music.R
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.viewholders.*
import com.zionhuang.music.utils.NavigationEndpointHandler

class SectionAdapter(
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : PagingDataAdapter<Section, SectionViewHolder>(SectionComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder = when (viewType) {
        SECTION_HEADER -> SectionHeaderViewHolder(parent.inflateWithBinding(R.layout.item_section_header), navigationEndpointHandler)
        SECTION_HEADER_ARTIST -> SectionArtistHeaderViewHolder(parent.inflateWithBinding(R.layout.item_section_header_artist), navigationEndpointHandler)
        SECTION_HEADER_ALBUM_OR_PLAYLIST -> SectionAlbumOrPlaylistHeaderViewHolder(parent.inflateWithBinding(R.layout.item_section_header_album), navigationEndpointHandler)
        SECTION_DESCRIPTION -> SectionDescriptionViewHolder(parent.inflateWithBinding(R.layout.item_section_description))
        else -> SectionItemViewHolder(parent.inflateWithBinding(R.layout.item_section_item), navigationEndpointHandler)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        getItem(position)?.let {
            when (holder) {
                is SectionHeaderViewHolder -> holder.bind(it as Header)
                is SectionArtistHeaderViewHolder -> holder.bind(it as ArtistHeader)
                is SectionAlbumOrPlaylistHeaderViewHolder -> holder.bind(it as AlbumOrPlaylistHeader)
                is SectionDescriptionViewHolder -> holder.bind(it as DescriptionSection)
                is SectionItemViewHolder -> holder.bind(it)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is Header -> SECTION_HEADER
        is ArtistHeader -> SECTION_HEADER_ARTIST
        is AlbumOrPlaylistHeader -> SECTION_HEADER_ALBUM_OR_PLAYLIST
        is ListSection -> SECTION_ITEM
        is CarouselSection -> SECTION_CAROUSEL
        is GridSection -> SECTION_GRID
        is DescriptionSection -> SECTION_DESCRIPTION
        else -> throw IllegalArgumentException("Unknown item type")
    }

    fun getItemByPosition(position: Int) = getItem(position)

    class SectionComparator : DiffUtil.ItemCallback<Section>() {
        override fun areItemsTheSame(oldItem: Section, newItem: Section): Boolean = oldItem::class == newItem::class && oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Section, newItem: Section): Boolean = oldItem == newItem
    }

    companion object {
        const val SECTION_HEADER = 1
        const val SECTION_HEADER_ARTIST = 2
        const val SECTION_HEADER_ALBUM_OR_PLAYLIST = 3
        const val SECTION_ITEM = 4
        const val SECTION_CAROUSEL = 5
        const val SECTION_GRID = 6
        const val SECTION_DESCRIPTION = 7
    }
}