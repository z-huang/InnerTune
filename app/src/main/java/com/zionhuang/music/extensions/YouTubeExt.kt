package com.zionhuang.music.extensions

import com.zionhuang.music.extractor.ytextractors.YouTubeSearchExtractor
import com.zionhuang.music.extractor.models.YouTubeSearch

suspend fun YouTubeSearch.Success.getNextPage() = YouTubeSearchExtractor.search(query, nextPageToken)