package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.repos.NewPipeRepository
import kotlinx.coroutines.launch

class SuggestionViewModel(application: Application) : AndroidViewModel(application) {
    val onFillQuery = MutableLiveData<String?>()
    val query = MutableLiveData<String?>(null)
    val suggestions = MutableLiveData<List<String>>(emptyList())

    fun fillQuery(q: String) {
        onFillQuery.postValue(q)
    }

    fun setQuery(q: String?) {
        query.postValue(q)
    }

    fun fetchSuggestions(query: String?) {
        if (query.isNullOrEmpty()) {
            suggestions.postValue(emptyList())
            return
        }
        viewModelScope.launch {
            try {
                suggestions.postValue(NewPipeRepository.suggestionsFor(query))
            } catch (e: Exception) {
            }
        }
    }
}