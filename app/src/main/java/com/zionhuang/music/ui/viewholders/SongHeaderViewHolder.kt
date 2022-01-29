package com.zionhuang.music.ui.viewholders

import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.constants.ORDER_ARTIST
import com.zionhuang.music.constants.ORDER_CREATE_DATE
import com.zionhuang.music.constants.ORDER_NAME
import com.zionhuang.music.databinding.ItemSongHeaderBinding
import com.zionhuang.music.models.base.IMutableSortInfo

class SongHeaderViewHolder(
    val binding: ItemSongHeaderBinding,
    private val sortInfo: IMutableSortInfo,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.sortMenu.setOnClickListener { view ->
            PopupMenu(view.context, view).apply {
                inflate(R.menu.sort_song)
                setOnMenuItemClickListener {
                    sortInfo.type = when (it.itemId) {
                        R.id.sort_by_create_date -> ORDER_CREATE_DATE
                        R.id.sort_by_name -> ORDER_NAME
                        R.id.sort_by_artist -> ORDER_ARTIST
                        else -> throw IllegalArgumentException("Unexpected sort type.")
                    }
                    updateSortName(sortInfo.type)
                    true
                }
                menu.findItem(when (sortInfo.type) {
                    ORDER_CREATE_DATE -> R.id.sort_by_create_date
                    ORDER_NAME -> R.id.sort_by_name
                    ORDER_ARTIST -> R.id.sort_by_artist
                    else -> throw IllegalArgumentException("Unexpected sort type.")
                })?.isChecked = true
                show()
            }
        }
        binding.sortMenu.setOnLongClickListener {
            sortInfo.toggleIsDescending()
            updateSortOrderIcon(sortInfo.isDescending)
            true
        }
        updateSortName(sortInfo.type)
        updateSortOrderIcon(sortInfo.isDescending, false)
    }

    fun bind(songsCount: Int) {
        binding.songsCount = songsCount
    }

    private fun updateSortName(sortType: Int) {
        binding.sortName.setText(when (sortType) {
            ORDER_CREATE_DATE -> R.string.sort_by_create_date
            ORDER_NAME -> R.string.sort_by_name
            ORDER_ARTIST -> R.string.sort_by_artist
            else -> throw IllegalArgumentException("Unexpected sort type.")
        })
    }

    private fun updateSortOrderIcon(sortDescending: Boolean, animate: Boolean = true) {
        if (sortDescending) {
            binding.sortOrderIcon.animateToDown(animate)
        } else {
            binding.sortOrderIcon.animateToUp(animate)
        }
    }
}