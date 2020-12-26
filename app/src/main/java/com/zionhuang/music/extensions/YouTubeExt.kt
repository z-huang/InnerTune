package com.zionhuang.music.extensions

import com.zionhuang.music.youtube.extractors.YouTubeSearchExtractor
import com.zionhuang.music.youtube.models.YouTubeSearch

suspend fun YouTubeSearch.Success.getNextPage() = YouTubeSearchExtractor.search(query, nextPageToken)