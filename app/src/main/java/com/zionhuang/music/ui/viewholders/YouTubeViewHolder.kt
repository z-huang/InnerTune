package com.zionhuang.music.ui.viewholders

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.innertube.models.*
import com.zionhuang.innertube.models.SuggestionTextItem.SuggestionSource.LOCAL
import com.zionhuang.music.R
import com.zionhuang.music.databinding.*
import com.zionhuang.music.extensions.context
import com.zionhuang.music.extensions.show
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.adapters.YouTubeItemAdapter
import com.zionhuang.music.ui.fragments.MenuBottomSheetDialogFragment
import com.zionhuang.music.ui.viewholders.base.BindingViewHolder
import com.zionhuang.music.utils.NavigationEndpointHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

sealed class YouTubeViewHolder<T : ViewDataBinding>(viewGroup: ViewGroup, @LayoutRes layoutId: Int) : BindingViewHolder<T>(viewGroup, layoutId)

class YouTubeHeaderViewHolder(
    val viewGroup: ViewGroup,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeViewHolder<ItemYoutubeHeaderBinding>(viewGroup, R.layout.item_youtube_header) {
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
    val viewGroup: ViewGroup,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeViewHolder<ItemYoutubeHeaderArtistBinding>(viewGroup, R.layout.item_youtube_header_artist) {
    fun bind(header: ArtistHeader) {
        binding.header = header
        val bannerThumbnail = header.bannerThumbnails.last()
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
    val viewGroup: ViewGroup,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeViewHolder<ItemYoutubeHeaderAlbumBinding>(viewGroup, R.layout.item_youtube_header_album) {
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
    val viewGroup: ViewGroup,
) : YouTubeViewHolder<ItemYoutubeDescriptionBinding>(viewGroup, R.layout.item_youtube_description) {
    fun bind(section: DescriptionSection) {
        binding.section = section
    }
}

class YouTubeItemContainerViewHolder(
    val viewGroup: ViewGroup,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeViewHolder<ItemRecyclerviewBinding>(viewGroup, R.layout.item_recyclerview) {
    fun bind(section: YTBaseItem) {
        when (section) {
            is CarouselSection -> {
                val itemAdapter = YouTubeItemAdapter(navigationEndpointHandler, section.itemViewType, false)
                binding.recyclerView.layoutManager = GridLayoutManager(binding.context, section.numItemsPerColumn, RecyclerView.HORIZONTAL, false)
                binding.recyclerView.adapter = itemAdapter
                itemAdapter.submitList(section.items)
            }
            is GridSection -> {
                val itemAdapter = YouTubeItemAdapter(navigationEndpointHandler, YTBaseItem.ViewType.BLOCK, true)
                binding.recyclerView.layoutManager = GridLayoutManager(binding.context, 2) // TODO spanCount for landscape or bigger screen
                binding.recyclerView.adapter = itemAdapter
                itemAdapter.submitList(section.items)
            }
            else -> {}
        }
    }
}

sealed class YouTubeItemViewHolder<T : ViewDataBinding>(viewGroup: ViewGroup, @LayoutRes layoutId: Int) : YouTubeViewHolder<T>(viewGroup, layoutId) {
    abstract fun bind(item: YTItem)
}

class YouTubeListItemViewHolder(
    val viewGroup: ViewGroup,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeItemViewHolder<ItemYoutubeListBinding>(viewGroup, R.layout.item_youtube_list) {
    override fun bind(item: YTItem) {
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
    val viewGroup: ViewGroup,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeItemViewHolder<ItemYoutubeSquareBinding>(viewGroup, R.layout.item_youtube_square) {
    override fun bind(item: YTItem) {
        binding.item = item
        val thumbnail = item.thumbnails.last()
        binding.thumbnail.updateLayoutParams<ConstraintLayout.LayoutParams> {
            dimensionRatio = "${thumbnail.width}:${thumbnail.height}"
        }
        listOf(1) + listOf(1)
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
    val viewGroup: ViewGroup,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeViewHolder<ItemYoutubeNavigationBinding>(viewGroup, R.layout.item_youtube_navigation) {
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

class YouTubeNavigationTileViewHolder(
    val viewGroup: ViewGroup,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeViewHolder<ItemYoutubeNavigationTileBinding>(viewGroup, R.layout.item_youtube_navigation_tile) {
    fun bind(item: NavigationItem) {
        binding.item = item
        binding.card.setOnClickListener {
            navigationEndpointHandler.handle(item.navigationEndpoint)
        }
        binding.executePendingBindings()
    }
}

class YouTubeSuggestionViewHolder(
    val viewGroup: ViewGroup,
    private val onFillQuery: (String) -> Unit,
    private val onSearch: (String) -> Unit,
    private val onRefreshSuggestions: () -> Unit,
) : YouTubeViewHolder<ItemYoutubeSuggestionBinding>(viewGroup, R.layout.item_youtube_suggestion) {
    @OptIn(DelicateCoroutinesApi::class)
    fun bind(item: SuggestionTextItem) {
        binding.item = item
        binding.executePendingBindings()
        binding.root.setOnClickListener { onSearch(item.query) }
        binding.fillTextBtn.setOnClickListener { onFillQuery(item.query) }
        if (item.source == LOCAL) {
            binding.deleteBtn.setOnClickListener {
                GlobalScope.launch {
                    SongRepository.deleteSearchHistory(item.query)
                    onRefreshSuggestions()
                }
            }
        }
    }
}

class YouTubeSeparatorViewHolder(
    val viewGroup: ViewGroup,
) : YouTubeViewHolder<ItemSeparatorBinding>(viewGroup, R.layout.item_separator)