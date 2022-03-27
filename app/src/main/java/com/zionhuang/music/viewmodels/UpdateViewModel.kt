package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.update.UpdateInfo
import com.zionhuang.music.update.UpdateManager
import kotlinx.coroutines.launch

class UpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val updateManager = UpdateManager(application)

    val updateInfo: LiveData<UpdateInfo> = updateManager.updateInfoLiveData

    fun checkForUpdate(force: Boolean = false) = viewModelScope.launch {
        updateManager.checkForUpdate(force)
    }

    suspend fun getLatestRelease() = updateManager.getLatestRelease()
}