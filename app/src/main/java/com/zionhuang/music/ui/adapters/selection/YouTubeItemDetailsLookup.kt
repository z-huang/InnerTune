package com.zionhuang.music.ui.adapters.selection

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.ui.viewholders.YouTubeListItemViewHolder

class YouTubeItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<String>? = recyclerView.findChildViewUnder(e.x, e.y)?.let { v ->
        (recyclerView.getChildViewHolder(v) as? YouTubeListItemViewHolder)?.itemDetails
    }
}