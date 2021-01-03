package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.viewholders.SuggestionViewHolder

class SearchSuggestionAdapter(
        private val fillQuery: (query: String) -> Unit,
) : RecyclerView.Adapter<SuggestionViewHolder>() {
    private var dataSet = emptyList<String>()

    fun setDataSet(dataSet: List<String>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder =
            SuggestionViewHolder(parent.inflateWithBinding(R.layout.item_suggestion), fillQuery)

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) =
            holder.bind(dataSet[position])

    override fun getItemCount(): Int = dataSet.size

    fun getQueryByPosition(position: Int): String = dataSet[position]
}