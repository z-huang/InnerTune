package com.zionhuang.music.ui.viewholders

import android.widget.PopupMenu
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants
import com.zionhuang.music.constants.ORDER_ARTIST
import com.zionhuang.music.constants.ORDER_CREATE_DATE
import com.zionhuang.music.constants.ORDER_NAME
import com.zionhuang.music.databinding.*
import com.zionhuang.music.db.entities.Album
import com.zionhuang.music.db.entities.Artist
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.context
import com.zionhuang.music.extensions.show
import com.zionhuang.music.models.DownloadProgress
import com.zionhuang.music.models.base.IMutableSortInfo
import com.zionhuang.music.ui.fragments.MenuBottomSheetDialogFragment
import com.zionhuang.music.ui.listeners.ArtistPopupMenuListener
import com.zionhuang.music.ui.listeners.PlaylistPopupMenuListener
import com.zionhuang.music.ui.listeners.SongPopupMenuListener

sealed class LocalItemViewHolder(open val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

open class SongViewHolder(
    override val binding: ItemSongBinding,
    private val popupMenuListener: SongPopupMenuListener?,
) : LocalItemViewHolder(binding) {
    val itemDetails: ItemDetailsLookup.ItemDetails<String>
        get() = object : ItemDetailsLookup.ItemDetails<String>() {
            override fun getPosition(): Int = absoluteAdapterPosition
            override fun getSelectionKey(): String? = binding.song?.song?.id
        }

    fun bind(song: Song, selected: Boolean? = false) {
        binding.song = song
        binding.artist.text = song.artists.joinToString { it.name }
        binding.btnMoreAction.setOnClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(R.menu.song)
                .setMenuModifier {
                    findItem(R.id.action_download).isVisible = song.song.downloadState == MediaConstants.STATE_NOT_DOWNLOADED
                    findItem(R.id.action_remove_download).isVisible = song.song.downloadState == MediaConstants.STATE_DOWNLOADED
                }
                .setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_edit -> popupMenuListener?.editSong(song, binding.context)
                        R.id.action_play_next -> popupMenuListener?.playNext(song, binding.context)
                        R.id.action_add_to_queue -> popupMenuListener?.addToQueue(song, binding.context)
                        R.id.action_add_to_playlist -> popupMenuListener?.addToPlaylist(song, binding.context)
                        R.id.action_download -> popupMenuListener?.downloadSong(song, binding.context)
                        R.id.action_remove_download -> popupMenuListener?.removeDownload(song, binding.context)
                        R.id.action_delete -> popupMenuListener?.deleteSongs(song)
                    }
                }
                .show(binding.context)
        }
        binding.isSelected = selected == true
        binding.executePendingBindings()
    }

    fun setProgress(progress: DownloadProgress, animate: Boolean = true) {
        binding.progressBar.run {
            max = progress.totalBytes
            setProgress(progress.currentBytes, animate)
        }
    }

    fun onSelectionChanged(selected: Boolean?) {
        binding.isSelected = selected == true
    }
}

class ArtistViewHolder(
    override val binding: ItemArtistBinding,
    private val popupMenuListener: ArtistPopupMenuListener?,
) : LocalItemViewHolder(binding) {
    fun bind(artist: Artist) {
        binding.artist = artist
        binding.btnMoreAction.setOnClickListener {
//            MenuBottomSheetDialogFragment
//                .newInstance(R.menu.artist)
//                .setOnMenuItemClickListener {
//                    when (it.itemId) {
//                        R.id.action_edit -> popupMenuListener?.editArtist(artist, binding.context)
//                        R.id.action_delete -> popupMenuListener?.deleteArtist(artist)
//                    }
//                }
//                .show(binding.context)
        }
    }
}

class AlbumViewHolder(
    override val binding: ItemAlbumBinding,
) : LocalItemViewHolder(binding) {
    fun bind(album: Album) {
        binding.album = album
        binding.subtitle.text = (if (album.artists.isNotEmpty()) album.artists.joinToString { it.name } + " • " else "") +
                binding.context.resources.getQuantityString(R.plurals.songs_count, album.songCount, album.songCount)
        binding.btnMoreAction.setOnClickListener {
//            MenuBottomSheetDialogFragment
//                .newInstance(R.menu.artist)
//                .setOnMenuItemClickListener {
//                    when (it.itemId) {
//                        R.id.action_edit -> popupMenuListener?.editArtist(artist, binding.context)
//                        R.id.action_delete -> popupMenuListener?.deleteArtist(artist)
//                    }
//                }
//                .show(binding.context)
        }
    }
}

class PlaylistViewHolder(
    override val binding: ItemPlaylistBinding,
    private val popupMenuListener: PlaylistPopupMenuListener?,
) : LocalItemViewHolder(binding) {
    fun bind(playlist: Playlist) {
        binding.playlist = playlist
        binding.btnMoreAction.setOnClickListener {
//            MenuBottomSheetDialogFragment
//                .newInstance(R.menu.artist)
//                .setOnMenuItemClickListener {
//                    when (it.itemId) {
//                        R.id.action_edit -> popupMenuListener?.editPlaylist(playlist, binding.context)
//                        R.id.action_delete -> popupMenuListener?.deletePlaylist(playlist)
//                    }
//                }
//                .show(binding.context)
        }
    }
}

class SongHeaderViewHolder(
    override val binding: ItemSongHeaderBinding,
    private val sortInfo: IMutableSortInfo,
) : LocalItemViewHolder(binding) {
    init {
        binding.sortMenu.setOnClickListener { view ->
            PopupMenu(view.context, view).apply {
                inflate(R.menu.sort_song)
                setOnMenuItemClickListener {
                    sortInfo.type = when (it.itemId) {
                        R.id.sort_by_create_date -> ORDER_CREATE_DATE
                        R.id.sort_by_name -> ORDER_NAME
                        R.id.sort_by_artist -> ORDER_ARTIST
                        else -> throw IllegalArgumentException("Unexpected sort type.")
                    }
                    updateSortName(sortInfo.type)
                    true
                }
                menu.findItem(when (sortInfo.type) {
                    ORDER_CREATE_DATE -> R.id.sort_by_create_date
                    ORDER_NAME -> R.id.sort_by_name
                    ORDER_ARTIST -> R.id.sort_by_artist
                    else -> throw IllegalArgumentException("Unexpected sort type.")
                })?.isChecked = true
                show()
            }
        }
        binding.sortMenu.setOnLongClickListener {
            sortInfo.toggleIsDescending()
            updateSortOrderIcon(sortInfo.isDescending)
            true
        }
        updateSortName(sortInfo.type)
        updateSortOrderIcon(sortInfo.isDescending, false)
    }

    fun bind(songsCount: Int) {
        binding.songsCount = songsCount
    }

    private fun updateSortName(sortType: Int) {
        binding.sortName.setText(when (sortType) {
            ORDER_CREATE_DATE -> R.string.sort_by_create_date
            ORDER_NAME -> R.string.sort_by_name
            ORDER_ARTIST -> R.string.sort_by_artist
            else -> throw IllegalArgumentException("Unexpected sort type.")
        })
    }

    private fun updateSortOrderIcon(sortDescending: Boolean, animate: Boolean = true) {
        if (sortDescending) {
            binding.sortOrderIcon.animateToDown(animate)
        } else {
            binding.sortOrderIcon.animateToUp(animate)
        }
    }
}