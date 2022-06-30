package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.zionhuang.innertube.models.*
import com.zionhuang.music.R
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.viewholders.SectionViewHolder
import com.zionhuang.music.utils.NavigationEndpointHandler

class SectionAdapter(
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : PagingDataAdapter<Section, SectionViewHolder>(SectionComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder =
        SectionViewHolder(parent.inflateWithBinding(R.layout.item_section), navigationEndpointHandler)

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ListSection -> SECTION_ITEM
        is CarouselSection -> SECTION_CAROUSEL
        is GridSection -> SECTION_GRID
        is DescriptionSection -> SECTION_DESCRIPTION
        else -> throw IllegalArgumentException("Unknown item type")
    }

    fun getItemByPosition(position: Int) = getItem(position)

    class SectionComparator : DiffUtil.ItemCallback<Section>() {
        override fun areItemsTheSame(oldItem: Section, newItem: Section): Boolean = oldItem::class == newItem::class && oldItem.header?.title == newItem.header?.title
        override fun areContentsTheSame(oldItem: Section, newItem: Section): Boolean = oldItem == newItem
    }

    companion object {
        const val SECTION_ITEM = 1
        const val SECTION_CAROUSEL = 2
        const val SECTION_GRID = 3
        const val SECTION_DESCRIPTION = 4
    }
}