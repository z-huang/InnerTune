package com.zionhuang.music.models;

import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.Collections;
import java.util.List;

public class QueueData {
    private String queueTitle = "";
    private List<MediaSessionCompat.QueueItem> queueItemList = Collections.emptyList();
    private long currentIndex = -1;

    public QueueData update(MediaControllerCompat controller, List<MediaSessionCompat.QueueItem> list) {
        queueTitle = controller.getQueueTitle().toString();
        queueItemList = list;
        return this;
    }
}
