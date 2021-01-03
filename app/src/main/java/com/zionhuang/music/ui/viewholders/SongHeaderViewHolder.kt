package com.zionhuang.music.ui.viewholders

import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.constants.ORDER_ARTIST
import com.zionhuang.music.constants.ORDER_CREATE_DATE
import com.zionhuang.music.constants.ORDER_NAME
import com.zionhuang.music.databinding.ItemSongHeaderBinding
import com.zionhuang.music.ui.listeners.SortMenuListener

class SongHeaderViewHolder(
        val binding: ItemSongHeaderBinding,
        private val sortMenuListener: SortMenuListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(songsCount: Int) {
        binding.allSongsCount.text = binding.root.context.resources.getQuantityString(R.plurals.channel_songs_count, songsCount, songsCount)
        binding.sortMenu.setOnClickListener { view ->
            PopupMenu(view.context, view).apply {
                inflate(R.menu.sort_song)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.sort_by_create_date -> sortMenuListener.sortByCreateDate()
                        R.id.sort_by_name -> sortMenuListener.sortByName()
                        R.id.sort_by_artist -> sortMenuListener.sortByArtist()
                    }
                    true
                }
                when (sortMenuListener.sortType()) {
                    ORDER_CREATE_DATE -> R.id.sort_by_create_date
                    ORDER_NAME -> R.id.sort_by_name
                    ORDER_ARTIST -> R.id.sort_by_artist
                    else -> null
                }?.let {
                    menu.findItem(it)?.isChecked = true
                }
                show()
            }
        }
    }
}