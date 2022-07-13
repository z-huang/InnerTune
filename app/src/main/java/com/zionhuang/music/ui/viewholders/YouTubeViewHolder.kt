package com.zionhuang.music.ui.viewholders

import androidx.core.view.isVisible
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
    override val binding: ItemSectionHeaderBinding,
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
    override val binding: ItemSectionHeaderArtistBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeViewHolder(binding) {
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

class YouTubeAlbumOrPlaylistHeaderViewHolder(
    override val binding: ItemSectionHeaderAlbumBinding,
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
    override val binding: ItemSectionDescriptionBinding,
) : YouTubeViewHolder(binding) {
    fun bind(section: DescriptionSection) {
        binding.section = section
    }
}

class YouTubeItemContainerViewHolder(
    override val binding: ItemSectionItemBinding,
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
            navigationEndpointHandler.handle(item.navigationEndpoint)
        }
        binding.btnMoreAction.setOnClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(R.menu.youtube_item)
                .setMenuModifier {
                    findItem(R.id.action_radio)?.isVisible = item.menu.radioEndpoint != null
                    findItem(R.id.action_view_artist)?.isVisible = item.menu.artistEndpoint != null
                    findItem(R.id.action_view_album)?.isVisible = item.menu.albumEndpoint != null
                }
                .setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_view_artist -> item.menu.artistEndpoint?.let { endpoint ->
                            navigationEndpointHandler.handle(endpoint)
                        }
                        R.id.action_view_album -> item.menu.albumEndpoint?.let { endpoint ->
                            navigationEndpointHandler.handle(endpoint)
                        }
                    }
                }
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
        binding.root.setOnClickListener {
            navigationEndpointHandler.handle(item.navigationEndpoint)
        }
        binding.root.setOnLongClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(R.menu.youtube_item)
                .setMenuModifier {
                    findItem(R.id.action_radio)?.isVisible = item.menu.radioEndpoint != null
                    findItem(R.id.action_view_artist)?.isVisible = item.menu.artistEndpoint != null
                    findItem(R.id.action_view_album)?.isVisible = item.menu.albumEndpoint != null
                }
                .setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_view_artist -> item.menu.artistEndpoint?.let { endpoint ->
                            navigationEndpointHandler.handle(endpoint)
                        }
                        R.id.action_view_album -> item.menu.albumEndpoint?.let { endpoint ->
                            navigationEndpointHandler.handle(endpoint)
                        }
                    }
                }
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