package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.zionhuang.music.youtube.YouTubeRepository
import com.zionhuang.music.youtube.YouTubeRepository.Companion.getInstance
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class SuggestionViewModel(application: Application) : AndroidViewModel(application) {
    private val youtubeRepo: YouTubeRepository = getInstance(application)
    val onFillQuery = MutableLiveData<String>()
    val query = MutableLiveData<String?>(null)
    val suggestions = MutableLiveData<List<String>>(emptyList())
    private val compositeDisposable = CompositeDisposable()

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
        val disposable = youtubeRepo.getSuggestions(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ value: List<String> -> suggestions.postValue(value) }) { obj: Throwable -> obj.printStackTrace() }
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

}