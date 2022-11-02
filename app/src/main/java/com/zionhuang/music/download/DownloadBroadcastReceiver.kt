package com.zionhuang.music.download

import android.app.DownloadManager
import android.app.DownloadManager.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import com.zionhuang.music.extensions.get
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DownloadBroadcastReceiver : BroadcastReceiver() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        val downloadManager = context.getSystemService<DownloadManager>()!!
        val songRepository = SongRepository(context)

        when (intent.action) {
            ACTION_DOWNLOAD_COMPLETE -> {
                val id = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1)
                if (id == -1L) return
                downloadManager.query(Query().setFilterById(id)).use { cursor ->
                    val success = cursor.moveToFirst() && cursor.get<Int>(COLUMN_STATUS) == STATUS_SUCCESSFUL
                    GlobalScope.launch(IO) {
                        songRepository.onDownloadComplete(id, success)
                    }
                }
            }
        }
    }
}