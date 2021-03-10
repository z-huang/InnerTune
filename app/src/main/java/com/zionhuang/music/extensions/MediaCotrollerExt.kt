package com.zionhuang.music.extensions

import android.support.v4.media.session.MediaControllerCompat
import androidx.core.os.bundleOf
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor.COMMAND_MOVE_QUEUE_ITEM
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_SEEK_TO_QUEUE_ITEM
import com.zionhuang.music.constants.MediaSessionConstants.EXTRA_MEDIA_ID

fun MediaControllerCompat.moveQueueItem(from: Int, to: Int) =
        sendCommand(COMMAND_MOVE_QUEUE_ITEM, bundleOf(
                TimelineQueueEditor.EXTRA_FROM_INDEX to from,
                TimelineQueueEditor.EXTRA_TO_INDEX to to
        ), null)

fun MediaControllerCompat.seekToQueueItem(mediaId: String) =
        sendCommand(COMMAND_SEEK_TO_QUEUE_ITEM, bundleOf(
                EXTRA_MEDIA_ID to mediaId
        ), null)