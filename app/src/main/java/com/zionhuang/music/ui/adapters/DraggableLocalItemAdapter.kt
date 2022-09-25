package com.zionhuang.music.ui.adapters

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.SelectionTracker.SELECTION_CHANGED_MARKER
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.listeners.*
import com.zionhuang.music.ui.viewholders.*

class DraggableLocalItemAdapter : RecyclerView.Adapter<LocalItemViewHolder>() {
    var currentList: List<LocalBaseItem> = emptyList()

    var songMenuListener: ISongMenuListener? = null
    var artistMenuListener: IArtistMenuListener? = null
    var albumMenuListener: IAlbumMenuListener? = null
    var playlistMenuListener: IPlaylistMenuListener? = null
    var likedPlaylistMenuListener: LikedPlaylistMenuListener? = null
    var downloadedPlaylistMenuListener: DownloadedPlaylistMenuListener? = null

    var tracker: SelectionTracker<String>? = null
    var allowMoreAction: Boolean = true // for choosing playlist
    var onShuffle: () -> Unit = {}

    var itemTouchHelper: ItemTouchHelper? = null
    var isDraggable: Boolean = false // for reorder playlist

    override fun onBindViewHolder(holder: LocalItemViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is SongViewHolder -> holder.bind(item as Song, tracker?.isSelected(getItem(position).id) ?: false)
            is ArtistViewHolder -> holder.bind(item as Artist, tracker?.isSelected(getItem(position).id) ?: false)
            is AlbumViewHolder -> holder.bind(item as Album, tracker?.isSelected(getItem(position).id) ?: false)
            is PlaylistViewHolder -> holder.bind(item as Playlist, tracker?.isSelected(getItem(position).id) ?: false)
            is CustomPlaylistViewHolder -> when (item) {
                is LikedPlaylist -> holder.bind(item, likedPlaylistMenuListener)
                is DownloadedPlaylist -> holder.bind(item, downloadedPlaylistMenuListener)
                else -> {}
            }
            is SongHeaderViewHolder -> holder.bind(item as SongHeader)
            is ArtistHeaderViewHolder -> holder.bind(item as ArtistHeader)
            is AlbumHeaderViewHolder -> holder.bind(item as AlbumHeader)
            is PlaylistHeaderViewHolder -> holder.bind(item as PlaylistHeader)
            is PlaylistSongHeaderViewHolder -> holder.bind(item as PlaylistSongHeader)
            is TextHeaderViewHolder -> holder.bind(item as TextHeader)
        }
    }

    override fun onBindViewHolder(holder: LocalItemViewHolder, position: Int, payloads: MutableList<Any>) {
        val payload = payloads.firstOrNull()
        when {
            payload is SongHeader && holder is SongHeaderViewHolder -> holder.bind(payload, true)
            payload is ArtistHeader && holder is ArtistHeaderViewHolder -> holder.bind(payload, true)
            payload is AlbumHeader && holder is AlbumHeaderViewHolder -> holder.bind(payload, true)
            payload is PlaylistHeader && holder is PlaylistHeaderViewHolder -> holder.bind(payload, true)
            payload is PlaylistSongHeader && holder is PlaylistSongHeaderViewHolder -> holder.bind(payload)
            payload is TextHeader && holder is TextHeaderViewHolder -> holder.bind(payload)
            payload == SELECTION_CHANGED_MARKER -> holder.onSelectionChanged(tracker?.isSelected(getItem(position).id) ?: false)
            else -> onBindViewHolder(holder, position)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalItemViewHolder = when (viewType) {
        TYPE_SONG -> SongViewHolder(parent.inflateWithBinding(R.layout.item_song), songMenuListener, isDraggable).apply {
            binding.dragHandle.setOnTouchListener { _, event ->
                if (tracker?.hasSelection() == false && event.actionMasked == MotionEvent.ACTION_DOWN) itemTouchHelper?.startDrag(this)
                true
            }
        }
        TYPE_ARTIST -> ArtistViewHolder(parent.inflateWithBinding(R.layout.item_artist), artistMenuListener)
        TYPE_ALBUM -> AlbumViewHolder(parent.inflateWithBinding(R.layout.item_album), albumMenuListener)
        TYPE_PLAYLIST -> PlaylistViewHolder(parent.inflateWithBinding(R.layout.item_playlist), playlistMenuListener, allowMoreAction)
        TYPE_LIKED_PLAYLIST, TYPE_DOWNLOADED_PLAYLIST -> CustomPlaylistViewHolder(parent.inflateWithBinding(R.layout.item_custom_playlist))
        TYPE_SONG_HEADER -> SongHeaderViewHolder(parent.inflateWithBinding(R.layout.item_header), onShuffle)
        TYPE_ARTIST_HEADER -> ArtistHeaderViewHolder(parent.inflateWithBinding(R.layout.item_header))
        TYPE_ALBUM_HEADER -> AlbumHeaderViewHolder(parent.inflateWithBinding(R.layout.item_header))
        TYPE_PLAYLIST_HEADER -> PlaylistHeaderViewHolder(parent.inflateWithBinding(R.layout.item_header))
        TYPE_PLAYLIST_SONG_HEADER -> PlaylistSongHeaderViewHolder(parent.inflateWithBinding(R.layout.item_playlist_header), onShuffle)
        TYPE_TEXT_HEADER -> TextHeaderViewHolder(parent.inflateWithBinding(R.layout.item_text_header))
        else -> error("Unknown view type")
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is Song -> TYPE_SONG
        is Artist -> TYPE_ARTIST
        is Album -> TYPE_ALBUM
        is Playlist -> TYPE_PLAYLIST
        is LikedPlaylist -> TYPE_LIKED_PLAYLIST
        is DownloadedPlaylist -> TYPE_DOWNLOADED_PLAYLIST
        is SongHeader -> TYPE_SONG_HEADER
        is ArtistHeader -> TYPE_ARTIST_HEADER
        is AlbumHeader -> TYPE_ALBUM_HEADER
        is PlaylistHeader -> TYPE_PLAYLIST_HEADER
        is PlaylistSongHeader -> TYPE_PLAYLIST_SONG_HEADER
        is TextHeader -> TYPE_TEXT_HEADER
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<LocalBaseItem>, animation: Boolean = true) {
        val oldList = currentList
        currentList = newList
        if (animation) {
            DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = oldList.size
                override fun getNewListSize(): Int = newList.size
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = itemComparator.areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])
                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = itemComparator.areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])
                override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int) = itemComparator.getChangePayload(oldList[oldItemPosition], newList[newItemPosition])
            }).dispatchUpdatesTo(this)
        } else {
            notifyDataSetChanged()
        }
    }

    private fun getItem(position: Int): LocalBaseItem = currentList[position]

    override fun getItemCount(): Int = currentList.size

    val itemComparator = object : DiffUtil.ItemCallback<LocalBaseItem>() {
        override fun areItemsTheSame(oldItem: LocalBaseItem, newItem: LocalBaseItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: LocalBaseItem, newItem: LocalBaseItem): Boolean = oldItem == newItem
        override fun getChangePayload(oldItem: LocalBaseItem, newItem: LocalBaseItem) = newItem
    }

    companion object {
        const val TYPE_SONG = 0
        const val TYPE_ARTIST = 1
        const val TYPE_ALBUM = 2
        const val TYPE_PLAYLIST = 3
        const val TYPE_LIKED_PLAYLIST = 4
        const val TYPE_DOWNLOADED_PLAYLIST = 5
        const val TYPE_SONG_HEADER = 6
        const val TYPE_ARTIST_HEADER = 7
        const val TYPE_ALBUM_HEADER = 8
        const val TYPE_PLAYLIST_HEADER = 9
        const val TYPE_PLAYLIST_SONG_HEADER = 10
        const val TYPE_TEXT_HEADER = 11
    }
}