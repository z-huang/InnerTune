package com.zionhuang.music.playback

import android.app.Notification
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.ext.workmanager.WorkManagerScheduler
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Requirements
import com.google.android.exoplayer2.scheduler.Requirements.NETWORK_UNMETERED
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.zionhuang.music.R
import java.io.File

class YtDownloadService : DownloadService(
        NOTIFICATION_ID,
        DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
        CHANNEL_ID,
        R.string.channel_name_download,
        0
) {
    companion object {
        const val NOTIFICATION_ID = 999
        const val CHANNEL_ID = "music_channel_02"
    }

    override fun getDownloadManager(): DownloadManager {
        val dataSourceFactory = DefaultDataSourceFactory(this)
        val databaseProvider = ExoDatabaseProvider(this)
        val downloadContentDirectory = File(getExternalFilesDir(null), getString(R.string.app_name))
        val downloadCache = SimpleCache(downloadContentDirectory, NoOpCacheEvictor(), databaseProvider)
        return DownloadManager(
                this,
                databaseProvider,
                downloadCache,
                dataSourceFactory,
                Runnable::run)
                .apply {
                    requirements = Requirements(NETWORK_UNMETERED)
                    maxParallelDownloads = 5
                }
    }

    override fun getScheduler(): Scheduler = WorkManagerScheduler(this, "Music Downloader")

    override fun getForegroundNotification(downloads: MutableList<Download>): Notification =
            DownloadNotificationHelper(this, CHANNEL_ID)
                    .buildProgressNotification(
                            applicationContext,
                            R.drawable.ic_library_music_black_24dp,
                            null,
                            null,
                            downloads)
}