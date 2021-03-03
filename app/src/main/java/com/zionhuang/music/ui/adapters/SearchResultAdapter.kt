package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.google.api.services.youtube.model.SearchResult
import com.zionhuang.music.R
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.viewholders.SearchChannelViewHolder
import com.zionhuang.music.ui.viewholders.SearchPlaylistViewHolder
import com.zionhuang.music.ui.viewholders.SearchStreamViewHolder
import com.zionhuang.music.ui.viewholders.base.SearchViewHolder

class SearchResultAdapter : PagingDataAdapter<SearchResult, SearchViewHolder>(SearchResultItemComparator()) {
    companion object {
        const val ITEM_VIDEO = 0
        const val ITEM_CHANNEL = 1
        const val ITEM_PLAYLIST = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder = when (viewType) {
        ITEM_VIDEO -> SearchStreamViewHolder(parent.inflateWithBinding(R.layout.item_search_stream))
        ITEM_CHANNEL -> SearchChannelViewHolder(parent.inflateWithBinding(R.layout.item_search_channel))
        ITEM_PLAYLIST -> SearchPlaylistViewHolder(parent.inflateWithBinding(R.layout.item_search_playlist))
        else -> throw IllegalArgumentException("Unexpected item type.")
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        when (holder) {
            is SearchStreamViewHolder -> holder.bind(getItem(position)!!)
            is SearchChannelViewHolder -> holder.bind(getItem(position)!!)
            is SearchPlaylistViewHolder -> holder.bind(getItem(position)!!)
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)?.id?.kind) {
        "youtube#video" -> ITEM_VIDEO
        "youtube#channel" -> ITEM_CHANNEL
        "youtube#playlist" -> ITEM_PLAYLIST
        else -> -1
    }

    fun getItemByPosition(position: Int) = getItem(position)

    class SearchResultItemComparator : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean = oldItem.etag == newItem.etag
    }
}