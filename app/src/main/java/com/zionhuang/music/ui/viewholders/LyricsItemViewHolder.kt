package com.zionhuang.music.ui.viewholders

import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.music.databinding.ItemLyricsBinding
import com.zionhuang.music.extensions.context
import com.zionhuang.music.utils.lyrics.LyricsHelper

class LyricsItemViewHolder(
    val binding: ItemLyricsBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(lyricsResult: LyricsHelper.LyricsResult) {
        binding.lyrics.text = lyricsResult.lyrics
        binding.provider.text = lyricsResult.providerName
        binding.synced.isVisible = lyricsResult.lyrics.startsWith("[")
        binding.btnView.setOnClickListener {
            MaterialAlertDialogBuilder(binding.context)
                .setMessage(lyricsResult.lyrics)
                .setPositiveButton(android.R.string.ok, null)
                .show()
                .apply {
                    window?.decorView?.findViewById<TextView>(android.R.id.message)?.setTextIsSelectable(true)
                }
        }
    }
}