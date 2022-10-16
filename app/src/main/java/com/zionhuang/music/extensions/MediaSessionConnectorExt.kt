package com.zionhuang.music.extensions

import android.support.v4.media.MediaDescriptionCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator

fun MediaSessionConnector.setQueueNavigator(getMediaDescription: (player: Player, windowIndex: Int) -> MediaDescriptionCompat) = setQueueNavigator(object : TimelineQueueNavigator(mediaSession, Int.MAX_VALUE) {
    override fun getMediaDescription(player: Player, windowIndex: Int) = getMediaDescription(player, windowIndex)
    override fun onSkipToPrevious(player: Player) {
        super.onSkipToPrevious(player)
        if (player.playerError != null) {
            player.prepare()
        }
    }

    override fun onSkipToNext(player: Player) {
        super.onSkipToNext(player)
        if (player.playerError != null) {
            player.prepare()
        }
    }

    override fun onSkipToQueueItem(player: Player, id: Long) {
        super.onSkipToQueueItem(player, id)
        if (player.playerError != null) {
            player.prepare()
        }
    }
})
