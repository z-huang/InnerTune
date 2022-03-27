package com.zionhuang.music.update

import android.app.DownloadManager
import android.content.Context
import android.util.Log
import androidx.core.content.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zionhuang.music.BuildConfig
import com.zionhuang.music.R
import com.zionhuang.music.extensions.TAG
import com.zionhuang.music.utils.OkHttpDownloader
import com.zionhuang.music.utils.preference.Preference
import com.zionhuang.music.utils.preference.PreferenceStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit

class UpdateManager(context: Context) {
    private val downloadManager by lazy { context.getSystemService<DownloadManager>()!! }
    private val preferenceStore by lazy { PreferenceStore.getInstance(context) }
    private val currentVersion = Version.parse(BuildConfig.VERSION_NAME)
    private var lastCheckTime by Preference({ context }, R.string.pref_last_check_time, 0L)

    private val _updateInfoLiveData = MutableLiveData(if (isLastCheckExpired) UpdateInfo.NotChecked else UpdateInfo.UpToDate)
    val updateInfoLiveData: LiveData<UpdateInfo> = _updateInfoLiveData

    private val _updateStatusLiveData: MutableLiveData<UpdateStatus> = MutableLiveData(UpdateStatus.Idle)
    val updateStatusLiveData: LiveData<UpdateStatus> = _updateStatusLiveData

    suspend fun getLatestRelease(): Release = withContext(Dispatchers.IO) {
        Gson().fromJson(OkHttpDownloader.downloadJson(RELEASE_API_URL), Array<Release>::class.java)[0]
    }

    private val isLastCheckExpired: Boolean get() = Instant.ofEpochSecond(lastCheckTime).plus(CHECK_FREQUENCY_DAY, ChronoUnit.DAYS) < Instant.now()

    suspend fun checkForUpdate(force: Boolean = false) {
        if (!force && !isLastCheckExpired) return
        _updateInfoLiveData.postValue(UpdateInfo.Checking)
        try {
            val release = getLatestRelease()
            val latestVersion = Version.parse(release.name)
            Log.d(TAG, release.toString())
            lastCheckTime = Instant.now().epochSecond
            _updateInfoLiveData.postValue(if (currentVersion < latestVersion) UpdateInfo.UpdateAvailable(latestVersion) else UpdateInfo.UpToDate)
        } catch (e: Exception) {
            _updateInfoLiveData.postValue(UpdateInfo.Exception)
        }
    }

    companion object {
        const val RELEASE_API_URL = "https://api.github.com/repos/z-huang/music/releases?per_page=1"
        const val APK_MIMETYPE = "application/vnd.android.package-archive"
        const val CHECK_FREQUENCY_DAY = 1L
        const val KEY_APK_FILENAME = "update_apk_filename"
        const val KEY_APK_DOWNLOAD_ID = "update_apk_download_id"
    }
}