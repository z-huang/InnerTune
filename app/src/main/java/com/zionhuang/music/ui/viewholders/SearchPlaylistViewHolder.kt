package com.zionhuang.music.ui.viewholders

import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemSearchPlaylistBinding
import com.zionhuang.music.extensions.context
import com.zionhuang.music.extensions.load
import com.zionhuang.music.extensions.roundCorner
import com.zionhuang.music.ui.viewholders.base.SearchViewHolder
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem

class SearchPlaylistViewHolder(private val binding: ItemSearchPlaylistBinding) : SearchViewHolder(binding.root) {
    fun bind(item: PlaylistInfoItem) {
        binding.root.transitionName = binding.context.resources.getString(R.string.youtube_playlist_item_transition_name, item.url)
        binding.playlistTitle.text = item.name
        binding.uploader.text = item.uploaderName
        if (item.streamCount > 0) {
            binding.streams.text = item.streamCount.toString()
        } else {
            binding.thumbnail.alpha = 1f
        }
        binding.thumbnail.load(item.thumbnailUrl) {
            placeholder(R.drawable.ic_music_note)
            roundCorner(binding.thumbnail.context.resources.getDimensionPixelSize(R.dimen.song_cover_radius))
        }
    }
}