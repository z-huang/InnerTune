package com.zionhuang.music.utils

import android.content.Context
import com.zionhuang.music.extensions.div
import java.io.File

fun getSongFile(context: Context, songId: String): File {
    val mediaDir = context.getExternalFilesDir(null)!! / "media"
    if (!mediaDir.isDirectory) mediaDir.mkdirs()
    return mediaDir / md5(songId)
}
