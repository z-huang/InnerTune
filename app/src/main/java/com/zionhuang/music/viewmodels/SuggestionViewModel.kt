package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.repos.NewPipeRepository
import kotlinx.coroutines.launch

class SuggestionViewModel(application: Application) : AndroidViewModel(application) {
    val suggestions = MutableLiveData<List<String>>(emptyList())

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