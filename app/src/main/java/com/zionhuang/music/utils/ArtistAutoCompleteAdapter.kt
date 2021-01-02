package com.zionhuang.music.utils

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.db.entities.ArtistEntity

class ArtistAutoCompleteAdapter(context: Context) : ArrayAdapter<ArtistEntity>(context, android.R.layout.simple_list_item_1) {
    private val songRepository = SongRepository(context)

    private val filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val resultsList = if (constraint.isNullOrEmpty()) {
                songRepository.allArtists
            } else {
                songRepository.searchArtists(constraint)
            }
            return FilterResults().apply {
                values = resultsList
                count = resultsList.size
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            clear()
            addAll(results.values as List<ArtistEntity>)
            notifyDataSetChanged()
        }

        override fun convertResultToString(resultValue: Any?): CharSequence = (resultValue as ArtistEntity).name
    }

    override fun getFilter(): Filter = filter

    init {
        setNotifyOnChange(false)
    }

    companion object {
        private const val TAG = "AutoCompleteAdapter"
    }
}