package com.zionhuang.music.ui.adapters

import android.content.Intent
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemSongBinding
import com.zionhuang.music.db.SongEntity
import com.zionhuang.music.download.DownloadHandler
import com.zionhuang.music.download.DownloadService
import com.zionhuang.music.download.DownloadTask
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADING
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.fragments.LibraryFragmentDirections

class SongsAdapter(val downloadHandler: DownloadHandler) : PagingDataAdapter<SongEntity, SongsAdapter.SongViewHolder>(ItemComparator()) {
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int, payloads: List<Any>) {
        when {
            payloads.isEmpty() -> getItem(position)?.let { holder.bind(it) }
            else -> {
                holder.bind(payloads.last() as SongEntity)
//                val payload = payloads.last() as List<Any?>
//                payload[0]?.let { holder.binding.songTitle.text = it as String }
//                payload[1]?.let { holder.binding.songArtist.text = it as String }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder =
            SongViewHolder(parent.inflateWithBinding(R.layout.item_song))

    fun getItemByPosition(position: Int): SongEntity? = getItem(position)

    inner class SongViewHolder(val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: SongEntity) {
            binding.song = song
            binding.btnMoreAction.setOnClickListener { view ->
                PopupMenu(view.context, view).apply {
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.action_edit -> {
                                view.findNavController().navigate(LibraryFragmentDirections.actionLibraryFragmentToSongDetailsFragment(song.id))
                            }
                            R.id.action_download -> {
                                view.context.startService(Intent(view.context, DownloadService::class.java).apply {
                                    action = DownloadService.DOWNLOAD_MUSIC_INTENT
                                    putExtra("task", DownloadTask(id = song.id))
                                })
                            }
                        }
                        true
                    }
                    inflate(R.menu.menu_song)
                    show()
                }
            }
            if (song.downloadState == STATE_DOWNLOADING) {
                binding.progressBar.progress = 0
                downloadHandler.add(song.id, this)
            }
            binding.executePendingBindings()
        }

        fun setProgress(task: DownloadTask) {
            binding.progressBar.apply {
                max = task.totalBytes.toInt()
                progress = task.currentBytes.toInt()
            }
        }
    }

    override fun onViewRecycled(holder: SongViewHolder) {
        holder.binding.song?.id?.let { downloadHandler.remove(it) }
    }

    internal class ItemComparator : DiffUtil.ItemCallback<SongEntity>() {
        override fun areItemsTheSame(oldItem: SongEntity, newItem: SongEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SongEntity, newItem: SongEntity): Boolean = oldItem == newItem
        override fun getChangePayload(oldItem: SongEntity, newItem: SongEntity): SongEntity = newItem
    }

}