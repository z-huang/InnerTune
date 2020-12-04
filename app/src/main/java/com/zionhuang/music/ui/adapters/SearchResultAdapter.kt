package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.api.services.youtube.model.SearchResult
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemSearchResultBinding
import com.zionhuang.music.extensions.inflateWithBinding

class SearchResultAdapter : PagingDataAdapter<SearchResult, SearchResultAdapter.ViewHolder>(ItemComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflateWithBinding(R.layout.item_search_result))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun getItemViewType(position: Int): Int =
            when (getItem(position)?.id?.kind) {
                "youtube#video" -> 0
                "youtube#channel" -> 1
                "youtube#playlist" -> 2
                else -> -1
            }

    fun getItemByPosition(position: Int): SearchResult? = getItem(position)

    inner class ViewHolder(private val binding: ItemSearchResultBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SearchResult) {
            binding.item = item
        }
    }

    internal class ItemComparator : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean =
                oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean =
                oldItem.etag == newItem.etag
    }
}