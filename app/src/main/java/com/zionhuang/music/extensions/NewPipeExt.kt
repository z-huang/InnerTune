package com.zionhuang.music.extensions

import com.zionhuang.music.constants.MediaConstants
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.youtube.NewPipeYouTubeHelper
import org.schabi.newpipe.extractor.stream.StreamInfoItem

val StreamInfoItem.id: String
    get() = NewPipeYouTubeHelper.extractVideoId(url)!!

fun StreamInfoItem.toSong(): Song = Song(
    id = id,
    title = name,
    artistName = uploaderName,
    artworkType = if ("music.youtube.com" in url) MediaConstants.TYPE_SQUARE else MediaConstants.TYPE_RECTANGLE,
    duration = duration.toInt()
)
