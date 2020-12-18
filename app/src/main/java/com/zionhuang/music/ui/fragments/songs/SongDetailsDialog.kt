package com.zionhuang.music.ui.fragments.songs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat.FEATURE_ACTION_BAR_OVERLAY
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.zionhuang.music.R
import com.zionhuang.music.databinding.LayoutSongDetailsBinding
import com.zionhuang.music.utils.ArtistAutoCompleteAdapter
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.launch


class SongDetailsDialog(private val songId: String) : DialogFragment() {
    private lateinit var binding: LayoutSongDetailsBinding
    private val songsViewModel by activityViewModels<SongsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LayoutSongDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUI()
        lifecycleScope.launch {
            binding.song = songsViewModel.getSong(songId)
        }
    }

    private fun setupUI() {
        requireDialog().window!!.setWindowAnimations(R.style.DialogAnimation)
        binding.toolbar.apply {
            setNavigationIcon(R.drawable.ic_baseline_close_24)
            inflateMenu(R.menu.menu_save)
            setNavigationOnClickListener {
                dismiss()
            }
            setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_save) {
                    item.isEnabled = false
                    saveAndExit()
                }
                true
            }
        }
        binding.songArtistAutoCompleteTextView.apply {
            setAdapter(ArtistAutoCompleteAdapter(requireContext()))
        }
        binding.songTitleEditText.doOnTextChanged { text, _, _, _ ->
            binding.songTitleInputLayout.error = if (text.isNullOrEmpty()) getString(R.string.error_song_title_empty) else null
        }
        binding.songArtistAutoCompleteTextView.doOnTextChanged { text, _, _, _ ->
            binding.songArtistInputLayout.error = if (text.isNullOrEmpty()) getString(R.string.error_song_artist_empty) else null
        }
    }

    private fun saveAndExit() {
        if (binding.songTitleInputLayout.error != null || binding.songArtistInputLayout.error != null) return
        lifecycleScope.launch {
            binding.song?.let { song ->
                songsViewModel.songRepository.updateSong(song.apply {
                    title = binding.songTitleEditText.text.toString()
                    artistName = binding.songArtistAutoCompleteTextView.text.toString()
                })
            }
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            super.onCreateDialog(savedInstanceState).apply {
                requestWindowFeature(FEATURE_ACTION_BAR_OVERLAY)
            }.also {
                Log.d(TAG, "on create dialog ")
            }

    companion object {
        private const val TAG = "SongDetailsFragment"
    }
}