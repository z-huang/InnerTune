package com.zionhuang.music.extractor;

public class YtStream {
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_AUDIO = 1;
    public static final int TYPE_VIDEO = 2;

    public String formatId;
    public String protocol;
    public String url;
    public int mediaType;
    public String ext;
    public int width;
    public int height;
    public float stretchedRatio;
    public String formatNote;
    public int fps;
    public String aCodec;
    public String vCodec;
    public float tbr; // bitrate of audio and video (KBit/s)
    public float abr; // audio bitrate (KBit/s)
    public float asr; // audio sampling rate (Hz)
    public float vbr; // video bitrate (KBit/s)
    public int fileSize;

    YtStream(JSON stream) {
        formatId = stream.getString("format_id");
        protocol = stream.getString("protocol");
        url = stream.getString("url");
        mediaType = stream.getInt("mediaType");
        ext = stream.getString("ext");
        width = stream.getInt("width");
        height = stream.getInt("height");
        formatNote = stream.getString("format_note");
        fps = stream.getInt("fps");
        aCodec = stream.getString("acodec");
        vCodec = stream.getString("vcodec");
        abr = stream.getFloat("abr");
        vbr = stream.getFloat("vbr");
        tbr = stream.getFloat("tbr");
        asr = stream.getFloat("asr");
        fileSize = stream.getInt("filesize");
        if (tbr == 0f) {
            tbr = abr + vbr;
        }
        if (ext.isEmpty()) {
            ext = ExtractorUtils.determineExt(url);
        }
    }
}
