package com.zionhuang.music.extensions

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator

typealias OnCustomAction = (Player, ControlDispatcher, action: String, extras: Bundle?) -> Unit

fun Context.createCustomAction(action: String, @StringRes name: Int, @DrawableRes icon: Int, permitted: () -> Boolean = { true }, onCustomAction: OnCustomAction) = object : MediaSessionConnector.CustomActionProvider {
    override fun onCustomAction(player: Player, controlDispatcher: ControlDispatcher, action: String, extras: Bundle?) = onCustomAction(player, controlDispatcher, action, extras)
    override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? = if (permitted()) PlaybackStateCompat.CustomAction.Builder(action, getString(name), icon).build() else null
}

fun MediaSessionConnector.setQueueNavigator(getMediaDescription: (player: Player, windowIndex: Int) -> MediaDescriptionCompat) = setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
    override fun getMediaDescription(player: Player, windowIndex: Int) = getMediaDescription(player, windowIndex)
})
