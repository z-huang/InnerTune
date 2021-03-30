package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.zionhuang.music.R
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.viewholders.SearchChannelViewHolder
import com.zionhuang.music.ui.viewholders.SearchPlaylistViewHolder
import com.zionhuang.music.ui.viewholders.SearchStreamViewHolder
import com.zionhuang.music.ui.viewholders.base.SearchViewHolder
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class InfoItemAdapter : PagingDataAdapter<InfoItem, SearchViewHolder>(InfoItemComparator()) {
    companion object {
        const val ITEM_HEADER = 0
        const val ITEM_VIDEO = 1
        const val ITEM_CHANNEL = 2
        const val ITEM_PLAYLIST = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder = when (viewType) {
        ITEM_VIDEO -> SearchStreamViewHolder(parent.inflateWithBinding(R.layout.item_search_stream))
        ITEM_CHANNEL -> SearchChannelViewHolder(parent.inflateWithBinding(R.layout.item_search_channel))
        ITEM_PLAYLIST -> SearchPlaylistViewHolder(parent.inflateWithBinding(R.layout.item_search_playlist))
        else -> throw IllegalArgumentException("Unexpected item type.")
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        when (holder) {
            is SearchStreamViewHolder -> holder.bind(getItem(position) as StreamInfoItem)
            is SearchChannelViewHolder -> holder.bind(getItem(position) as ChannelInfoItem)
            is SearchPlaylistViewHolder -> holder.bind(getItem(position) as PlaylistInfoItem)
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is StreamInfoItem -> ITEM_VIDEO
        is ChannelInfoItem -> ITEM_CHANNEL
        is PlaylistInfoItem -> ITEM_PLAYLIST
        else -> -1
    }

    fun getItemByPosition(position: Int) = getItem(position)

    class InfoItemComparator : DiffUtil.ItemCallback<InfoItem>() {
        override fun areItemsTheSame(oldItem: InfoItem, newItem: InfoItem): Boolean = oldItem.url == newItem.url
        override fun areContentsTheSame(oldItem: InfoItem, newItem: InfoItem): Boolean = true
    }
}