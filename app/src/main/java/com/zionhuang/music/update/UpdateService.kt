package com.zionhuang.music.update

import android.app.DownloadManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zionhuang.music.BuildConfig
import com.zionhuang.music.R
import com.zionhuang.music.extensions.TAG
import com.zionhuang.music.extensions.div
import com.zionhuang.music.extensions.preference
import com.zionhuang.music.update.UpdateInfo.*
import com.zionhuang.music.utils.OkHttpDownloader
import com.zionhuang.music.utils.preference.PreferenceStore
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.Exception

class UpdateService : Service(), CoroutineScope by CoroutineScope(IO) {
    private val downloadManager by lazy { getSystemService<DownloadManager>()!! }
    private val preferenceStore by lazy { PreferenceStore.getInstance(this) }
    private val currentVersion = Version.parse(BuildConfig.VERSION_NAME)
    private var lastCheckTime by preference(R.string.pref_last_check_time, 0L)

    private val _updateInfoLiveData = MutableLiveData<UpdateInfo>(NotChecked)
    val updateInfoLiveData: LiveData<UpdateInfo> = _updateInfoLiveData

    private val _updateStatusLiveData: MutableLiveData<UpdateStatus> = MutableLiveData(UpdateStatus.Idle)
    val updateStatusLiveData: LiveData<UpdateStatus> = _updateStatusLiveData

    private suspend fun getLatestRelease(): Release = withContext(IO) {
        Gson().fromJson(OkHttpDownloader.downloadJson(RELEASE_API_URL), Array<Release>::class.java)[0]
    }

    fun checkForUpdate(force: Boolean = false) = launch {
        if (!force) {
            val lastCheckTime = Instant.ofEpochSecond(lastCheckTime)
            if (Instant.now() < lastCheckTime.plus(CHECK_FREQUENCY_DAY, ChronoUnit.DAYS)) return@launch
        }
        _updateInfoLiveData.postValue(Checking)
        try {
            val release = getLatestRelease()
            val latestVersion = Version.parse(release.name)
            Log.d(TAG, release.toString())
            lastCheckTime = Instant.now().epochSecond
            _updateInfoLiveData.postValue(if (currentVersion < latestVersion) UpdateAvailable(latestVersion) else UpToDate)
        } catch (e: Exception) {
            _updateInfoLiveData.postValue(Exception)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = UpdateBinder()

    internal inner class UpdateBinder : Binder() {
        val service: UpdateService get() = this@UpdateService
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    companion object {
        const val RELEASE_API_URL = "https://api.github.com/repos/z-huang/music/releases?per_page=1"
        const val APK_MIMETYPE = "application/vnd.android.package-archive"
        const val CHECK_FREQUENCY_DAY = 1L
        const val KEY_APK_FILENAME = "update_apk_filename"
        const val KEY_APK_DOWNLOAD_ID = "update_apk_download_id"
    }
}