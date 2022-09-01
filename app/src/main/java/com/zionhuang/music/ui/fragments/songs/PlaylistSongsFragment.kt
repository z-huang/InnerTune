package com.zionhuang.music.ui.fragments.songs

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.LocalItem
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.addFastScroller
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.ui.adapters.LocalItemAdapter
import com.zionhuang.music.ui.fragments.base.RecyclerViewFragment
import com.zionhuang.music.ui.listeners.SongMenuListener
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.PlaylistSongsViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlaylistSongsFragment : RecyclerViewFragment<LocalItemAdapter>(), MenuProvider {
    private val args: PlaylistSongsFragmentArgs by navArgs()
    private val playlistId by lazy { args.playlistId }

    private val playbackViewModel by activityViewModels<PlaybackViewModel>()
    private val songsViewModel by activityViewModels<SongsViewModel>()
    private val viewModel by viewModels<PlaylistSongsViewModel>()
    override val adapter = LocalItemAdapter().apply {
        songMenuListener = SongMenuListener(this@PlaylistSongsFragment)
    }

//    private val itemTouchHelper = ItemTouchHelper(object : SimpleCallback(UP or DOWN, LEFT or RIGHT) {
//        private val elevation by lazy { requireContext().resources.getDimension(R.dimen.drag_item_elevation) }
//
//        override fun isLongPressDragEnabled(): Boolean = false
//
//        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//            if (isCurrentlyActive) {
//                ViewCompat.setElevation(viewHolder.itemView, elevation)
//            }
//        }
//
//        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
//            super.clearView(recyclerView, viewHolder)
//            ViewCompat.setElevation(viewHolder.itemView, 0f)
//            adapter.processMove()
//        }
//
//        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
//            val from = viewHolder.absoluteAdapterPosition
//            val to = target.absoluteAdapterPosition
//            adapter.moveItem(from, to)
//            return true
//        }
//
//        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//            viewModel.removeFromPlaylist(playlistId, adapter.getItemByPosition(viewHolder.absoluteAdapterPosition)!!.idInPlaylist!!)
//        }
//    })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
//            itemTouchHelper.attachToRecyclerView(this)
            addFastScroller { useMd2Style() }
        }
        binding.recyclerView.addOnClickListener { position, _ ->
            if (adapter.currentList[position] !is LocalItem) return@addOnClickListener

            playbackViewModel.playQueue(requireActivity(), ListQueue(
                items = adapter.currentList.filterIsInstance<Song>().map { it.toMediaItem() },
                startIndex = position - 1
            ))
        }
        adapter.onShuffle = {
            playbackViewModel.playQueue(requireActivity(), ListQueue(
                items = adapter.currentList.filterIsInstance<Song>().shuffled().map { it.toMediaItem() }
            ))
        }

        lifecycleScope.launch {
            songsViewModel.getPlaylistSongsAsFlow(playlistId).collectLatest {
                adapter.submitList(it)
            }
        }

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.playlist_songs_fragment, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> findNavController().navigate(PlaylistSongsFragmentDirections.actionPlaylistSongsFragmentToPlaylistSongsEditFragment(playlistId))
        }
        return true
    }
}