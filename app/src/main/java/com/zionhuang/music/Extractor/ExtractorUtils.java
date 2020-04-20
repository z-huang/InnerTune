package com.zionhuang.music.Extractor;

import android.util.Log;

import com.google.gson.JsonObject;
import com.zionhuang.music.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

class ExtractorUtils {
    private static final String TAG = "ExtractorUtils";
    private static final ArrayList<String> VCODECS = new ArrayList<>(Arrays.asList("avc1", "avc2", "avc3", "avc4", "vp9", "vp8", "hev1", "hev2", "h263", "h264", "mp4v", "hvc1", "av01", "theora"));
    private static final ArrayList<String> ACODECS = new ArrayList<>(Arrays.asList("mp4a", "opus", "vorbis", "mp3", "aac", "ac-3", "ec-3", "eac3", "dtsc", "dtse", "dtsh", "dtsl"));
    private static final ArrayList<String> KNOWN_EXTENSIONS = new ArrayList<>(Arrays.asList("mp4", "m4a", "m4p", "m4b", "m4r", "m4v", "aac",
            "flv", "f4v", "f4a", "f4b",
            "webm", "ogg", "ogv", "oga", "ogx", "spx", "opus",
            "mkv", "mka", "mk3d",
            "avi", "divx",
            "mov",
            "asf", "wmv", "wma",
            "3gp", "3g2",
            "mp3",
            "flac",
            "ape",
            "wav",
            "f4f", "f4m", "m3u8", "smil"));

    static JsonObject parseCodecs(String codecs_str) {
        if (codecs_str == null) {
            return new JsonObject();
        }
        ArrayList<String> splited_codecs = new ArrayList<>();
        for (String s : Utils.strip(codecs_str.trim(), ",").split(",")) {
            if (s != null && s.length() > 0) {
                splited_codecs.add(s);
            }
        }
        String vcodec = null;
        String acodec = null;
        for (String full_codec : splited_codecs) {
            String codec = full_codec.split(".")[0];
            if (VCODECS.contains(codec)) {
                if (vcodec == null) {
                    vcodec = full_codec;
                }
            } else if (ACODECS.contains(codec)) {
                if (acodec == null) {
                    acodec = full_codec;
                }
            } else {
                Log.w(TAG, "Unknown codec " + full_codec);
            }
        }
        JsonObject res = new JsonObject();
        if (vcodec == null && acodec == null) {
            if (splited_codecs.size() == 2) {
                res.addProperty("vcodec", splited_codecs.get(0));
                res.addProperty("acodec", splited_codecs.get(1));
                return res;
            }
        } else {
            res.addProperty("vcodec", vcodec != null ? vcodec : "none");
            res.addProperty("acodec", acodec != null ? acodec : "none");
            return res;

        }
        return res;
    }

    static String mimetype2ext(String mt) {
        if (mt == null) {
            return null;
        }
        switch (mt) {
            case "audio/mp4":
                return "m4a";
            case "audio/mpeg":
                return "mp3";
        }
        String[] part = mt.split("/");
        String res = part[part.length - 1];
        res = res.split(";")[0].trim().toLowerCase();
        switch (res) {
            case "3gpp":
                return "3gp";
            case "smptett+xml":
                return "tt";
            case "ttaf+xml":
                return "dfxp";
            case "ttml+xml":
                return "ttml";
            case "x-flv":
                return "flv";
            case "x-mp4-fragmented":
                return "mp4";
            case "x-ms-sami":
                return "sami";
            case "x-ms-wmv":
                return "wmv";
            case "mpegurl":
                return "m3u8";
            case "x-mpegurl":
                return "m3u8";
            case "vnd.apple.mpegurl":
                return "m3u8";
            case "dash+xml":
                return "mpd";
            case "f4m+xml":
                return "f4m";
            case "hds+xml":
                return "f4m";
            case "vnd.ms-sstr+xml":
                return "ism";
            case "quicktime":
                return "mov";
            case "mp2t":
                return "ts";
            default:
                return res;
        }
    }

    static String determineExt(String url) {
        if (url == null || !url.contains(".")) {
            return "unknown_video";
        }
        String guess = Utils.rPartition(url.split("\\?", 2)[0], '.')[2];
        if (Pattern.compile("^[A-Za-z0-9]+$").matcher(guess).matches()) {
            return guess;
        } else if (KNOWN_EXTENSIONS.contains(Utils.rStrip(guess, "/"))) {
            return Utils.rStrip(guess, "/");
        } else {
            return "unknown_video";
        }
    }
}
