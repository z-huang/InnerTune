package com.zionhuang.music.ui.viewholders

import android.widget.PopupMenu
import androidx.core.view.isVisible
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
import com.zionhuang.music.ui.listeners.IAlbumMenuListener
import com.zionhuang.music.ui.listeners.IArtistMenuListener
import com.zionhuang.music.ui.listeners.IPlaylistMenuListener
import com.zionhuang.music.ui.listeners.ISongMenuListener
import com.zionhuang.music.utils.joinByBullet
import com.zionhuang.music.utils.makeTimeString

sealed class LocalItemViewHolder(open val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

open class SongViewHolder(
    override val binding: ItemSongBinding,
    private val menuListener: ISongMenuListener?,
) : LocalItemViewHolder(binding) {
    val itemDetails: ItemDetailsLookup.ItemDetails<String>
        get() = object : ItemDetailsLookup.ItemDetails<String>() {
            override fun getPosition(): Int = absoluteAdapterPosition
            override fun getSelectionKey(): String? = binding.song?.song?.id
        }

    fun bind(song: Song, selected: Boolean? = false) {
        binding.song = song
        binding.subtitle.text = listOf(song.artists.joinToString { it.name }, song.song.albumName, makeTimeString(song.song.duration.toLong() * 1000)).joinByBullet()
        binding.btnMoreAction.setOnClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(R.menu.song)
                .setMenuModifier {
                    findItem(R.id.action_download).isVisible = song.song.downloadState == MediaConstants.STATE_NOT_DOWNLOADED
                    findItem(R.id.action_remove_download).isVisible = song.song.downloadState == MediaConstants.STATE_DOWNLOADED
                    findItem(R.id.action_view_album).isVisible = song.album != null
                    findItem(R.id.action_delete).isVisible = song.album == null
                }
                .setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_edit -> menuListener?.editSong(song)
                        R.id.action_play_next -> menuListener?.playNext(song)
                        R.id.action_add_to_queue -> menuListener?.addToQueue(song)
                        R.id.action_add_to_playlist -> menuListener?.addToPlaylist(song)
                        R.id.action_download -> menuListener?.download(song)
                        R.id.action_remove_download -> menuListener?.removeDownload(song)
                        R.id.action_view_artist -> menuListener?.viewArtist(song)
                        R.id.action_view_album -> menuListener?.viewAlbum(song)
                        R.id.action_share -> menuListener?.share(song)
                        R.id.action_delete -> menuListener?.delete(song)
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
    private val menuListener: IArtistMenuListener?,
) : LocalItemViewHolder(binding) {
    fun bind(artist: Artist) {
        binding.artist = artist
        binding.btnMoreAction.setOnClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(R.menu.artist)
                .setMenuModifier {
                    findItem(R.id.action_edit).isVisible = false // temporary
                    findItem(R.id.action_share).isVisible = artist.artist.isYouTubeArtist
                }
                .setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_edit -> menuListener?.edit(artist)
                        R.id.action_play_next -> menuListener?.playNext(artist)
                        R.id.action_add_to_queue -> menuListener?.addToQueue(artist)
                        R.id.action_add_to_playlist -> menuListener?.addToPlaylist(artist)
                        R.id.action_share -> menuListener?.edit(artist)
                        R.id.action_delete -> menuListener?.delete(artist)
                    }
                }
                .show(binding.context)
        }
    }
}

class AlbumViewHolder(
    override val binding: ItemAlbumBinding,
    private val menuListener: IAlbumMenuListener?,
) : LocalItemViewHolder(binding) {
    fun bind(album: Album) {
        binding.album = album
        binding.subtitle.text = listOf(album.artists.joinToString { it.name }, binding.context.resources.getQuantityString(R.plurals.songs_count, album.album.songCount, album.album.songCount), album.album.year?.toString()).joinByBullet()
        binding.btnMoreAction.setOnClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(R.menu.album)
                .setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_play_next -> menuListener?.playNext(album)
                        R.id.action_add_to_queue -> menuListener?.addToQueue(album)
                        R.id.action_add_to_playlist -> menuListener?.addToPlaylist(album)
                        R.id.action_view_artist -> menuListener?.viewArtist(album)
                        R.id.action_share -> menuListener?.share(album)
                        R.id.action_delete -> menuListener?.delete(album)
                    }
                }
                .show(binding.context)
        }
    }
}

class PlaylistViewHolder(
    override val binding: ItemPlaylistBinding,
    private val menuListener: IPlaylistMenuListener?,
    private val allowMoreAction: Boolean,
) : LocalItemViewHolder(binding) {
    fun bind(playlist: Playlist) {
        binding.playlist = playlist
        binding.subtitle.text = if (playlist.playlist.isYouTubePlaylist) {
            listOf(playlist.playlist.name, playlist.playlist.year.toString()).joinByBullet()
        } else {
            binding.context.resources.getQuantityString(R.plurals.songs_count, playlist.songCount, playlist.songCount)
        }
        binding.btnMoreAction.isVisible = allowMoreAction
        binding.btnMoreAction.setOnClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(R.menu.playlist)
                .setMenuModifier {
                    findItem(R.id.action_share).isVisible = playlist.playlist.isYouTubePlaylist
                }
                .setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_edit -> menuListener?.edit(playlist)
                        R.id.action_play -> menuListener?.play(playlist)
                        R.id.action_play_next -> menuListener?.playNext(playlist)
                        R.id.action_add_to_queue -> menuListener?.addToQueue(playlist)
                        R.id.action_add_to_playlist -> menuListener?.addToPlaylist(playlist)
                        R.id.action_share -> menuListener?.share(playlist)
                        R.id.action_delete -> menuListener?.delete(playlist)
                    }
                }
                .show(binding.context)
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