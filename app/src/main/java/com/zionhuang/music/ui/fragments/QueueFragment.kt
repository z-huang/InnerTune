package com.zionhuang.music.ui.fragments

import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.R
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.moveQueueItem
import com.zionhuang.music.extensions.seekToQueueItem
import com.zionhuang.music.ui.adapters.MediaQueueAdapter
import com.zionhuang.music.ui.fragments.base.RecyclerViewFragment
import com.zionhuang.music.viewmodels.PlaybackViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QueueFragment : RecyclerViewFragment<MediaQueueAdapter>() {
    private val viewModel by activityViewModels<PlaybackViewModel>()

    private val dragEventManager = DragEventManager()
    private val itemTouchHelper = ItemTouchHelper(object : SimpleCallback(UP or DOWN, LEFT or RIGHT) {
        private val elevation by lazy { requireContext().resources.getDimension(R.dimen.drag_item_elevation) }

        override fun isLongPressDragEnabled(): Boolean = false

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            when (actionState) {
                ACTION_STATE_DRAG -> dragEventManager.postDragStart(viewHolder?.absoluteAdapterPosition)
            }
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            if (isCurrentlyActive) {
                ViewCompat.setElevation(viewHolder.itemView, elevation)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            ViewCompat.setElevation(viewHolder.itemView, 0f)
            dragEventManager.postDragEnd(viewHolder.absoluteAdapterPosition)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val from = viewHolder.absoluteAdapterPosition
            val to = target.absoluteAdapterPosition
            adapter.moveItem(from, to)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            viewModel.mediaController?.removeQueueItem(adapter.getItem(viewHolder.absoluteAdapterPosition).description)
        }
    })

    override val adapter: MediaQueueAdapter = MediaQueueAdapter(itemTouchHelper)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
        exitTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemTouchHelper.attachToRecyclerView(this)
            addOnClickListener { pos, _ ->
                this@QueueFragment.adapter.getItem(pos).description.mediaId?.let {
                    viewModel.mediaController?.seekToQueueItem(it)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.queueItems.collectLatest {
                adapter.submitData(it)
            }
        }

        dragEventManager.onDragged = { fromPos, toPos ->
            viewModel.mediaController?.moveQueueItem(fromPos, toPos)
        }
    }

    class DragEventManager {
        private var dragFromPosition: Int? = null
        var onDragged: ((fromPos: Int, toPos: Int) -> Unit)? = null

        fun postDragStart(pos: Int?) {
            if (pos == null) return
            dragFromPosition = pos
        }

        fun postDragEnd(pos: Int?) {
            if (pos == null) return
            dragFromPosition?.let { fromPos ->
                dragFromPosition = null
                onDragged?.invoke(fromPos, pos)
            }
        }
    }
}