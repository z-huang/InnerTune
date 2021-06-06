package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.listeners.PlaylistPopupMenuListener
import com.zionhuang.music.ui.viewholders.PlaylistViewHolder

class PlaylistsAdapter : PagingDataAdapter<PlaylistEntity, PlaylistViewHolder>(PlaylistItemComparator()) {
    var popupMenuListener: PlaylistPopupMenuListener? = null
    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder =
        PlaylistViewHolder(parent.inflateWithBinding(R.layout.item_playlist), popupMenuListener)

    fun getItemByPosition(position: Int): PlaylistEntity? = getItem(position)

    class PlaylistItemComparator : DiffUtil.ItemCallback<PlaylistEntity>() {
        override fun areItemsTheSame(oldItem: PlaylistEntity, newItem: PlaylistEntity): Boolean = oldItem.playlistId == newItem.playlistId
        override fun areContentsTheSame(oldItem: PlaylistEntity, newItem: PlaylistEntity): Boolean = oldItem.name == newItem.name
    }
}