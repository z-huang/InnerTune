package com.zionhuang.music.download

import android.app.DownloadManager
import android.app.DownloadManager.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADED
import com.zionhuang.music.download.DownloadTask.Companion.STATE_NOT_DOWNLOADED
import com.zionhuang.music.extensions.get
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DownloadBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val downloadManager = context.getSystemService<DownloadManager>()!!
        val songRepository = SongRepository(context)

        when (intent.action) {
            ACTION_DOWNLOAD_COMPLETE -> {
                val id = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1)
                if (id == -1L) return
                downloadManager.query(Query().setFilterById(id)).use { cursor ->
                    val isSuccess =
                        cursor.moveToFirst() && cursor.get<Int>(COLUMN_STATUS) == STATUS_SUCCESSFUL
                    GlobalScope.launch(IO) {
                        val songId = songRepository.getSongIdByDownloadId(id)
                        songRepository.updateSongEntity(songId) {
                            downloadState =
                                if (isSuccess) STATE_DOWNLOADED else STATE_NOT_DOWNLOADED
                        }
                        songRepository.removeDownload(id)
                    }
                }
            }
        }
    }
}