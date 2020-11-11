package com.zionhuang.music.extensions

import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SearchViewTextEvent(val query: String?, val isSubmitted: Boolean)

fun SearchView.getQueryTextChangeFlow(): StateFlow<SearchViewTextEvent> {
    val query = MutableStateFlow(SearchViewTextEvent(this.query.toString(), false))
    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(s: String?): Boolean {
            query.value = SearchViewTextEvent(s, true)
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            query.value = SearchViewTextEvent(newText, false)
            return true
        }
    })
    return query
}