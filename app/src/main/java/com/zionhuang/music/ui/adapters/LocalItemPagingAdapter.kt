package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.listeners.IAlbumMenuListener
import com.zionhuang.music.ui.listeners.IArtistMenuListener
import com.zionhuang.music.ui.listeners.IPlaylistMenuListener
import com.zionhuang.music.ui.listeners.ISongMenuListener
import com.zionhuang.music.ui.viewholders.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.PopupTextProvider
import java.time.Duration
import java.time.LocalDateTime

class LocalItemPagingAdapter : PagingDataAdapter<LocalBaseItem, LocalItemViewHolder>(ItemComparator()), PopupTextProvider {
    var songMenuListener: ISongMenuListener? = null
    var artistMenuListener: IArtistMenuListener? = null
    var albumMenuListener: IAlbumMenuListener? = null
    var playlistMenuListener: IPlaylistMenuListener? = null

    var tracker: SelectionTracker<String>? = null
    var allowMoreAction: Boolean = true

    @OptIn(DelicateCoroutinesApi::class)
    override fun onBindViewHolder(holder: LocalItemViewHolder, position: Int) {
        val item = getItem(position) ?: return
        when (holder) {
            is SongViewHolder -> holder.bind(item as Song)
            is AlbumViewHolder -> {
                holder.bind(item as Album)
                if (item.album.thumbnailUrl == null || item.album.year == null) {
                    GlobalScope.launch {
                        SongRepository.refetchAlbum(item.album)
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
            is SongHeaderViewHolder -> holder.bind(item as SongHeader)
            is ArtistHeaderViewHolder -> holder.bind(item as ArtistHeader)
        }
    }

    override fun onBindViewHolder(holder: LocalItemViewHolder, position: Int, payloads: MutableList<Any>) {
        val payload = payloads.firstOrNull()
        when {
            payload is SongHeader && holder is SongHeaderViewHolder -> holder.bind(payload, true)
            payload is ArtistHeader && holder is ArtistHeaderViewHolder -> holder.bind(payload, true)
            else -> onBindViewHolder(holder, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalItemViewHolder = when (viewType) {
        TYPE_SONG -> SongViewHolder(parent.inflateWithBinding(R.layout.item_song), songMenuListener)
        TYPE_ARTIST -> ArtistViewHolder(parent.inflateWithBinding(R.layout.item_artist), artistMenuListener)
        TYPE_ALBUM -> AlbumViewHolder(parent.inflateWithBinding(R.layout.item_album), albumMenuListener)
        TYPE_PLAYLIST -> PlaylistViewHolder(parent.inflateWithBinding(R.layout.item_playlist), playlistMenuListener, allowMoreAction)
        TYPE_SONG_HEADER -> SongHeaderViewHolder(parent.inflateWithBinding(R.layout.item_song_header))
        TYPE_ARTIST_HEADER -> ArtistHeaderViewHolder(parent.inflateWithBinding(R.layout.item_artist_header))
        else -> error("Unknown view type")
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)!!) {
        is Song -> TYPE_SONG
        is Artist -> TYPE_ARTIST
        is Album -> TYPE_ALBUM
        is Playlist -> TYPE_PLAYLIST
        is SongHeader -> TYPE_SONG_HEADER
        is ArtistHeader -> TYPE_ARTIST_HEADER
    }

    fun getItemAt(position: Int) = getItem(position)

    override fun getPopupText(position: Int): String = ""

    class ItemComparator : DiffUtil.ItemCallback<LocalBaseItem>() {
        override fun areItemsTheSame(oldItem: LocalBaseItem, newItem: LocalBaseItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: LocalBaseItem, newItem: LocalBaseItem): Boolean = oldItem == newItem
    }

    companion object {
        const val TYPE_SONG = 0
        const val TYPE_ARTIST = 1
        const val TYPE_ALBUM = 2
        const val TYPE_PLAYLIST = 3
        const val TYPE_SONG_HEADER = 4
        const val TYPE_ARTIST_HEADER = 5
    }
}