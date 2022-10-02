package com.zionhuang.music.extensions

import android.support.v4.media.MediaDescriptionCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator

fun MediaSessionConnector.setQueueNavigator(getMediaDescription: (player: Player, windowIndex: Int) -> MediaDescriptionCompat) = setQueueNavigator(object : TimelineQueueNavigator(mediaSession, Int.MAX_VALUE) {
    override fun getMediaDescription(player: Player, windowIndex: Int) = getMediaDescription(player, windowIndex)
})
