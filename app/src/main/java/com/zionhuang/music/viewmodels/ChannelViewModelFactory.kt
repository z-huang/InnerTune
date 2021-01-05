@file:Suppress("UNCHECKED_CAST")

package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChannelViewModelFactory(val application: Application, val channelId: String) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            ChannelViewModel(application, channelId) as T
}