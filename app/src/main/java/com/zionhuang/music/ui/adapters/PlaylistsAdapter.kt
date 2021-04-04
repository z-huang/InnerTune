package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.viewholders.PlaylistViewHolder

class PlaylistsAdapter : PagingDataAdapter<PlaylistEntity, PlaylistViewHolder>(PlaylistItemComparator()) {
    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder =
            PlaylistViewHolder(parent.inflateWithBinding(R.layout.item_playlist))

    fun getItemByPosition(position: Int): PlaylistEntity? = getItem(position)

    class PlaylistItemComparator : DiffUtil.ItemCallback<PlaylistEntity>() {
        override fun areItemsTheSame(oldItem: PlaylistEntity, newItem: PlaylistEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PlaylistEntity, newItem: PlaylistEntity): Boolean = oldItem.name == newItem.name
    }
}