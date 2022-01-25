package com.zionhuang.music.extensions

import com.google.api.services.youtube.model.ThumbnailDetails

val ThumbnailDetails.maxResUrl: String?
    get() = (maxres ?: high ?: medium ?: standard ?: default)?.url
