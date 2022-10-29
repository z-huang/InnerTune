package com.zionhuang.music.ui.viewholders

import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants
import com.zionhuang.music.databinding.*
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.extensions.*
import com.zionhuang.music.models.sortInfo.*
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.fragments.MenuBottomSheetDialogFragment
import com.zionhuang.music.ui.listeners.*
import com.zionhuang.music.utils.joinByBullet
import com.zionhuang.music.utils.makeTimeString
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

sealed class LocalItemViewHolder(open val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    abstract val itemDetails: ItemDetailsLookup.ItemDetails<String>?
    open fun onSelectionChanged(isSelected: Boolean) {}
}

open class SongViewHolder(
    override val binding: ItemSongBinding,
    private val menuListener: ISongMenuListener?,
    private val draggable: Boolean = false,
) : LocalItemViewHolder(binding) {
    override val itemDetails: ItemDetailsLookup.ItemDetails<String>
        get() = object : ItemDetailsLookup.ItemDetails<String>() {
            override fun getPosition(): Int = absoluteAdapterPosition
            override fun getSelectionKey(): String? = binding.song?.id
        }

    fun bind(song: Song, isSelected: Boolean = false) {
        binding.song = song
        binding.subtitle.text = listOf(song.artists.joinToString { it.name }, song.song.albumName, makeTimeString(song.song.duration.toLong() * 1000)).joinByBullet()
        binding.btnMoreAction.setOnClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(R.menu.song)
                .setMenuModifier {
                    findItem(R.id.action_favorite).apply {
                        setIcon(if (song.song.liked) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
                        setTitle(if (song.song.liked) R.string.action_remove_like else R.string.action_like)
                    }
                    findItem(R.id.action_download).isVisible = song.song.downloadState == MediaConstants.STATE_NOT_DOWNLOADED
                    findItem(R.id.action_remove_download).isVisible = song.song.downloadState == MediaConstants.STATE_DOWNLOADED
                    findItem(R.id.action_view_artist).isVisible = song.artists[0].isYouTubeArtist
                    findItem(R.id.action_view_album).isVisible = song.song.albumId != null
                    findItem(R.id.action_delete).isVisible = song.album == null
                }
                .setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_edit -> menuListener?.editSong(song)
                        R.id.action_favorite -> menuListener?.toggleLike(song)
                        R.id.action_radio -> menuListener?.startRadio(song)
                        R.id.action_play_next -> menuListener?.playNext(song)
                        R.id.action_add_to_queue -> menuListener?.addToQueue(song)
                        R.id.action_add_to_playlist -> menuListener?.addToPlaylist(song)
                        R.id.action_download -> menuListener?.download(song)
                        R.id.action_remove_download -> menuListener?.removeDownload(song)
                        R.id.action_view_artist -> menuListener?.viewArtist(song)
                        R.id.action_view_album -> menuListener?.viewAlbum(song)
                        R.id.action_refetch -> menuListener?.refetch(song)
                        R.id.action_share -> menuListener?.share(song)
                        R.id.action_delete -> menuListener?.delete(song)
                    }
                }
                .show(binding.context)
        }
        binding.selectedIndicator.isVisible = isSelected
        binding.dragHandle.isVisible = draggable
        binding.executePendingBindings()
    }

    override fun onSelectionChanged(isSelected: Boolean) {
        binding.selectedIndicator.isVisible = isSelected
    }
}

class ArtistViewHolder(
    override val binding: ItemArtistBinding,
    private val menuListener: IArtistMenuListener?,
) : LocalItemViewHolder(binding) {
    override val itemDetails: ItemDetailsLookup.ItemDetails<String>
        get() = object : ItemDetailsLookup.ItemDetails<String>() {
            override fun getPosition(): Int = absoluteAdapterPosition
            override fun getSelectionKey(): String? = binding.artist?.id
        }

    @OptIn(DelicateCoroutinesApi::class)
    fun bind(artist: Artist, isSelected: Boolean = false) {
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
                        R.id.action_refetch -> menuListener?.refetch(artist)
                        R.id.action_share -> menuListener?.edit(artist)
                    }
                }
                .show(binding.context)
        }
        binding.selectedIndicator.isVisible = isSelected
        if (artist.artist.bannerUrl == null || Duration.between(artist.artist.lastUpdateTime, LocalDateTime.now()) > Duration.ofDays(10)) {
            GlobalScope.launch(binding.context.exceptionHandler) {
                SongRepository(binding.context).refetchArtist(artist.artist)
            }
        }
    }

    override fun onSelectionChanged(isSelected: Boolean) {
        binding.selectedIndicator.isVisible = isSelected
    }
}

class AlbumViewHolder(
    override val binding: ItemAlbumBinding,
    private val menuListener: IAlbumMenuListener?,
) : LocalItemViewHolder(binding) {
    override val itemDetails: ItemDetailsLookup.ItemDetails<String>
        get() = object : ItemDetailsLookup.ItemDetails<String>() {
            override fun getPosition(): Int = absoluteAdapterPosition
            override fun getSelectionKey(): String? = binding.album?.id
        }

    @OptIn(DelicateCoroutinesApi::class)
    fun bind(album: Album, isSelected: Boolean = false) {
        binding.album = album
        binding.subtitle.text = listOf(album.artists.joinToString { it.name }, binding.context.resources.getQuantityString(R.plurals.song_count, album.album.songCount, album.album.songCount), album.album.year?.toString()).joinByBullet()
        binding.btnMoreAction.setOnClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(R.menu.album)
                .setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_play_next -> menuListener?.playNext(album)
                        R.id.action_add_to_queue -> menuListener?.addToQueue(album)
                        R.id.action_add_to_playlist -> menuListener?.addToPlaylist(album)
                        R.id.action_view_artist -> menuListener?.viewArtist(album)
                        R.id.action_refetch -> menuListener?.refetch(album)
                        R.id.action_share -> menuListener?.share(album)
                        R.id.action_delete -> menuListener?.delete(album)
                    }
                }
                .show(binding.context)
        }
        binding.selectedIndicator.isVisible = isSelected
        if (album.album.thumbnailUrl == null || album.album.year == null) {
            GlobalScope.launch(binding.context.exceptionHandler) {
                SongRepository(binding.context).refetchAlbum(album.album)
            }
        }
    }

    override fun onSelectionChanged(isSelected: Boolean) {
        binding.selectedIndicator.isVisible = isSelected
    }
}

class PlaylistViewHolder(
    override val binding: ItemPlaylistBinding,
    private val menuListener: IPlaylistMenuListener?,
    private val allowMoreAction: Boolean,
) : LocalItemViewHolder(binding) {
    override val itemDetails: ItemDetailsLookup.ItemDetails<String>
        get() = object : ItemDetailsLookup.ItemDetails<String>() {
            override fun getPosition(): Int = absoluteAdapterPosition
            override fun getSelectionKey(): String? = binding.playlist?.id
        }

    fun bind(playlist: Playlist, isSelected: Boolean = false) {
        binding.playlist = playlist
        binding.subtitle.text = if (playlist.playlist.isYouTubePlaylist) {
            listOf(playlist.playlist.name, playlist.playlist.year.toString()).joinByBullet()
        } else {
            binding.context.resources.getQuantityString(R.plurals.song_count, playlist.songCount, playlist.songCount)
        }
        binding.btnMoreAction.isVisible = allowMoreAction
        binding.btnMoreAction.setOnClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(R.menu.playlist)
                .setMenuModifier {
                    findItem(R.id.action_edit).isVisible = playlist.playlist.isLocalPlaylist
                    findItem(R.id.action_download).isVisible = playlist.playlist.isLocalPlaylist
                    findItem(R.id.action_refetch).isVisible = playlist.playlist.isYouTubePlaylist
                    findItem(R.id.action_share).isVisible = playlist.playlist.isYouTubePlaylist
                }
                .setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_edit -> menuListener?.edit(playlist)
                        R.id.action_play -> menuListener?.play(playlist)
                        R.id.action_play_next -> menuListener?.playNext(playlist)
                        R.id.action_add_to_queue -> menuListener?.addToQueue(playlist)
                        R.id.action_add_to_playlist -> menuListener?.addToPlaylist(playlist)
                        R.id.action_download -> menuListener?.download(playlist)
                        R.id.action_refetch -> menuListener?.refetch(playlist)
                        R.id.action_share -> menuListener?.share(playlist)
                        R.id.action_delete -> menuListener?.delete(playlist)
                    }
                }
                .show(binding.context)
        }
        binding.selectedIndicator.isVisible = isSelected
    }

    override fun onSelectionChanged(isSelected: Boolean) {
        binding.selectedIndicator.isVisible = isSelected
    }
}

class CustomPlaylistViewHolder(
    override val binding: ItemCustomPlaylistBinding,
) : LocalItemViewHolder(binding) {
    override val itemDetails: ItemDetailsLookup.ItemDetails<String>? = null

    fun bind(playlist: LikedPlaylist, menuListener: LikedPlaylistMenuListener?) {
        binding.title.setText(R.string.liked_songs)
        binding.subtitle.text = binding.context.resources.getQuantityString(R.plurals.song_count, playlist.songCount, playlist.songCount)
        binding.thumbnail.setImageResource(R.drawable.ic_favorite)
        binding.offlineIcon.isVisible = false
        binding.btnMoreAction.setOnClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(R.menu.playlist)
                .setMenuModifier {
                    findItem(R.id.action_edit).isVisible = false
                    findItem(R.id.action_refetch).isVisible = false
                    findItem(R.id.action_share).isVisible = false
                    findItem(R.id.action_delete).isVisible = false
                }
                .setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_play -> menuListener?.play()
                        R.id.action_play_next -> menuListener?.playNext()
                        R.id.action_add_to_queue -> menuListener?.addToQueue()
                        R.id.action_add_to_playlist -> menuListener?.addToPlaylist()
                        R.id.action_download -> menuListener?.download()
                    }
                }
                .show(binding.context)
        }
    }

    fun bind(playlist: DownloadedPlaylist, menuListener: DownloadedPlaylistMenuListener?) {
        binding.title.setText(R.string.downloaded_songs)
        binding.subtitle.text = binding.context.resources.getQuantityString(R.plurals.song_count, playlist.songCount, playlist.songCount)
        binding.thumbnail.setImageResource(R.drawable.ic_save_alt)
        binding.offlineIcon.isVisible = true
        binding.btnMoreAction.setOnClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(R.menu.playlist)
                .setMenuModifier {
                    findItem(R.id.action_edit).isVisible = false
                    findItem(R.id.action_download).isVisible = false
                    findItem(R.id.action_refetch).isVisible = false
                    findItem(R.id.action_share).isVisible = false
                    findItem(R.id.action_delete).isVisible = false
                }
                .setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_play -> menuListener?.play()
                        R.id.action_play_next -> menuListener?.playNext()
                        R.id.action_add_to_queue -> menuListener?.addToQueue()
                        R.id.action_add_to_playlist -> menuListener?.addToPlaylist()
                    }
                }
                .show(binding.context)
        }
    }

    override fun onSelectionChanged(isSelected: Boolean) {
        binding.selectedIndicator.isVisible = isSelected
    }
}

class SongHeaderViewHolder(
    override val binding: ItemHeaderBinding,
    private val onShuffle: () -> Unit = {},
) : LocalItemViewHolder(binding) {
    override val itemDetails: ItemDetailsLookup.ItemDetails<String>? = null

    fun bind(header: SongHeader, isPayload: Boolean = false) {
        binding.sortName.setOnClickListener { view ->
            PopupMenu(view.context, view).apply {
                inflate(R.menu.sort_song)
                setOnMenuItemClickListener {
                    SongSortInfoPreference.type = when (it.itemId) {
                        R.id.sort_by_create_date -> SongSortType.CREATE_DATE
                        R.id.sort_by_name -> SongSortType.NAME
                        R.id.sort_by_artist -> SongSortType.ARTIST
                        R.id.sort_by_play_time -> SongSortType.PLAY_TIME
                        else -> throw IllegalArgumentException("Unexpected sort type.")
                    }
                    true
                }
                menu.findItem(when (header.sortInfo.type) {
                    SongSortType.CREATE_DATE -> R.id.sort_by_create_date
                    SongSortType.NAME -> R.id.sort_by_name
                    SongSortType.ARTIST -> R.id.sort_by_artist
                    SongSortType.PLAY_TIME -> R.id.sort_by_play_time
                })?.isChecked = true
                show()
            }
        }
        binding.sortName.setText(when (header.sortInfo.type) {
            SongSortType.CREATE_DATE -> R.string.sort_by_create_date
            SongSortType.NAME -> R.string.sort_by_name
            SongSortType.ARTIST -> R.string.sort_by_artist
            SongSortType.PLAY_TIME -> R.string.sort_by_play_time
        })
        binding.sortOrder.setOnClickListener {
            SongSortInfoPreference.toggleIsDescending()
        }
        updateSortOrderIcon(header.sortInfo.isDescending, isPayload)
        binding.btnShuffle.isVisible = true
        binding.btnShuffle.setOnClickListener {
            onShuffle()
        }
        binding.countText.text = binding.context.resources.getQuantityString(R.plurals.song_count, header.songCount, header.songCount)
    }

    private fun updateSortOrderIcon(sortDescending: Boolean, animate: Boolean = true) {
        if (sortDescending) {
            binding.sortOrder.animateToDown(animate)
        } else {
            binding.sortOrder.animateToUp(animate)
        }
    }
}

class ArtistHeaderViewHolder(
    override val binding: ItemHeaderBinding,
) : LocalItemViewHolder(binding) {
    override val itemDetails: ItemDetailsLookup.ItemDetails<String>? = null

    fun bind(header: ArtistHeader, isPayload: Boolean = false) {
        binding.sortName.setOnClickListener { view ->
            PopupMenu(view.context, view).apply {
                inflate(R.menu.sort_artist)
                setOnMenuItemClickListener {
                    ArtistSortInfoPreference.type = when (it.itemId) {
                        R.id.sort_by_create_date -> ArtistSortType.CREATE_DATE
                        R.id.sort_by_name -> ArtistSortType.NAME
                        R.id.sort_by_song_count -> ArtistSortType.SONG_COUNT
                        else -> throw IllegalArgumentException("Unexpected sort type.")
                    }
                    true
                }
                menu.findItem(when (header.sortInfo.type) {
                    ArtistSortType.CREATE_DATE -> R.id.sort_by_create_date
                    ArtistSortType.NAME -> R.id.sort_by_name
                    ArtistSortType.SONG_COUNT -> R.id.sort_by_song_count
                })?.isChecked = true
                show()
            }
        }
        binding.sortOrder.setOnClickListener {
            ArtistSortInfoPreference.toggleIsDescending()
        }
        binding.sortName.setText(when (header.sortInfo.type) {
            ArtistSortType.CREATE_DATE -> R.string.sort_by_create_date
            ArtistSortType.NAME -> R.string.sort_by_name
            ArtistSortType.SONG_COUNT -> R.string.sort_by_song_count
        })
        updateSortOrderIcon(header.sortInfo.isDescending, isPayload)
        binding.countText.text = binding.context.resources.getQuantityString(R.plurals.artist_count, header.artistCount, header.artistCount)
    }

    private fun updateSortOrderIcon(sortDescending: Boolean, animate: Boolean = true) {
        if (sortDescending) {
            binding.sortOrder.animateToDown(animate)
        } else {
            binding.sortOrder.animateToUp(animate)
        }
    }
}

class AlbumHeaderViewHolder(
    override val binding: ItemHeaderBinding,
) : LocalItemViewHolder(binding) {
    override val itemDetails: ItemDetailsLookup.ItemDetails<String>? = null

    fun bind(header: AlbumHeader, isPayload: Boolean = false) {
        binding.sortName.setOnClickListener { view ->
            PopupMenu(view.context, view).apply {
                inflate(R.menu.sort_album)
                setOnMenuItemClickListener {
                    AlbumSortInfoPreference.type = when (it.itemId) {
                        R.id.sort_by_create_date -> AlbumSortType.CREATE_DATE
                        R.id.sort_by_name -> AlbumSortType.NAME
                        R.id.sort_by_artist -> AlbumSortType.ARTIST
                        R.id.sort_by_year -> AlbumSortType.YEAR
                        R.id.sort_by_song_count -> AlbumSortType.SONG_COUNT
                        R.id.sort_by_length -> AlbumSortType.LENGTH
                        else -> throw IllegalArgumentException("Unexpected sort type.")
                    }
                    true
                }
                menu.findItem(when (header.sortInfo.type) {
                    AlbumSortType.CREATE_DATE -> R.id.sort_by_create_date
                    AlbumSortType.NAME -> R.id.sort_by_name
                    AlbumSortType.ARTIST -> R.id.sort_by_artist
                    AlbumSortType.YEAR -> R.id.sort_by_year
                    AlbumSortType.SONG_COUNT -> R.id.sort_by_song_count
                    AlbumSortType.LENGTH -> R.id.sort_by_length
                })?.isChecked = true
                show()
            }
        }
        binding.sortOrder.setOnClickListener {
            AlbumSortInfoPreference.toggleIsDescending()
        }
        binding.sortName.setText(when (header.sortInfo.type) {
            AlbumSortType.CREATE_DATE -> R.string.sort_by_create_date
            AlbumSortType.NAME -> R.string.sort_by_name
            AlbumSortType.ARTIST -> R.string.sort_by_artist
            AlbumSortType.YEAR -> R.string.sort_by_year
            AlbumSortType.SONG_COUNT -> R.string.sort_by_song_count
            AlbumSortType.LENGTH -> R.string.sort_by_length
        })
        updateSortOrderIcon(header.sortInfo.isDescending, isPayload)
        binding.countText.text = binding.context.resources.getQuantityString(R.plurals.album_count, header.albumCount, header.albumCount)
    }

    private fun updateSortOrderIcon(sortDescending: Boolean, animate: Boolean = true) {
        if (sortDescending) {
            binding.sortOrder.animateToDown(animate)
        } else {
            binding.sortOrder.animateToUp(animate)
        }
    }
}


class PlaylistHeaderViewHolder(
    override val binding: ItemHeaderBinding,
) : LocalItemViewHolder(binding) {
    override val itemDetails: ItemDetailsLookup.ItemDetails<String>? = null

    fun bind(header: PlaylistHeader, isPayload: Boolean = false) {
        binding.sortName.setOnClickListener { view ->
            PopupMenu(view.context, view).apply {
                inflate(R.menu.sort_playlist)
                setOnMenuItemClickListener {
                    PlaylistSortInfoPreference.type = when (it.itemId) {
                        R.id.sort_by_create_date -> PlaylistSortType.CREATE_DATE
                        R.id.sort_by_name -> PlaylistSortType.NAME
                        R.id.sort_by_song_count -> PlaylistSortType.SONG_COUNT
                        else -> throw IllegalArgumentException("Unexpected sort type.")
                    }
                    true
                }
                menu.findItem(when (header.sortInfo.type) {
                    PlaylistSortType.CREATE_DATE -> R.id.sort_by_create_date
                    PlaylistSortType.NAME -> R.id.sort_by_name
                    PlaylistSortType.SONG_COUNT -> R.id.sort_by_song_count
                })?.isChecked = true
                show()
            }
        }
        binding.sortOrder.setOnClickListener {
            PlaylistSortInfoPreference.toggleIsDescending()
        }
        binding.sortName.setText(when (header.sortInfo.type) {
            PlaylistSortType.CREATE_DATE -> R.string.sort_by_create_date
            PlaylistSortType.NAME -> R.string.sort_by_name
            PlaylistSortType.SONG_COUNT -> R.string.sort_by_song_count
        })
        updateSortOrderIcon(header.sortInfo.isDescending, isPayload)
        binding.countText.text = binding.context.resources.getQuantityString(R.plurals.playlist_count, header.playlistCount, header.playlistCount)
    }

    private fun updateSortOrderIcon(sortDescending: Boolean, animate: Boolean = true) {
        if (sortDescending) {
            binding.sortOrder.animateToDown(animate)
        } else {
            binding.sortOrder.animateToUp(animate)
        }
    }
}

class PlaylistSongHeaderViewHolder(
    override val binding: ItemPlaylistHeaderBinding,
    private val onShuffle: () -> Unit = {},
) : LocalItemViewHolder(binding) {
    override val itemDetails: ItemDetailsLookup.ItemDetails<String>? = null

    fun bind(header: PlaylistSongHeader) {
        binding.title.text = listOf(
            binding.context.resources.getQuantityString(R.plurals.song_count, header.songCount, header.songCount),
            makeTimeString(header.length * 1000)
        ).joinByBullet()
        binding.btnShuffle.setOnClickListener {
            onShuffle()
        }
    }
}

class TextHeaderViewHolder(
    override val binding: ItemTextHeaderBinding,
) : LocalItemViewHolder(binding) {
    override val itemDetails: ItemDetailsLookup.ItemDetails<String>? = null

    fun bind(header: TextHeader) {
        binding.title.text = header.title
    }
}