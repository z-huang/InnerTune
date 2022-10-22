package com.zionhuang.music.lyrics

import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.WatchEndpoint

object YouTubeLyricsProvider : LyricsProvider {
    override suspend fun getLyrics(id: String, title: String, artist: String, duration: Int): Result<String> =
        YouTube.next(WatchEndpoint(videoId = id)).mapCatching { nextResult ->
            YouTube.browse(nextResult.lyricsEndpoint ?: throw IllegalStateException("Lyrics endpoint not found")).getOrThrow()
        }.mapCatching { browseResult ->
            browseResult.lyrics ?: throw IllegalStateException("Lyrics unavailable")
        }

}