package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.moveQueueItem
import com.zionhuang.music.extensions.seekToQueueItem
import com.zionhuang.music.ui.adapters.MediaQueueAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.PlaybackViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QueueFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    private val viewModel by activityViewModels<PlaybackViewModel>()
    private lateinit var queueAdapter: MediaQueueAdapter
    private val dragEventManager = DragEventManager()

    private val itemTouchHelper = ItemTouchHelper(object : SimpleCallback(UP or DOWN, LEFT or RIGHT) {
        override fun isLongPressDragEnabled(): Boolean = false

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            when (actionState) {
                ACTION_STATE_DRAG -> dragEventManager.postDragStart(viewHolder?.absoluteAdapterPosition)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            dragEventManager.postDragEnd(viewHolder.absoluteAdapterPosition)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val from = viewHolder.absoluteAdapterPosition
            val to = target.absoluteAdapterPosition
            queueAdapter.moveItem(from, to)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            viewModel.mediaController.value?.removeQueueItem(queueAdapter.getItem(viewHolder.absoluteAdapterPosition).description)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply { duration = 300L }
        exitTransition = MaterialFadeThrough().apply { duration = 300L }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        queueAdapter = MediaQueueAdapter(itemTouchHelper)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = queueAdapter
            itemTouchHelper.attachToRecyclerView(this)
            addOnClickListener { pos, _ ->
                queueAdapter.getItem(pos).description.mediaId?.let {
                    viewModel.mediaController.value?.seekToQueueItem(it)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.queueData.asFlow().collectLatest {
                queueAdapter.submitData(it.items)
            }
        }

        dragEventManager.onDragged = { fromPos, toPos ->
            viewModel.mediaController.value?.moveQueueItem(fromPos, toPos)
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