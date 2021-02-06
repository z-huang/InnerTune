package com.zionhuang.music.ui.fragments.songs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.doOnTextChanged
import com.zionhuang.music.R
import com.zionhuang.music.databinding.LayoutSongDetailsBinding
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.utils.ArtistAutoCompleteAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SongDetailsDialog : AppCompatDialogFragment() {
    private lateinit var binding: LayoutSongDetailsBinding
    private lateinit var song: Song

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        song = arguments?.getParcelable("song")!!
    }

    private fun setupUI() {
        binding.songArtistAutoCompleteTextView.apply {
            setAdapter(ArtistAutoCompleteAdapter(requireContext()))
        }
        binding.songTitleEditText.doOnTextChanged { text, _, _, _ ->
            binding.songTitleInputLayout.error = if (text.isNullOrEmpty()) getString(R.string.error_song_title_empty) else null
        }
        binding.songArtistAutoCompleteTextView.doOnTextChanged { text, _, _, _ ->
            binding.songArtistInputLayout.error = if (text.isNullOrEmpty()) getString(R.string.error_song_artist_empty) else null
        }
        with(binding) {
            songTitleEditText.setText(song.title)
            songArtistAutoCompleteTextView.setText(song.artistName)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = LayoutSongDetailsBinding.inflate(requireActivity().layoutInflater, null, false)
        setupUI()

        return AlertDialog.Builder(requireContext())
                .setView(binding.root)
                .setTitle(R.string.dialog_edit_details_title)
                .setPositiveButton(R.string.dialog_button_save) { _, _ ->
                    val title = binding.songTitleEditText.text.toString()
                    val artistName = binding.songArtistAutoCompleteTextView.text.toString()
                    GlobalScope.launch {
                        SongRepository(requireContext()).updateSong(song.apply {
                            this.title = title
                            this.artistName = artistName
                        })
                    }
                }
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .create()
    }

    companion object {
        const val TAG = "SongDetailsFragment"
    }
}