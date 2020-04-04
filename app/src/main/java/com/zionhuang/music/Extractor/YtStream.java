package com.zionhuang.music.Extractor;

import com.google.gson.JsonObject;

public class YtStream {
    public String formatId;
    public String protocol;
    public String url;
    public String mediaType;
    public String ext;
    public int width;
    public int height;
    public float stretchedRatio;
    public String formatNote;
    public int fps;
    public String acodec;
    public String vcodec;
    public float abr;
    public float vbr;
    public float tbr;
    public float asr;
    public int filesize;

    YtStream(JsonObject stream) {
        formatId = stream.has("format_id") ? stream.get("format_id").getAsString() : null;
        protocol = stream.has("protocol") ? stream.get("protocol").getAsString() : null;
        url = stream.has("url") ? stream.get("url").getAsString() : null;
        mediaType = stream.has("mediatype") ? stream.get("mediatype").getAsString() : null;
        ext = stream.has("ext") ? stream.get("ext").getAsString() : null;
        width = stream.has("width") ? stream.get("width").getAsInt() : 0;
        height = stream.has("height") ? stream.get("height").getAsInt() : 0;
        formatNote = stream.has("format_note") ? stream.get("format_note").getAsString() : null;
        fps = stream.has("fps") ? stream.get("fps").getAsInt() : 0;
        acodec = stream.has("acodec") ? stream.get("acodec").getAsString() : null;
        vcodec = stream.has("vcodec") ? stream.get("vcodec").getAsString() : null;
        abr = stream.has("abr") ? stream.get("abr").getAsFloat() : 0;
        vbr = stream.has("vbr") ? stream.get("vbr").getAsFloat() : 0;
        tbr = stream.has("tbr") ? stream.get("tbr").getAsFloat() : 0;
        asr = stream.has("asr") ? stream.get("asr").getAsFloat() : 0;
        filesize = stream.has("filesize") ? stream.get("filesize").getAsInt() : 0;
        if (tbr == 0f) {
            tbr = abr + vbr;
        }
        if (ext == null) {
            ext = ExtractorUtils.determineExt(url);
        }
    }


}
