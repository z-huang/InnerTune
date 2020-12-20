package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemArtistBinding
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.extensions.inflateWithBinding

class ArtistsAdapter : PagingDataAdapter<ArtistEntity, ArtistsAdapter.ArtistViewHolder>(ItemComparator()) {
    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder =
            ArtistViewHolder(parent.inflateWithBinding(R.layout.item_artist))

    inner class ArtistViewHolder(val binding: ItemArtistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(artist: ArtistEntity) {
            binding.artist = artist
        }
    }

    fun getItemByPosition(position: Int): ArtistEntity? = getItem(position)

    internal class ItemComparator : DiffUtil.ItemCallback<ArtistEntity>() {
        override fun areItemsTheSame(oldItem: ArtistEntity, newItem: ArtistEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ArtistEntity, newItem: ArtistEntity): Boolean = oldItem.name == newItem.name
        override fun getChangePayload(oldItem: ArtistEntity, newItem: ArtistEntity): ArtistEntity = newItem
    }
}