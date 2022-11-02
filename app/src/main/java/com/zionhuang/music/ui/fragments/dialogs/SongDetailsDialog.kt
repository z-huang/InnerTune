package com.zionhuang.music.ui.fragments.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE
import android.text.format.Formatter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.getSystemService
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.music.R
import com.zionhuang.music.databinding.DialogSongDetailsBinding
import com.zionhuang.music.viewmodels.PlaybackViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SongDetailsDialog : AppCompatDialogFragment() {
    private lateinit var binding: DialogSongDetailsBinding
    private val viewModel by activityViewModels<PlaybackViewModel>()

    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        listOf(binding.songTitle, binding.songArtist, binding.mediaId, binding.mimeType, binding.codecs, binding.bitrate, binding.sampleRate, binding.loudness, binding.fileSize).forEach { textView ->
            textView.setOnClickListener {
                val clipboardManager = requireContext().getSystemService<ClipboardManager>()!!
                val clip = ClipData.newPlainText(null, textView.text)
                clipboardManager.setPrimaryClip(clip)
                Toast.makeText(requireContext(), R.string.copied, Toast.LENGTH_SHORT).show()
            }
        }
        lifecycleScope.launch {
            viewModel.mediaMetadata.collectLatest { mediaMetadata ->
                binding.songTitle.text = mediaMetadata?.getString(METADATA_KEY_TITLE)
                binding.songArtist.text = mediaMetadata?.getString(METADATA_KEY_ARTIST)
                binding.mediaId.text = mediaMetadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            }
        }
        lifecycleScope.launch {
            viewModel.currentSongFormat.collectLatest { format ->
                binding.itag.text = format?.itag?.toString() ?: getString(R.string.unknown)
                binding.mimeType.text = format?.mimeType ?: getString(R.string.unknown)
                binding.codecs.text = format?.codecs ?: getString(R.string.unknown)
                binding.bitrate.text = format?.bitrate?.let { "${it / 1000} Kbps" } ?: getString(R.string.unknown)
                binding.sampleRate.text = format?.sampleRate?.let { "$it Hz" } ?: getString(R.string.unknown)
                binding.loudness.text = format?.loudnessDb?.let { "$it dB" } ?: getString(R.string.unknown)
                binding.fileSize.text = format?.contentLength?.let { Formatter.formatShortFileSize(requireContext(), it) } ?: getString(R.string.unknown)
            }
        }
        lifecycleScope.launch {
            viewModel.playerVolume.collectLatest { volume ->
                binding.volume.text = "${(volume * 100).toInt()}%"
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSongDetailsBinding.inflate(requireActivity().layoutInflater)
        setupUI()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_title_details)
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok, null)
            .create()
    }
}
