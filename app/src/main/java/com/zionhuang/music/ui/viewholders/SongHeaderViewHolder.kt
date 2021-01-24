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
    init {
        binding.sortMenu.setOnClickListener { view ->
            PopupMenu(view.context, view).apply {
                inflate(R.menu.sort_song)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.sort_by_create_date -> sortMenuListener.sortByCreateDate()
                        R.id.sort_by_name -> sortMenuListener.sortByName()
                        R.id.sort_by_artist -> sortMenuListener.sortByArtist()
                    }
                    updateSortName()
                    true
                }
                menu.findItem(when (sortMenuListener.sortType()) {
                    ORDER_CREATE_DATE -> R.id.sort_by_create_date
                    ORDER_NAME -> R.id.sort_by_name
                    ORDER_ARTIST -> R.id.sort_by_artist
                    else -> throw IllegalArgumentException("Unexpected sort type.")
                })?.isChecked = true
                show()
            }
        }
        binding.sortMenu.setOnLongClickListener {
            sortMenuListener.toggleSortOrder()
            updateSortOrderIcon()
            true
        }
        updateSortName()
        updateSortOrderIcon(false)
    }

    fun bind(songsCount: Int) {
        binding.songsCount = songsCount
    }

    private fun updateSortName() {
        binding.sortName.setText(when (sortMenuListener.sortType()) {
            ORDER_CREATE_DATE -> R.string.sort_by_create_date
            ORDER_NAME -> R.string.sort_by_name
            ORDER_ARTIST -> R.string.sort_by_artist
            else -> throw IllegalArgumentException("Unexpected sort type.")
        })
    }

    private fun updateSortOrderIcon(animate: Boolean = true) {
        if (sortMenuListener.sortDescending()) {
            binding.sortOrderIcon.animateToDown(animate)
        } else {
            binding.sortOrderIcon.animateToUp(animate)
        }
    }

}