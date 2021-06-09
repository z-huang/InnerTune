package com.zionhuang.music.ui.viewholders

import androidx.core.view.isVisible
import com.google.api.services.youtube.model.SearchResult
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemSearchChannelBinding
import com.zionhuang.music.extensions.circle
import com.zionhuang.music.extensions.context
import com.zionhuang.music.extensions.load
import com.zionhuang.music.extensions.maxResUrl
import com.zionhuang.music.ui.viewholders.base.SearchViewHolder
import org.schabi.newpipe.extractor.channel.ChannelInfoItem

class SearchChannelViewHolder(private val binding: ItemSearchChannelBinding) : SearchViewHolder(binding.root) {
    private val context = binding.context

    fun bind(item: ChannelInfoItem) {
        binding.root.transitionName = binding.context.resources.getString(
            R.string.youtube_channel_item_transition_name,
            item.url
        )
        binding.channelTitle.text = item.name
        binding.subscribers.text = context.resources.getQuantityString(R.plurals.subscribers, item.subscriberCount.toInt(), item.subscriberCount.toInt())
        binding.streams.text = context.resources.getQuantityString(R.plurals.videos, item.streamCount.toInt(), item.streamCount.toInt())
        binding.thumbnail.load(item.thumbnailUrl) {
            placeholder(R.drawable.ic_music_note)
            circle()
        }
    }

    fun bind(item: SearchResult) {
        require(item.id?.kind == "youtube#channel") { "You should bind a channel type of [SearchResult] to [SearchStreamViewHolder]" }
        binding.root.transitionName = binding.context.resources.getString(
            R.string.youtube_channel_item_transition_name,
            item.id
        )
        binding.channelTitle.text = item.snippet?.channelTitle
        binding.bullet.isVisible = false
        binding.thumbnail.load(item.snippet.thumbnails.maxResUrl) {
            placeholder(R.drawable.ic_music_note)
            circle()
        }
    }
}