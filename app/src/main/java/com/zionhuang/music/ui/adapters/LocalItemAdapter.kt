package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.models.base.IMutableSortInfo
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.listeners.SongPopupMenuListener
import com.zionhuang.music.ui.viewholders.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.PopupTextProvider
import java.text.DateFormat
import java.time.Duration
import java.time.LocalDateTime

class LocalItemAdapter : PagingDataAdapter<LocalItem, LocalItemViewHolder>(ItemComparator()), PopupTextProvider {
    var popupMenuListener: SongPopupMenuListener? = null
    var sortInfo: IMutableSortInfo? = null
    var tracker: SelectionTracker<String>? = null
    var allowMoreAction: Boolean = true

    @OptIn(DelicateCoroutinesApi::class)
    override fun onBindViewHolder(holder: LocalItemViewHolder, position: Int) {
        getItem(position)?.let { item ->
            when (holder) {
                is SongViewHolder -> holder.bind(item as Song)
                is AlbumViewHolder -> {
                    holder.bind(item as Album)
                    if (item.album.thumbnailUrl == null) {
                        GlobalScope.launch {
                            SongRepository.fetchAlbumThumbnail(item.album)
                        }
                    }
                }
                is ArtistViewHolder -> {
                    holder.bind(item as Artist)
                    if (item.artist.bannerUrl == null || Duration.between(item.artist.lastUpdateTime, LocalDateTime.now()) > Duration.ofDays(10)) {
                        GlobalScope.launch {
                            SongRepository.refetchArtist(item.artist)
                        }
                    }
                }
                is PlaylistViewHolder -> holder.bind(item as Playlist)
                else -> {}
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalItemViewHolder = when (viewType) {
        TYPE_SONG -> SongViewHolder(parent.inflateWithBinding(R.layout.item_song), popupMenuListener)
        TYPE_ARTIST -> ArtistViewHolder(parent.inflateWithBinding(R.layout.item_artist), null)
        TYPE_ALBUM -> AlbumViewHolder(parent.inflateWithBinding(R.layout.item_album))
        TYPE_PLAYLIST -> PlaylistViewHolder(parent.inflateWithBinding(R.layout.item_playlist), null, allowMoreAction)
        else -> error("Unknown view type")
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)!!) {
        is Song -> TYPE_SONG
        is Artist -> TYPE_ARTIST
        is Album -> TYPE_ALBUM
        is Playlist -> TYPE_PLAYLIST
    }

    fun getItemAt(position: Int) = getItem(position)

    private val dateFormat = DateFormat.getDateInstance()

    override fun getPopupText(position: Int): String = ""

    class ItemComparator : DiffUtil.ItemCallback<LocalItem>() {
        override fun areItemsTheSame(oldItem: LocalItem, newItem: LocalItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: LocalItem, newItem: LocalItem): Boolean = oldItem == newItem
    }

    companion object {
        const val TYPE_SONG = 0
        const val TYPE_ARTIST = 1
        const val TYPE_ALBUM = 2
        const val TYPE_PLAYLIST = 3
    }
}