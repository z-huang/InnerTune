package com.zionhuang.music.ui.viewholders

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.zionhuang.innertube.models.*
import com.zionhuang.music.databinding.ItemSectionDescriptionBinding
import com.zionhuang.music.databinding.ItemSectionHeaderBinding
import com.zionhuang.music.databinding.ItemSectionItemBinding
import com.zionhuang.music.extensions.context
import com.zionhuang.music.ui.adapters.YouTubeItemAdapter
import com.zionhuang.music.utils.NavigationEndpointHandler

sealed class SectionViewHolder(open val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

class SectionHeaderViewHolder(
    override val binding: ItemSectionHeaderBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : SectionViewHolder(binding) {
    fun bind(header: Header) {
        binding.header = header
    }
}

class SectionDescriptionViewHolder(
    override val binding: ItemSectionDescriptionBinding,
) : SectionViewHolder(binding) {
    fun bind(section: DescriptionSection) {
        binding.section = section
    }
}

class SectionItemViewHolder(
    override val binding: ItemSectionItemBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : SectionViewHolder(binding) {
    fun bind(section: Section) {
        when (section) {
            is ListSection -> {
                val itemAdapter = YouTubeItemAdapter(section.itemViewType, false, navigationEndpointHandler)
                binding.recyclerView.layoutManager = LinearLayoutManager(binding.context)
                binding.recyclerView.adapter = itemAdapter
                itemAdapter.submitList(section.items)
            }
            is CarouselSection -> {
                val itemAdapter = YouTubeItemAdapter(section.itemViewType, false, navigationEndpointHandler)
                binding.recyclerView.layoutManager = GridLayoutManager(binding.context, section.numItemsPerColumn, RecyclerView.HORIZONTAL, false)
                binding.recyclerView.adapter = itemAdapter
                itemAdapter.submitList(section.items)
            }
            is GridSection -> {
                val itemAdapter = YouTubeItemAdapter(
                    if (section.items[0].let { it is NavigationItem && it.stripeColor == null }) Section.ViewType.LIST // [New releases, Charts, Moods & genres] in Explore tab
                    else Section.ViewType.BLOCK,
                    true,
                    navigationEndpointHandler
                )
                binding.recyclerView.layoutManager = GridLayoutManager(binding.context, if (section.items[0].let { it is NavigationItem && it.stripeColor == null }) 1 else 2) // TODO spanCount for bigger screen
                binding.recyclerView.adapter = itemAdapter
                itemAdapter.submitList(section.items)
            }
            else -> {}
        }
    }
}