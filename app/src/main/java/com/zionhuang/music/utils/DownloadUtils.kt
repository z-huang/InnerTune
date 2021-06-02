package com.zionhuang.music.utils

import android.app.DownloadManager
import android.content.Context
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.zionhuang.music.constants.MediaConstants.STATE_DOWNLOADED
import com.zionhuang.music.constants.MediaConstants.STATE_DOWNLOADING
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.extensions.audioDir
import com.zionhuang.music.extensions.div
import com.zionhuang.music.extensions.getArtworkFile
import com.zionhuang.music.youtube.newpipe.ExtractorHelper

suspend fun Context.downloadSong(mediaId: String, songRepository: SongRepository) {
    if (songRepository.getSongById(mediaId)!!.downloadState == STATE_DOWNLOADED) return
    songRepository.updateSongEntity(mediaId) {
        downloadState = STATE_DOWNLOADING
    }
    val stream = ExtractorHelper.getStreamInfo(mediaId)
    stream.thumbnailUrl?.let {
        OkHttpDownloader.downloadFile(
            OkHttpDownloader.requestOf(it),
            getArtworkFile(mediaId)
        )
    }
    val downloadManager = getSystemService<DownloadManager>()!!
    val req = DownloadManager.Request(stream.videoStreams.maxByOrNull { it.width }?.url?.toUri())
        .setTitle(stream.name)
        .setDestinationUri((audioDir / stream.id).toUri())
        .setVisibleInDownloadsUi(false)
    val id = downloadManager.enqueue(req)
    songRepository.addDownload(id, mediaId)
}
