package com.zionhuang.music.ui.fragments.songs

import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.models.sortInfo.SongSortInfoPreference
import com.zionhuang.music.ui.adapters.PlaylistSongsEditAdapter
import com.zionhuang.music.ui.fragments.base.RecyclerViewFragment
import com.zionhuang.music.viewmodels.PlaylistSongsViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.launch

class PlaylistSongsEditFragment : RecyclerViewFragment<PlaylistSongsEditAdapter>() {
    private val args: PlaylistSongsEditFragmentArgs by navArgs()
    private val playlistId by lazy { args.playlistId }

    private val songsViewModel by activityViewModels<SongsViewModel>()
    private val viewModel by viewModels<PlaylistSongsViewModel>()
    override val adapter = PlaylistSongsEditAdapter()

    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
        private val elevation by lazy { requireContext().resources.getDimension(R.dimen.drag_item_elevation) }

        override fun isLongPressDragEnabled(): Boolean = false

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            if (isCurrentlyActive) {
                ViewCompat.setElevation(viewHolder.itemView, elevation)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            ViewCompat.setElevation(viewHolder.itemView, 0f)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val from = viewHolder.absoluteAdapterPosition
            val to = target.absoluteAdapterPosition
            adapter.moveItem(from, to)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
    })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.apply {
            itemTouchHelper = this@PlaylistSongsEditFragment.itemTouchHelper
            onProcessMove = {
                viewModel.processMove(playlistId, it)
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemTouchHelper.attachToRecyclerView(this)
        }

        lifecycleScope.launch {
            adapter.submitList(songsViewModel.songRepository.getPlaylistSongs(playlistId, SongSortInfoPreference).getList())
        }
    }

    override fun onDestroy() {
        adapter.processMove()
        super.onDestroy()
    }
}