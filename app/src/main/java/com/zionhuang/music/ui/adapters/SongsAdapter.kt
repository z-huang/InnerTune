package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemSongBinding
import com.zionhuang.music.db.SongEntity
import com.zionhuang.music.extensions.inflateWithBinding

class SongsAdapter : RecyclerView.Adapter<SongsAdapter.ViewHolder>() {
    private var mDataSet: List<SongEntity> = emptyList()
    fun setDataSet(list: List<SongEntity>) {
        mDataSet = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflateWithBinding(R.layout.item_song))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int = mDataSet.size

    fun getSongFromPosition(i: Int): SongEntity = mDataSet[i]

    inner class ViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: SongEntity?) {
            binding.song = song
            binding.executePendingBindings()
        }
    }
}