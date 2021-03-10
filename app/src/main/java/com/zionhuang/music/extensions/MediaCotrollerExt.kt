package com.zionhuang.music.extensions

import android.support.v4.media.session.MediaControllerCompat
import androidx.core.os.bundleOf
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor.COMMAND_MOVE_QUEUE_ITEM

fun MediaControllerCompat.moveQueueItem(from: Int, to: Int) =
        sendCommand(COMMAND_MOVE_QUEUE_ITEM, bundleOf(
                TimelineQueueEditor.EXTRA_FROM_INDEX to from,
                TimelineQueueEditor.EXTRA_TO_INDEX to to
        ), null)