package com.zionhuang.music.ui.fragments.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_MEDIA_METADATA
import com.zionhuang.music.databinding.DialogChooseLyricsBinding
import com.zionhuang.music.db.entities.LyricsEntity
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.adapters.LyricsAdapter
import com.zionhuang.music.utils.lyrics.LyricsHelper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChooseLyricsDialog : AppCompatDialogFragment() {
    private lateinit var binding: DialogChooseLyricsBinding
    private lateinit var mediaMetadata: MediaMetadata
    private val adapter = LyricsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaMetadata = arguments?.getParcelable(EXTRA_MEDIA_METADATA)!!
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("NotifyDataSetChanged")
    private fun setupUI() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addOnClickListener { position, _ ->
            GlobalScope.launch {
                SongRepository(requireContext()).upsert(LyricsEntity(
                    mediaMetadata.id,
                    adapter.items[position].lyrics
                ))
            }
            dismiss()
        }
        lifecycleScope.launch {
            adapter.items = LyricsHelper.getAllLyrics(requireContext(), mediaMetadata)
            adapter.notifyDataSetChanged()
            binding.progressBar.isVisible = false
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogChooseLyricsBinding.inflate(layoutInflater)
        setupUI()

        return MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
            .setTitle(R.string.dialog_title_choose_lyrics)
            .setView(binding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }
}