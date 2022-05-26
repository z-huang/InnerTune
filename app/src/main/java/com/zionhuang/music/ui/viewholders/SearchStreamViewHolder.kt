package com.zionhuang.music.ui.viewholders

import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemSearchStreamBinding
import com.zionhuang.music.extensions.context
import com.zionhuang.music.extensions.id
import com.zionhuang.music.extensions.load
import com.zionhuang.music.extensions.roundCorner
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.listeners.StreamPopupMenuListener
import com.zionhuang.music.ui.viewholders.base.SearchViewHolder
import com.zionhuang.music.utils.makeTimeString
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class SearchStreamViewHolder(
    override val binding: ItemSearchStreamBinding,
    private val listener: StreamPopupMenuListener?,
) : SearchViewHolder(binding) {
    private var inLibraryLiveData: LiveData<Boolean>? = null
    private var inLibraryObserver = Observer<Boolean> {
        binding.addedToLibrary.isVisible = it
    }

    fun bind(item: StreamInfoItem) {
        binding.songTitle.text = item.name
        binding.duration.text = makeTimeString(item.duration)
        binding.songArtist.text = item.uploaderName
        binding.publishDate.text = item.textualUploadDate
        if (item.textualUploadDate.isNullOrEmpty()) {
            binding.publishBullet.isVisible = false
        }
        binding.thumbnail.load(item.thumbnailUrl) {
            placeholder(R.drawable.ic_music_note)
            roundCorner(binding.thumbnail.context.resources.getDimensionPixelSize(R.dimen.song_cover_radius))
        }
        removeListener()
        SongRepository.hasSong(item.id).liveData.apply {
            inLibraryLiveData = this
            observeForever(inLibraryObserver)
        }
        setupMenu(item)
    }

    private fun setupMenu(item: StreamInfoItem) {
        binding.btnMoreAction.setOnClickListener { view ->
            PopupMenu(view.context, view).apply {
                inflate(R.menu.search_item)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_add_to_library -> listener?.addToLibrary(item)
                        R.id.action_play_next -> listener?.playNext(item)
                        R.id.action_add_to_queue -> listener?.addToQueue(item)
                        R.id.action_add_to_playlist -> listener?.addToPlaylist(item, binding.context)
                        R.id.action_download -> listener?.download(item, binding.context)
                    }
                    true
                }
                show()
            }
        }
    }

    private fun removeListener() {
        binding.addedToLibrary.isVisible = false
        inLibraryLiveData?.removeObserver(inLibraryObserver)
        inLibraryLiveData = null
    }

    override fun onDetach() {
        super.onDetach()
        removeListener()
    }
}