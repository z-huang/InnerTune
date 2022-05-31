package com.zionhuang.music.ui.viewholders

import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemSearchChannelBinding
import com.zionhuang.music.extensions.circle
import com.zionhuang.music.extensions.context
import com.zionhuang.music.extensions.load
import com.zionhuang.music.ui.viewholders.base.SearchViewHolder
import org.schabi.newpipe.extractor.channel.ChannelInfoItem

class SearchChannelViewHolder(override val binding: ItemSearchChannelBinding) : SearchViewHolder(binding) {
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
}