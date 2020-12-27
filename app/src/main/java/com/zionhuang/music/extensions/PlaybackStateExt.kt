package com.zionhuang.music.extensions

import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.STATE_BUFFERING
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING

inline val PlaybackStateCompat.isPlaying
    get() = state == STATE_PLAYING || state == STATE_BUFFERING