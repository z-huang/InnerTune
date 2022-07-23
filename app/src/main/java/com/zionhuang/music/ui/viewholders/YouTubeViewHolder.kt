package com.zionhuang.music.ui.viewholders

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.innertube.models.*
import com.zionhuang.music.R
import com.zionhuang.music.databinding.*
import com.zionhuang.music.extensions.context
import com.zionhuang.music.extensions.show
import com.zionhuang.music.ui.adapters.YouTubeItemAdapter
import com.zionhuang.music.ui.fragments.MenuBottomSheetDialogFragment
import com.zionhuang.music.utils.NavigationEndpointHandler

sealed class YouTubeViewHolder(open val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)


class YouTubeHeaderViewHolder(
    override val binding: ItemYoutubeHeaderBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeViewHolder(binding) {
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

class YouTubeArtistHeaderViewHolder(
    override val binding: ItemYoutubeHeaderArtistBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeViewHolder(binding) {
    fun bind(header: ArtistHeader) {
        binding.header = header
        val bannerThumbnail = header.bannerThumbnail.last()
        binding.banner.updateLayoutParams<ConstraintLayout.LayoutParams> {
            dimensionRatio = "${bannerThumbnail.width}:${bannerThumbnail.height}"
        }
        binding.btnShuffle.setOnClickListener {
            navigationEndpointHandler.handle(header.shuffleEndpoint)
        }
        binding.btnRadio.setOnClickListener {
            navigationEndpointHandler.handle(header.radioEndpoint)
        }
    }
}

class YouTubeAlbumOrPlaylistHeaderViewHolder(
    override val binding: ItemYoutubeHeaderAlbumBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeViewHolder(binding) {
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

class YouTubeDescriptionViewHolder(
    override val binding: ItemYoutubeDescriptionBinding,
) : YouTubeViewHolder(binding) {
    fun bind(section: DescriptionSection) {
        binding.section = section
    }
}

class YouTubeItemContainerViewHolder(
    override val binding: ItemRecyclerviewBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeViewHolder(binding) {
    fun bind(section: BaseItem) {
        when (section) {
            is CarouselSection -> {
                val itemAdapter = YouTubeItemAdapter(navigationEndpointHandler, section.itemViewType, false)
                binding.recyclerView.layoutManager = GridLayoutManager(binding.context, section.numItemsPerColumn, RecyclerView.HORIZONTAL, false)
                binding.recyclerView.adapter = itemAdapter
                itemAdapter.submitList(section.items)
            }
            is GridSection -> {
                val itemAdapter = YouTubeItemAdapter(navigationEndpointHandler, BaseItem.ViewType.BLOCK, true)
                binding.recyclerView.layoutManager = GridLayoutManager(binding.context, 2) // TODO spanCount for landscape or bigger screen
                binding.recyclerView.adapter = itemAdapter
                itemAdapter.submitList(section.items)
            }
            else -> {}
        }
    }
}

sealed class YouTubeItemViewHolder(override val binding: ViewDataBinding) : YouTubeViewHolder(binding) {
    abstract fun bind(item: Item)
}

class YouTubeListItemViewHolder(
    override val binding: ItemYoutubeListBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeItemViewHolder(binding) {
    override fun bind(item: Item) {
        binding.item = item
        if (item is SongItem && item.index != null) {
            binding.index.text = item.index
        }
        binding.secondaryLine.isVisible = !item.subtitle.isNullOrEmpty()
        binding.root.setOnClickListener {
            navigationEndpointHandler.handle(item.navigationEndpoint, item)
        }
        binding.btnMoreAction.setOnClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(item, navigationEndpointHandler)
                .show(binding.context)
        }
        binding.executePendingBindings()
    }
}

class YouTubeSquareItemViewHolder(
    override val binding: ItemYoutubeSquareBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeItemViewHolder(binding) {
    override fun bind(item: Item) {
        binding.item = item
        val thumbnail = item.thumbnails.last()
        binding.thumbnail.updateLayoutParams<ConstraintLayout.LayoutParams> {
            dimensionRatio = "${thumbnail.width}:${thumbnail.height}"
        }
        binding.root.setOnClickListener {
            navigationEndpointHandler.handle(item.navigationEndpoint, item)
        }
        binding.root.setOnLongClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(item, navigationEndpointHandler)
                .show(binding.context)
            true
        }
        binding.executePendingBindings()
    }
}

class YouTubeNavigationItemViewHolder(
    override val binding: ItemYoutubeNavigationBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeViewHolder(binding) {
    fun bind(item: NavigationItem) {
        binding.item = item
        when (item.icon) {
            "MUSIC_NEW_RELEASE" -> R.drawable.ic_new_releases
            "TRENDING_UP" -> R.drawable.ic_trending_up
            "STICKER_EMOTICON" -> R.drawable.ic_sentiment_satisfied
            else -> null
        }?.let {
            binding.icon.setImageResource(it)
        }
        binding.container.setOnClickListener {
            navigationEndpointHandler.handle(item.navigationEndpoint)
        }
        binding.executePendingBindings()
    }
}

class YouTubeNavigationButtonViewHolder(
    override val binding: ItemYoutubeNavigationBtnBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeViewHolder(binding) {
    fun bind(item: NavigationItem) {
        binding.item = item
        binding.card.setOnClickListener {
            navigationEndpointHandler.handle(item.navigationEndpoint)
        }
        binding.executePendingBindings()
    }
}

class YouTubeSuggestionViewHolder(
    override val binding: ItemYoutubeSuggestionBinding,
    private val onFillQuery: (String) -> Unit,
    private val onSearch: (String) -> Unit,
) : YouTubeViewHolder(binding) {
    fun bind(item: SuggestionTextItem) {
        binding.query = item.query
        binding.executePendingBindings()
        binding.root.setOnClickListener { onSearch(item.query) }
        binding.fillTextButton.setOnClickListener { onFillQuery(item.query) }
    }
}

class YouTubeSeparatorViewHolder(
    override val binding: ItemSeparatorBinding,
) : YouTubeViewHolder(binding)