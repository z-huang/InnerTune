package com.zionhuang.music.ui.viewholders

import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.innertube.models.Item
import com.zionhuang.innertube.models.NavigationItem
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.models.SuggestionTextItem
import com.zionhuang.music.R
import com.zionhuang.music.databinding.*
import com.zionhuang.music.extensions.context
import com.zionhuang.music.extensions.show
import com.zionhuang.music.ui.fragments.MenuBottomSheetDialogFragment
import com.zionhuang.music.utils.NavigationEndpointHandler

sealed class YouTubeItemViewHolder(open val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

class YouTubeListItemViewHolder(
    override val binding: ItemYoutubeListBinding,
    private val navigationEndpointHandler: NavigationEndpointHandler,
) : YouTubeItemViewHolder(binding) {
    fun bind(item: Item) {
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
    fun bind(item: Item) {
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
) : YouTubeItemViewHolder(binding) {
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
) : YouTubeItemViewHolder(binding) {
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
) : YouTubeItemViewHolder(binding) {
    fun bind(item: SuggestionTextItem) {
        binding.query = item.title
        binding.executePendingBindings()
        binding.root.setOnClickListener { onSearch(item.title) }
        binding.fillTextButton.setOnClickListener { onFillQuery(item.title) }
    }
}

class YouTubeSeparatorViewHolder(
    override val binding: ItemSeparatorBinding,
) : YouTubeItemViewHolder(binding)