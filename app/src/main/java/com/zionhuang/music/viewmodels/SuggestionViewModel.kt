package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.models.YTBaseItem
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.repos.YouTubeRepository
import kotlinx.coroutines.launch

class SuggestionViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository(application)
    private val youTubeRepository = YouTubeRepository(application)
    val suggestions = MutableLiveData<List<YTBaseItem>>(emptyList())

    fun fetchSuggestions(query: String?) = viewModelScope.launch {
//        if (query.isNullOrEmpty()) {
//            suggestions.postValue(songRepository.getAllSearchHistory().map { SuggestionTextItem(it.query, LOCAL) })
//        } else {
//            val history = songRepository.getSearchHistory(query).map {
//                SuggestionTextItem(it.query, LOCAL)
//            }
//            val ytSuggestions = try {
//                youTubeRepository.getSuggestions(query).filter { item ->
//                    item !is SuggestionTextItem || history.find { it.query == item.query } == null
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                // Fix incorrect visitorData
//                // comment out because now YouTube Music doesn't give us suggestions if we're not logged in
////                if (e is MissingFieldException) {
////                    // Reset visitorData
////                    YouTube.generateVisitorData().getOrNull()?.let {
////                        getApplication<Application>().sharedPreferences.edit {
////                            putString(getApplication<Application>().getString(R.string.pref_visitor_data), it)
////                        }
////                        YouTube.visitorData = it
////                    }
////                }
//                emptyList()
//            }
//            suggestions.postValue(history + ytSuggestions)
//        }
    }
}