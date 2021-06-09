package com.zionhuang.music.extensions

import com.zionhuang.music.constants.MediaConstants
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.youtube.newpipe.ExtractorHelper
import org.schabi.newpipe.extractor.stream.StreamInfoItem

fun StreamInfoItem.toSong(): Song = Song(
    songId = ExtractorHelper.extractVideoId(url),
    title = name,
    artistName = uploaderName,
    artworkType = if ("music.youtube.com" in url) MediaConstants.TYPE_SQUARE else MediaConstants.TYPE_RECTANGLE,
    duration = duration.toInt()
)
