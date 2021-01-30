package com.zionhuang.music.ui.viewholders

import com.google.api.services.youtube.model.SearchResult
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemSearchPlaylistBinding
import com.zionhuang.music.extensions.load
import com.zionhuang.music.extensions.maxResUrl
import com.zionhuang.music.extensions.roundCorner
import com.zionhuang.music.ui.viewholders.base.SearchViewHolder
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem

class SearchPlaylistViewHolder(private val binding: ItemSearchPlaylistBinding) : SearchViewHolder(binding.root) {
    fun bind(item: PlaylistInfoItem) {
        binding.playlistTitle.text = item.name
        binding.uploader.text = item.uploaderName
        binding.streams.text = item.streamCount.toString()
        binding.thumbnail.load(item.thumbnailUrl) {
            placeholder(R.drawable.ic_music_note)
            roundCorner(binding.thumbnail.context.resources.getDimensionPixelSize(R.dimen.song_cover_radius))
        }
    }

    fun bind(item: SearchResult) {
        require(item.id?.kind == "youtube#playlist") { "You should bind a playlist type of [SearchResult] to [SearchStreamViewHolder]" }
        binding.playlistTitle.text = item.snippet?.title
        binding.uploader.text = item.snippet?.channelTitle
        binding.thumbnail.load(item.snippet.thumbnails.maxResUrl) {
            placeholder(R.drawable.ic_music_note)
            roundCorner(binding.thumbnail.context.resources.getDimensionPixelSize(R.dimen.song_cover_radius))
        }
    }
}