package com.zionhuang.music.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.ArtistHeader
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.YTBaseItem
import kotlinx.coroutines.launch

class ArtistViewModel(
    context: Context,
    val artistId: String,
) : ViewModel() {
    val artistHeader = MutableLiveData<ArtistHeader>(null)
    val content = MutableLiveData<List<YTBaseItem>>(emptyList())

    init {
        viewModelScope.launch {
            YouTube.browse(BrowseEndpoint(browseId = artistId)).onSuccess { browseResult ->
                artistHeader.value = browseResult.items.firstOrNull() as? ArtistHeader
                content.value = browseResult.items.drop(1)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class ArtistViewModelFactory(
    val context: Context,
    val artistId: String,
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ArtistViewModel(context, artistId) as T
}
