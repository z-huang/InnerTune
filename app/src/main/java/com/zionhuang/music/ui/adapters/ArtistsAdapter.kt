package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.listeners.ArtistPopupMenuListener
import com.zionhuang.music.ui.viewholders.ArtistViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

class ArtistsAdapter(private val scope: CoroutineScope) : PagingDataAdapter<ArtistEntity, ArtistViewHolder>(ArtistItemComparator()) {
    var popupMenuListener: ArtistPopupMenuListener? = null
    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        getItem(position)?.let { artist ->
            holder.bind(artist)
            if (artist.isYouTubeArtist) {
                if (artist.bannerUrl == null || Duration.between(artist.lastUpdateTime, LocalDateTime.now()) > Duration.ofDays(10)) {
                    scope.launch {
                        SongRepository.refetchArtist(artist)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder =
        ArtistViewHolder(parent.inflateWithBinding(R.layout.item_artist), popupMenuListener)

    fun getItemByPosition(position: Int): ArtistEntity? = getItem(position)

    internal class ArtistItemComparator : DiffUtil.ItemCallback<ArtistEntity>() {
        override fun areItemsTheSame(oldItem: ArtistEntity, newItem: ArtistEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ArtistEntity, newItem: ArtistEntity): Boolean = oldItem == newItem
    }
}