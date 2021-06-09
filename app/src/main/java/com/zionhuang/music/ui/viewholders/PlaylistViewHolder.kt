package com.zionhuang.music.ui.viewholders

import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemPlaylistBinding
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.extensions.context
import com.zionhuang.music.ui.listeners.PlaylistPopupMenuListener

class PlaylistViewHolder(
    val binding: ItemPlaylistBinding,
    private val popupMenuListener: PlaylistPopupMenuListener?,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(playlist: PlaylistEntity) {
        binding.playlist = playlist
        binding.btnMoreAction.setOnClickListener { view ->
            PopupMenu(view.context, view).apply {
                inflate(R.menu.artist)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_edit -> popupMenuListener?.editPlaylist(playlist, binding.context)
                        R.id.action_delete -> popupMenuListener?.deletePlaylist(playlist)
                    }
                    true
                }
                show()
            }
        }
    }
}