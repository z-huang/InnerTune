package com.zionhuang.music.ui.fragments.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.music.R
import com.zionhuang.music.databinding.DialogChooseLyricsBinding
import com.zionhuang.music.db.entities.LyricsEntity
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.adapters.LyricsAdapter
import com.zionhuang.music.utils.lyrics.LyricsHelper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChooseLyricsDialog() : AppCompatDialogFragment() {
    private lateinit var binding: DialogChooseLyricsBinding
    private val adapter = LyricsAdapter()

    private var mediaId: String? = null
    private lateinit var songTitle: String
    private lateinit var songArtists: String
    private var duration = -1

    constructor(mediaId: String?, songTitle: String, songArtists: String, duration: Int) : this() {
        arguments = bundleOf(
            EXTRA_MEDIA_ID to mediaId,
            EXTRA_SONG_TITLE to songTitle,
            EXTRA_ARTISTS to songArtists,
            EXTRA_DURATION to duration
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaId = arguments?.getString(EXTRA_MEDIA_ID)
        songTitle = arguments?.getString(EXTRA_SONG_TITLE)!!
        songArtists = arguments?.getString(EXTRA_ARTISTS)!!
        duration = arguments?.getInt(EXTRA_DURATION) ?: -1
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("NotifyDataSetChanged")
    private fun setupUI() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addOnClickListener { position, _ ->
            GlobalScope.launch {
                mediaId?.let { mediaId ->
                    SongRepository(requireContext()).upsert(LyricsEntity(
                        mediaId,
                        adapter.items[position].lyrics
                    ))
                }
            }
            dismiss()
        }
        lifecycleScope.launch {
            adapter.items = LyricsHelper.getAllLyrics(requireContext(), mediaId, songTitle, songArtists, duration)
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

    companion object {
        const val EXTRA_MEDIA_ID = "media_id"
        const val EXTRA_SONG_TITLE = "song_title"
        const val EXTRA_ARTISTS = "artists"
        const val EXTRA_DURATION = "duration"
    }
}