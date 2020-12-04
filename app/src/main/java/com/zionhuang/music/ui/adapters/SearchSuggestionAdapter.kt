package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemSuggestionBinding
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.viewmodels.SuggestionViewModel

class SearchSuggestionAdapter(private val viewModel: SuggestionViewModel) : RecyclerView.Adapter<SearchSuggestionAdapter.ViewHolder>() {
    private var dataSet = emptyList<String>()
    fun setDataSet(dataSet: List<String>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflateWithBinding(R.layout.item_suggestion))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int = dataSet.size

    fun getQueryByPosition(position: Int): String = dataSet[position]

    inner class ViewHolder(private val binding:ItemSuggestionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(query: String) {
            binding.query = query
            binding.executePendingBindings()
            binding.fillTextButton.setOnClickListener { viewModel.fillQuery(binding.query!!) }
        }
    }
}