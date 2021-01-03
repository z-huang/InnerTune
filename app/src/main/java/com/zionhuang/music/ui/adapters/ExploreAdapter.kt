package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.google.api.services.youtube.model.Video
import com.zionhuang.music.R
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.viewholders.VideoViewHolder

class ExploreAdapter : PagingDataAdapter<Video, VideoViewHolder>(VideoItemComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder =
            VideoViewHolder(parent.inflateWithBinding(R.layout.item_video))

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    fun getItemByPosition(position: Int): Video? = getItem(position)

    class VideoItemComparator : DiffUtil.ItemCallback<Video>() {
        override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean = oldItem.etag == newItem.etag
    }
}