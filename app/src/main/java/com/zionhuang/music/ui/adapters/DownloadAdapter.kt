package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemDownloadBinding
import com.zionhuang.music.download.DownloadTask
import com.zionhuang.music.extensions.inflateWithBinding

class DownloadAdapter : RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder>() {
    private var dataSet = emptyList<DownloadTask>()

    fun setData(dataSet: List<DownloadTask>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder =
            DownloadViewHolder(parent.inflateWithBinding(R.layout.item_download))

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int, payloads: MutableList<Any>) {
        when {
            payloads.isEmpty() -> holder.bind(dataSet[position])
        }
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int = dataSet.size

    inner class DownloadViewHolder(private val binding: ItemDownloadBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: DownloadTask) {
            binding.songTitle.text = task.songTitle
            binding.progressBar.max = task.totalBytes.toInt()
            binding.progressBar.progress = task.currentBytes.toInt()
        }
    }
}