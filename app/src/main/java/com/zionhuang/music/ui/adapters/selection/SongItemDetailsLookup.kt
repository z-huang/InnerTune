package com.zionhuang.music.ui.adapters.selection

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.ui.viewholders.SongViewHolder

class SongItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<String>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        return view?.let { v ->
            (recyclerView.getChildViewHolder(v) as SongViewHolder).itemDetails
        }
    }
}