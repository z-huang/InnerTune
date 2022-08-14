package com.zionhuang.music.ui.fragments.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.innertube.models.YTItem
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants
import com.zionhuang.music.constants.MediaConstants.EXTRA_BLOCK
import com.zionhuang.music.databinding.DialogChoosePlaylistBinding
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.adapters.LocalItemAdapter
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChoosePlaylistDialog : AppCompatDialogFragment() {
    private lateinit var binding: DialogChoosePlaylistBinding
    private val viewModel by activityViewModels<SongsViewModel>()
    private val adapter = LocalItemAdapter().apply {
        allowMoreAction = false
    }

    private lateinit var item: YTItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        item = arguments?.getParcelable(MediaConstants.EXTRA_YT_ITEM)!!
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setupUI() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChoosePlaylistDialog.adapter
            addOnClickListener { position, _ ->
                GlobalScope.launch {
                    SongRepository.addToPlaylist(item, (this@ChoosePlaylistDialog.adapter.getItemAt(position) as Playlist).playlist)
                }
                dismiss()
            }
        }
        binding.createPlaylist.setOnClickListener {
            CreatePlaylistDialog().apply {
                arguments = bundleOf(EXTRA_BLOCK to { playlist: PlaylistEntity ->
                    GlobalScope.launch {
                        SongRepository.addToPlaylist(item, playlist)
                    }
                    dismiss()
                })
            }.show(parentFragmentManager, null)
            dismiss()
        }
        lifecycleScope.launch {
            viewModel.allPlaylistsFlow.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogChoosePlaylistBinding.inflate(requireActivity().layoutInflater)
        setupUI()

        return MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
            .setTitle(R.string.dialog_title_choose_playlist)
            .setView(binding.root)
            .create()
    }
}