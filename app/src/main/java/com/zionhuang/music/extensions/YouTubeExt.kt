package com.zionhuang.music.extensions

import com.google.api.services.youtube.model.ThumbnailDetails
import com.zionhuang.music.youtube.extractors.YouTubeSearchExtractor
import com.zionhuang.music.youtube.models.YouTubeSearch

suspend fun YouTubeSearch.Success.getNextPage() = YouTubeSearchExtractor.search(query, nextPageToken)

val ThumbnailDetails.maxResUrl: String?
    get() = (maxres ?: high ?: medium ?: standard ?: default)?.url
