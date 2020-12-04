package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.api.services.youtube.model.Video
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemVideoBinding
import com.zionhuang.music.extensions.inflateWithBinding

class ExploreAdapter : PagingDataAdapter<Video, ExploreAdapter.ViewHolder>(ItemComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflateWithBinding(R.layout.item_video))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    fun getItemByPosition(position: Int): Video? = getItem(position)

    internal class ItemComparator : DiffUtil.ItemCallback<Video>() {
        override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean =
                oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean =
                oldItem.etag == newItem.etag
    }

    inner class ViewHolder(private val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(video: Video) {
            binding.video = video
        }
    }
}