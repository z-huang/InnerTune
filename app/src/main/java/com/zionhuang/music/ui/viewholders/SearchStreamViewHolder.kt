package com.zionhuang.music.ui.viewholders

import androidx.core.view.isVisible
import com.google.api.services.youtube.model.SearchResult
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemSearchStreamBinding
import com.zionhuang.music.extensions.load
import com.zionhuang.music.extensions.maxResUrl
import com.zionhuang.music.extensions.roundCorner
import com.zionhuang.music.ui.viewholders.base.SearchViewHolder
import com.zionhuang.music.utils.makeTimeString
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.text.DateFormat
import java.util.*

class SearchStreamViewHolder(private val binding: ItemSearchStreamBinding) : SearchViewHolder(binding.root) {
    fun bind(item: StreamInfoItem) {
        binding.songTitle.text = item.name
        binding.duration.text = makeTimeString(item.duration)
        binding.songArtist.text = item.uploaderName
        binding.publishDate.text = item.textualUploadDate
        if (!item.textualUploadDate.isNullOrEmpty()) {
            binding.publishBullet.isVisible = false
        }
        binding.thumbnail.load(item.thumbnailUrl) {
            placeholder(R.drawable.ic_music_note)
            roundCorner(binding.thumbnail.context.resources.getDimensionPixelSize(R.dimen.song_cover_radius))
        }
    }

    fun bind(item: SearchResult) {
        require(item.id?.kind == "youtube#video") { "You should bind a video type of [SearchResult] to [SearchStreamViewHolder]" }
        binding.songTitle.text = item.snippet?.title
        binding.duration.isVisible = false
        binding.durationBullet.isVisible = false
        binding.songArtist.text = item.snippet?.channelTitle
        binding.publishDate.text = DateFormat.getDateInstance().format(Date(item.snippet.publishedAt.value))
        binding.thumbnail.load(item.snippet.thumbnails.maxResUrl) {
            placeholder(R.drawable.ic_music_note)
            roundCorner(binding.thumbnail.context.resources.getDimensionPixelSize(R.dimen.song_cover_radius))
        }
    }
}