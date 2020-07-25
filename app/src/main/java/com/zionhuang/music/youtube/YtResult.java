package com.zionhuang.music.youtube;

import java.util.List;

public class YtResult {
    public List<YtItem.BaseItem> list;
    public String nextPageToken;

    YtResult(List<YtItem.BaseItem> list, String nextPageToken) {
        this.list = list;
        this.nextPageToken = nextPageToken;
    }
}
