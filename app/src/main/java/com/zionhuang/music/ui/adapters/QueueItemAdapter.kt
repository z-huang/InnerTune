package com.zionhuang.music.ui.adapters

import android.annotation.SuppressLint
import android.support.v4.media.session.MediaSessionCompat.QueueItem
import android.view.MotionEvent.ACTION_DOWN
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.extensions.swap
import com.zionhuang.music.ui.viewholders.QueueItemViewHolder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext


class QueueItemAdapter(private val itemTouchHelper: ItemTouchHelper) : RecyclerView.Adapter<QueueItemViewHolder>() {
    private var currentList: MutableList<QueueItem> = mutableListOf()
    private val diffCallback = QueueItemComparator()

    override fun onBindViewHolder(holder: QueueItemViewHolder, position: Int) = holder.bind(getItem(position))

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueItemViewHolder =
        QueueItemViewHolder(parent.inflateWithBinding(R.layout.item_queue)).apply {
            binding.dragHandle.setOnTouchListener { _, event ->
                if (event.actionMasked == ACTION_DOWN) {
                    itemTouchHelper.startDrag(this)
                }
                true
            }
        }

    fun moveItem(from: Int, to: Int) {
        currentList.swap(from, to)
        notifyItemMoved(from, to)
    }

    fun removeItem(index: Int) {
        currentList.removeAt(index)
        notifyItemRemoved(index)
    }

    suspend fun submitData(newList: List<QueueItem>) {
        val oldList: List<QueueItem> = currentList
        val result = withContext(IO) {
            DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = oldList.size
                override fun getNewListSize(): Int = newList.size
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = diffCallback.areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])
                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = diffCallback.areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])
            })
        }
        currentList = newList.toMutableList()
        result.dispatchUpdatesTo(this)
    }

    fun getItem(@IntRange(from = 0) position: Int): QueueItem = currentList[position]

    override fun getItemCount(): Int = currentList.size

    class QueueItemComparator : DiffUtil.ItemCallback<QueueItem>() {
        override fun areItemsTheSame(oldItem: QueueItem, newItem: QueueItem): Boolean = oldItem.description.mediaId == newItem.description.mediaId
        override fun areContentsTheSame(oldItem: QueueItem, newItem: QueueItem): Boolean =
            oldItem.description.title.toString() == newItem.description.title.toString() &&
                    oldItem.description.subtitle.toString() == newItem.description.subtitle.toString()
    }

}