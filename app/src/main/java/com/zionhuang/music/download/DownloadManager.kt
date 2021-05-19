package com.zionhuang.music.download

import android.app.DownloadManager
import android.content.Context
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.downloader.Status.*
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADED
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADING
import com.zionhuang.music.extensions.audioDir
import com.zionhuang.music.extensions.div
import com.zionhuang.music.extensions.getArtworkFile
import com.zionhuang.music.models.AssetDownloadMission
import com.zionhuang.music.models.AssetDownloadMission.Companion.ASSET_CHANNEL
import com.zionhuang.music.utils.OkHttpDownloader
import com.zionhuang.music.utils.OkHttpDownloader.requestOf
import com.zionhuang.music.youtube.newpipe.ExtractorHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

typealias DownloadListener = (DownloadTask) -> Unit

class DownloadManager(private val context: Context, private val scope: CoroutineScope) {
    private val songRepository = SongRepository(context)

    fun addMusicDownload(task: DownloadTask) {
        scope.launch {
            if (songRepository.getSongById(task.id)!!.downloadState == STATE_DOWNLOADED) return@launch
            songRepository.updateSongEntity(task.id) {
                downloadState = STATE_DOWNLOADING
            }
            val stream = ExtractorHelper.getStreamInfo(task.id)
            task.apply {
                title = stream.name
            }
            stream.thumbnailUrl?.let {
                OkHttpDownloader.downloadFile(requestOf(it), context.getArtworkFile(task.id))
            }
            val downloadManager = context.getSystemService<DownloadManager>()!!
            val req =
                DownloadManager.Request(stream.videoStreams.maxByOrNull { it.width }?.url?.toUri())
                    .setTitle(stream.name)
                    .setDestinationUri((context.audioDir / stream.id).toUri())
                    .setVisibleInDownloadsUi(false)
            val id = downloadManager.enqueue(req)
            songRepository.addDownload(id, task.id)
        }
    }

    fun addAssetDownload(task: AssetDownloadMission) {
        when (task.type) {
            ASSET_CHANNEL -> {

            }
        }
    }
}