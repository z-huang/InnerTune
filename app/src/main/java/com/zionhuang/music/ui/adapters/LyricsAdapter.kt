package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.viewholders.LyricsItemViewHolder
import com.zionhuang.music.utils.lyrics.LyricsResult

class LyricsAdapter : RecyclerView.Adapter<LyricsItemViewHolder>() {
    var items = emptyList<LyricsResult>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LyricsItemViewHolder =
        LyricsItemViewHolder(parent.inflateWithBinding(R.layout.item_lyrics))


    override fun onBindViewHolder(holder: LyricsItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}