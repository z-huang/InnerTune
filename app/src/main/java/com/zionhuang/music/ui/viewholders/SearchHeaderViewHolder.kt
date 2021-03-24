package com.zionhuang.music.ui.viewholders

import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemSearchHeaderBinding
import com.zionhuang.music.ui.listeners.SearchFilterListener
import com.zionhuang.music.ui.viewholders.base.SearchViewHolder
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.*

class SearchHeaderViewHolder(
        val binding: ItemSearchHeaderBinding,
        val listener: SearchFilterListener,
) : SearchViewHolder(binding.root) {
    init {
        binding.chipGroup.setOnCheckedChangeListener { _, checkedId ->
            val filter = when (checkedId) {
                R.id.chip_all -> ALL
                R.id.chip_songs -> MUSIC_SONGS
                R.id.chip_videos -> MUSIC_VIDEOS
                R.id.chip_playlists -> PLAYLISTS
                R.id.chip_channels -> CHANNELS
                else -> throw IllegalArgumentException("Unexpected filter type.")
            }
            listener.filter = filter
        }
    }

    fun bind() {
        listener.filter.let { filter ->
            val chip = when (filter) {
                ALL -> binding.chipAll
                MUSIC_SONGS -> binding.chipSongs
                VIDEOS -> binding.chipVideos
                PLAYLISTS -> binding.chipPlaylists
                CHANNELS -> binding.chipChannels
                else -> null
            } ?: return
            chip.isChecked = true
        }
    }
}