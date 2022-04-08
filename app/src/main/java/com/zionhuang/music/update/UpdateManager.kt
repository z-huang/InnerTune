package com.zionhuang.music.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.content.IntentFilter
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zionhuang.music.BuildConfig
import com.zionhuang.music.R
import com.zionhuang.music.extensions.div
import com.zionhuang.music.extensions.get
import com.zionhuang.music.utils.OkHttpDownloader
import com.zionhuang.music.utils.preference.Preference
import com.zionhuang.music.utils.preference.serializablePreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit

class UpdateManager(private val context: Context) {
    private val downloadManager by lazy { context.getSystemService<DownloadManager>()!! }
    private val currentVersion = Version.parse(BuildConfig.VERSION_NAME)
    private var lastCheckTime by Preference(context, R.string.pref_last_check_time, 0L)
    private var lastCheckInfo: UpdateInfo by serializablePreference(context, R.string.pref_last_check_info, UpdateInfo.NotChecked)

    private val _updateInfoLiveData = MutableLiveData(if (isLastCheckExpired) UpdateInfo.NotChecked else lastCheckInfo)
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
            val latestVersion = release.version
            lastCheckTime = Instant.now().epochSecond
            val updateInfo = if (currentVersion < latestVersion) UpdateInfo.UpdateAvailable(latestVersion) else UpdateInfo.UpToDate
            _updateInfoLiveData.postValue(updateInfo)
            lastCheckInfo = updateInfo
        } catch (e: Exception) {
            _updateInfoLiveData.postValue(UpdateInfo.Exception)
        }
    }

    suspend fun updateApp() {
        _updateStatusLiveData.postValue(UpdateStatus.Preparing)
        if (updateInfoLiveData.value !is UpdateInfo.UpdateAvailable) {
            _updateStatusLiveData.postValue(UpdateStatus.Idle)
            return
        }
        val release = getLatestRelease()
        val asset = release.assets.find { it.contentType == APK_MIMETYPE }
        if (asset == null) {
            _updateStatusLiveData.postValue(UpdateStatus.Idle)
            return
        }
        val dest = context.externalCacheDir!! / APK_FILENAME
        if (dest.exists()) dest.delete()
        val request = DownloadManager.Request(asset.downloadUrl.toUri())
            .setTitle("${context.getString(R.string.app_name)} ${release.version}")
            .setMimeType(APK_MIMETYPE)
            .setDestinationUri(dest.toUri())
        val id = downloadManager.enqueue(request)
        _updateStatusLiveData.postValue(UpdateStatus.Downloading(id))
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (id != intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)) return
                context.unregisterReceiver(this)
                downloadManager.query(DownloadManager.Query().setFilterById(id)).use { cursor ->
                    _updateStatusLiveData.postValue(UpdateStatus.Idle)
                    val isSuccess = cursor.moveToFirst() && cursor.get<Int>(DownloadManager.COLUMN_STATUS) == DownloadManager.STATUS_SUCCESSFUL
                    if (!isSuccess) return
                    val installIntent = Intent(ACTION_VIEW).apply {
                        setDataAndType(FileProvider.getUriForFile(context, "${context.applicationContext.packageName}.FileProvider", dest), APK_MIMETYPE)
                        addFlags(FLAG_ACTIVITY_NEW_TASK)
                        addFlags(FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(installIntent)
                }
            }
        }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    companion object {
        const val RELEASE_API_URL = "https://api.github.com/repos/z-huang/music/releases?per_page=1"
        const val APK_MIMETYPE = "application/vnd.android.package-archive"
        const val CHECK_FREQUENCY_DAY = 1L
        const val APK_FILENAME = "update.apk"
    }
}