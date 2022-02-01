package com.zionhuang.music.utils

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.runBlocking

class ArtistAutoCompleteAdapter(context: Context) : ArrayAdapter<ArtistEntity>(context, android.R.layout.simple_list_item_1) {
    private val songRepository = SongRepository

    private val filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val resultsList = runBlocking {
                if (constraint.isNullOrEmpty()) {
                    songRepository.getAllArtists().getList()
                } else {
                    songRepository.searchArtists(constraint.toString()).getList()
                }
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