package com.zionhuang.music.extractor;

import com.zionhuang.music.db.SongEntity;

import java.util.ArrayList;

import static com.zionhuang.music.extractor.YtStream.TYPE_AUDIO;
import static com.zionhuang.music.extractor.YtStream.TYPE_NORMAL;
import static com.zionhuang.music.extractor.YtStream.TYPE_VIDEO;

public class ExtractedInfo {
    private static final String TAG = "ExtractedInfo";
    public boolean success;
    public SongEntity song;
    public ErrorCode errorCode;
    public String errorMessage;
    public String id;
    public ArrayList<YtStream> formats;
    public ArrayList<YtStream> normalStreams;
    public ArrayList<YtStream> videoStreams;
    public ArrayList<YtStream> audioStreams;

    ExtractedInfo(String id) {
        this.id = id;
        success = true;
    }

    ExtractedInfo(ErrorCode errCode, String msg) {
        success = false;
        errorCode = errCode;
        errorMessage = msg;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setSong(SongEntity song) {
        this.song = song;
    }

    public SongEntity getSong() {
        return song;
    }

    public void setFormats(ArrayList<YtStream> formats) {
        this.formats = formats;
        normalStreams = new ArrayList<>();
        videoStreams = new ArrayList<>();
        audioStreams = new ArrayList<>();
        for (YtStream fmt : formats) {
            switch (fmt.mediaType) {
                case TYPE_NORMAL:
                    normalStreams.add(fmt);
                    break;
                case TYPE_AUDIO:
                    audioStreams.add(fmt);
                    break;
                case TYPE_VIDEO:
                    videoStreams.add(fmt);
                    break;
            }
        }
        if (normalStreams.size() > 0) {
            normalStreams.sort((o1, o2) -> o2.width - o1.width);
        }
        if (audioStreams.size() > 0) {
            audioStreams.sort((o1, o2) -> 0);
        }
        if (videoStreams.size() > 0) {
            videoStreams.sort((o1, o2) -> o2.width - o1.width);
        }
    }

    public boolean hasNormalStream() {
        return normalStreams.size() > 0;
    }

    public YtStream getNormalStream() {
        return normalStreams.size() > 0 ? normalStreams.get(0) : null;
    }

    public YtStream getVideoStream() {
        return videoStreams.size() > 0 ? videoStreams.get(0) : null;
    }

}
