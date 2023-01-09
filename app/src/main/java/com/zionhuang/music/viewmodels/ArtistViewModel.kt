package com.zionhuang.music.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.ArtistHeader
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.YTBaseItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ArtistViewModel(
    context: Context,
    val artistId: String,
) : ViewModel() {
    val artistHeader = MutableStateFlow<ArtistHeader?>(null)
    val content = MutableStateFlow<List<YTBaseItem>>(emptyList())

    init {
        viewModelScope.launch {
            YouTube.browse(BrowseEndpoint(browseId = artistId)).onSuccess { browseResult ->
                artistHeader.value = browseResult.items.firstOrNull() as? ArtistHeader
                content.value = browseResult.items.drop(1)
            }
        }
    }

    class Factory(
        val context: Context,
        val artistId: String,
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ArtistViewModel(context, artistId) as T
    }
}
