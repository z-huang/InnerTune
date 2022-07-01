package com.zionhuang.music.ui.viewholders

import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.innertube.models.*
import com.zionhuang.music.databinding.ItemSectionBinding
import com.zionhuang.music.extensions.context
import com.zionhuang.music.ui.adapters.YouTubeItemAdapter
import com.zionhuang.music.utils.NavigationEndpointHandler

class SectionViewHolder(
    val binding: ItemSectionBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(section: Section) {
        binding.header.isVisible = section.header != null
        section.header?.let {
            binding.title.text = it.title
            binding.subtitle.isVisible = !it.subtitle.isNullOrEmpty()
            binding.subtitle.text = it.subtitle
            binding.btnMore.isVisible = it.moreNavigationEndpoint != null
        }
        binding.description.isVisible = section is DescriptionSection
        binding.recyclerView.isVisible = section !is DescriptionSection
        when (section) {
            is DescriptionSection -> {
                binding.description.text = section.description
            }
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
        }
    }
}