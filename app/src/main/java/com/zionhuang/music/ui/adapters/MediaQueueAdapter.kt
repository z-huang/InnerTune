package com.zionhuang.music.ui.adapters

import android.support.v4.media.session.MediaSessionCompat.QueueItem
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.viewholders.QueueItemViewHolder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class MediaQueueAdapter : RecyclerView.Adapter<QueueItemViewHolder>() {
    private var items: List<QueueItem> = emptyList()
    private val diffCallback = QueueItemComparator()

    override fun onBindViewHolder(holder: QueueItemViewHolder, position: Int) =
            holder.bind(items[position])

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueItemViewHolder =
            QueueItemViewHolder(parent.inflateWithBinding(R.layout.item_queue))

    override fun getItemCount(): Int = items.size

    suspend fun submitData(newItems: List<QueueItem>) {
        val diffResult = withContext(IO) {
            DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = items.size
                override fun getNewListSize(): Int = newItems.size
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = diffCallback.areItemsTheSame(items[oldItemPosition], newItems[newItemPosition])
                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = diffCallback.areContentsTheSame(items[oldItemPosition], newItems[newItemPosition])
            })
        }
        diffResult.dispatchUpdatesTo(this)
        items = newItems
    }

    class QueueItemComparator : DiffUtil.ItemCallback<QueueItem>() {
        override fun areItemsTheSame(oldItem: QueueItem, newItem: QueueItem): Boolean = oldItem.description.mediaId == newItem.description.mediaId

        override fun areContentsTheSame(oldItem: QueueItem, newItem: QueueItem): Boolean = oldItem.description.title.toString() == newItem.description.title.toString() &&
                oldItem.description.subtitle.toString() == newItem.description.subtitle.toString() &&
                oldItem.description.iconUri == newItem.description.iconUri
    }
}