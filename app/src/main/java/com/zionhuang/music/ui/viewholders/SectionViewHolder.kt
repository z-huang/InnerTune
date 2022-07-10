package com.zionhuang.music.ui.viewholders

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.zionhuang.innertube.models.*
import com.zionhuang.music.databinding.*
import com.zionhuang.music.extensions.context
import com.zionhuang.music.ui.adapters.YouTubeBaseItemAdapter
import com.zionhuang.music.utils.NavigationEndpointHandler

sealed class SectionViewHolder(open val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

class SectionHeaderViewHolder(
    override val binding: ItemSectionHeaderBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : SectionViewHolder(binding) {
    fun bind(header: Header) {
        binding.header = header
        header.moreNavigationEndpoint?.let { endpoint ->
            binding.root.isClickable = true
            binding.root.setOnClickListener {
                navigationEndpointHandler.handle(endpoint)
            }
        }
    }
}

class SectionArtistHeaderViewHolder(
    override val binding: ItemSectionHeaderArtistBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : SectionViewHolder(binding) {
    fun bind(header: ArtistHeader) {
        binding.header = header
        binding.btnShuffle.setOnClickListener {
            navigationEndpointHandler.handle(header.shuffleEndpoint)
        }
        binding.btnRadio.setOnClickListener {
            navigationEndpointHandler.handle(header.radioEndpoint)
        }
    }
}

class SectionAlbumOrPlaylistHeaderViewHolder(
    override val binding: ItemSectionHeaderAlbumBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : SectionViewHolder(binding) {
    fun bind(header: AlbumOrPlaylistHeader) {
        binding.header = header
        binding.btnPlay.setOnClickListener {
            header.menu.playEndpoint?.let { endpoint ->
                navigationEndpointHandler.handle(endpoint)
            }
        }
        binding.btnShuffle.setOnClickListener {
            header.menu.shuffleEndpoint?.let { endpoint ->
                navigationEndpointHandler.handle(endpoint)
            }
        }
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
                val itemAdapter = YouTubeBaseItemAdapter(section.itemViewType, false, navigationEndpointHandler)
                binding.recyclerView.layoutManager = LinearLayoutManager(binding.context)
                binding.recyclerView.adapter = itemAdapter
                itemAdapter.submitList(section.items)
            }
            is CarouselSection -> {
                val itemAdapter = YouTubeBaseItemAdapter(section.itemViewType, false, navigationEndpointHandler)
                binding.recyclerView.layoutManager = GridLayoutManager(binding.context, section.numItemsPerColumn, RecyclerView.HORIZONTAL, false)
                binding.recyclerView.adapter = itemAdapter
                itemAdapter.submitList(section.items)
            }
            is GridSection -> {
                val itemAdapter = YouTubeBaseItemAdapter(
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