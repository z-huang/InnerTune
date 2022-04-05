package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.update.UpdateInfo
import com.zionhuang.music.update.UpdateManager
import com.zionhuang.music.update.UpdateStatus
import kotlinx.coroutines.launch

class UpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val updateManager = UpdateManager(application)

    val updateInfo: LiveData<UpdateInfo> = updateManager.updateInfoLiveData
    val updateStatus: LiveData<UpdateStatus> = updateManager.updateStatusLiveData

    fun checkForUpdate(force: Boolean = false) = viewModelScope.launch {
        updateManager.checkForUpdate(force)
    }

    suspend fun getLatestRelease() = updateManager.getLatestRelease()

    fun updateApp() = viewModelScope.launch { updateManager.updateApp() }
}