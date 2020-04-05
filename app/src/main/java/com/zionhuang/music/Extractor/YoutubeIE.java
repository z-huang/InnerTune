package com.zionhuang.music.Extractor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.zionhuang.music.utils.Utils;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

public class YoutubeIE {
    public enum ErrorCode {
        NODATA, RENTAL, RTMPE, DRM, JSINTERPRETERROR, UNKNOWN
    }

    public static class ExtractException extends java.lang.Exception {
        private ErrorCode errorCode;

        ExtractException(ErrorCode errCode, String msg) {
            super(msg);
            errorCode = errCode;
        }

        ExtractException(ErrorCode errCode) {
            errorCode = errCode;
        }

        ExtractException(String msg) {
            super(msg);
        }

        ErrorCode getErrorCode() {
            return errorCode;
        }
    }

    public static class Result {
        public boolean success;
        public ErrorCode errorCode;
        public String errorMessage;
        public ArrayList<YtStream> formats;
        public ArrayList<YtStream> normalStreams;
        public ArrayList<YtStream> videoStreams;
        public ArrayList<YtStream> audioStreams;

        Result(ErrorCode errCode, String msg) {
            success = false;
            errorCode = errCode;
            errorMessage = msg;
        }

        Result(ArrayList<YtStream> fmts) {
            success = true;
            formats = fmts;
            normalStreams = new ArrayList<>();
            videoStreams = new ArrayList<>();
            audioStreams = new ArrayList<>();
            for (YtStream fmt : formats) {
                if (fmt.mediaType.equals("normal")) {
                    normalStreams.add(fmt);
                } else if (fmt.mediaType.equals("video")) {
                    videoStreams.add(fmt);
                } else {
                    audioStreams.add(fmt);
                }
            }
            if (normalStreams.size() > 0) {
                normalStreams.sort(new Comparator<YtStream>() {
                    @Override
                    public int compare(YtStream o1, YtStream o2) {
                        return o2.width - o1.width;
                    }
                });
            }
            if (audioStreams.size() > 0) {
                audioStreams.sort(new Comparator<YtStream>() {
                    @Override
                    public int compare(YtStream o1, YtStream o2) {
                        return 0;
                    }
                });
            }
            if (videoStreams.size() > 0) {
                videoStreams.sort(new Comparator<YtStream>() {
                    @Override
                    public int compare(YtStream o1, YtStream o2) {
                        return o2.width - o1.width;
                    }
                });
            }
        }

        public boolean hasNormalStream() {
            return normalStreams.size() > 0;
        }

        public YtStream getBestNormal() {
            return normalStreams.size() > 0 ? normalStreams.get(0) : null;
        }

        public YtStream getWorstNormal() {
            return normalStreams.size() > 0 ? normalStreams.get(normalStreams.size() - 1) : null;
        }

        public YtStream getBestVideo() {
            return videoStreams.size() > 0 ? videoStreams.get(0) : null;
        }

        public YtStream getWorstVideo() {
            return videoStreams.size() > 0 ? videoStreams.get(videoStreams.size() - 1) : null;
        }

        public YtStream getBestAudio() {
            return audioStreams.size() > 0 ? audioStreams.get(0) : null;
        }

        public YtStream getWorstAudio() {
            return audioStreams.size() > 0 ? audioStreams.get(audioStreams.size() - 1) : null;
        }
    }

    private static JsonObject formats = JsonParser.parseString("{\"5\":{\"ext\":\"flv\",\"width\":400,\"height\":240,\"acodec\":\"mp3\",\"abr\":64,\"vcodec\":\"h263\"},\"6\":{\"ext\":\"flv\",\"width\":450,\"height\":270,\"acodec\":\"mp3\",\"abr\":64,\"vcodec\":\"h263\"},\"13\":{\"ext\":\"3gp\",\"acodec\":\"aac\",\"vcodec\":\"mp4v\"},\"17\":{\"ext\":\"3gp\",\"width\":176,\"height\":144,\"acodec\":\"aac\",\"abr\":24,\"vcodec\":\"mp4v\"},\"18\":{\"ext\":\"mp4\",\"width\":640,\"height\":360,\"acodec\":\"aac\",\"abr\":96,\"vcodec\":\"h264\"},\"22\":{\"ext\":\"mp4\",\"width\":1280,\"height\":720,\"acodec\":\"aac\",\"abr\":192,\"vcodec\":\"h264\"},\"34\":{\"ext\":\"flv\",\"width\":640,\"height\":360,\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\"},\"35\":{\"ext\":\"flv\",\"width\":854,\"height\":480,\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\"},\"36\":{\"ext\":\"3gp\",\"width\":320,\"acodec\":\"aac\",\"vcodec\":\"mp4v\"},\"37\":{\"ext\":\"mp4\",\"width\":1920,\"height\":1080,\"acodec\":\"aac\",\"abr\":192,\"vcodec\":\"h264\"},\"38\":{\"ext\":\"mp4\",\"width\":4096,\"height\":3072,\"acodec\":\"aac\",\"abr\":192,\"vcodec\":\"h264\"},\"43\":{\"ext\":\"webm\",\"width\":640,\"height\":360,\"acodec\":\"vorbis\",\"abr\":128,\"vcodec\":\"vp8\"},\"44\":{\"ext\":\"webm\",\"width\":854,\"height\":480,\"acodec\":\"vorbis\",\"abr\":128,\"vcodec\":\"vp8\"},\"45\":{\"ext\":\"webm\",\"width\":1280,\"height\":720,\"acodec\":\"vorbis\",\"abr\":192,\"vcodec\":\"vp8\"},\"46\":{\"ext\":\"webm\",\"width\":1920,\"height\":1080,\"acodec\":\"vorbis\",\"abr\":192,\"vcodec\":\"vp8\"},\"59\":{\"ext\":\"mp4\",\"width\":854,\"height\":480,\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\"},\"78\":{\"ext\":\"mp4\",\"width\":854,\"height\":480,\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\"},\"82\":{\"ext\":\"mp4\",\"height\":360,\"format_note\":\"3D\",\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\",\"preference\":-20},\"83\":{\"ext\":\"mp4\",\"height\":480,\"format_note\":\"3D\",\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\",\"preference\":-20},\"84\":{\"ext\":\"mp4\",\"height\":720,\"format_note\":\"3D\",\"acodec\":\"aac\",\"abr\":192,\"vcodec\":\"h264\",\"preference\":-20},\"85\":{\"ext\":\"mp4\",\"height\":1080,\"format_note\":\"3D\",\"acodec\":\"aac\",\"abr\":192,\"vcodec\":\"h264\",\"preference\":-20},\"100\":{\"ext\":\"webm\",\"height\":360,\"format_note\":\"3D\",\"acodec\":\"vorbis\",\"abr\":128,\"vcodec\":\"vp8\",\"preference\":-20},\"101\":{\"ext\":\"webm\",\"height\":480,\"format_note\":\"3D\",\"acodec\":\"vorbis\",\"abr\":192,\"vcodec\":\"vp8\",\"preference\":-20},\"102\":{\"ext\":\"webm\",\"height\":720,\"format_note\":\"3D\",\"acodec\":\"vorbis\",\"abr\":192,\"vcodec\":\"vp8\",\"preference\":-20},\"91\":{\"ext\":\"mp4\",\"height\":144,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":48,\"vcodec\":\"h264\",\"preference\":-10},\"92\":{\"ext\":\"mp4\",\"height\":240,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":48,\"vcodec\":\"h264\",\"preference\":-10},\"93\":{\"ext\":\"mp4\",\"height\":360,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\",\"preference\":-10},\"94\":{\"ext\":\"mp4\",\"height\":480,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\",\"preference\":-10},\"95\":{\"ext\":\"mp4\",\"height\":720,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":256,\"vcodec\":\"h264\",\"preference\":-10},\"96\":{\"ext\":\"mp4\",\"height\":1080,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":256,\"vcodec\":\"h264\",\"preference\":-10},\"132\":{\"ext\":\"mp4\",\"height\":240,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":48,\"vcodec\":\"h264\",\"preference\":-10},\"151\":{\"ext\":\"mp4\",\"height\":72,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":24,\"vcodec\":\"h264\",\"preference\":-10},\"133\":{\"ext\":\"mp4\",\"height\":240,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"134\":{\"ext\":\"mp4\",\"height\":360,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"135\":{\"ext\":\"mp4\",\"height\":480,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"136\":{\"ext\":\"mp4\",\"height\":720,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"137\":{\"ext\":\"mp4\",\"height\":1080,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"138\":{\"ext\":\"mp4\",\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"160\":{\"ext\":\"mp4\",\"height\":144,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"212\":{\"ext\":\"mp4\",\"height\":480,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"264\":{\"ext\":\"mp4\",\"height\":1440,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"298\":{\"ext\":\"mp4\",\"height\":720,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\",\"fps\":60},\"299\":{\"ext\":\"mp4\",\"height\":1080,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\",\"fps\":60},\"266\":{\"ext\":\"mp4\",\"height\":2160,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"139\":{\"ext\":\"m4a\",\"format_note\":\"DASHaudio\",\"acodec\":\"aac\",\"abr\":48,\"container\":\"m4a_dash\"},\"140\":{\"ext\":\"m4a\",\"format_note\":\"DASHaudio\",\"acodec\":\"aac\",\"abr\":128,\"container\":\"m4a_dash\"},\"141\":{\"ext\":\"m4a\",\"format_note\":\"DASHaudio\",\"acodec\":\"aac\",\"abr\":256,\"container\":\"m4a_dash\"},\"256\":{\"ext\":\"m4a\",\"format_note\":\"DASHaudio\",\"acodec\":\"aac\",\"container\":\"m4a_dash\"},\"258\":{\"ext\":\"m4a\",\"format_note\":\"DASHaudio\",\"acodec\":\"aac\",\"container\":\"m4a_dash\"},\"325\":{\"ext\":\"m4a\",\"format_note\":\"DASHaudio\",\"acodec\":\"dtse\",\"container\":\"m4a_dash\"},\"328\":{\"ext\":\"m4a\",\"format_note\":\"DASHaudio\",\"acodec\":\"ec-3\",\"container\":\"m4a_dash\"},\"167\":{\"ext\":\"webm\",\"height\":360,\"width\":640,\"format_note\":\"DASHvideo\",\"container\":\"webm\",\"vcodec\":\"vp8\"},\"168\":{\"ext\":\"webm\",\"height\":480,\"width\":854,\"format_note\":\"DASHvideo\",\"container\":\"webm\",\"vcodec\":\"vp8\"},\"169\":{\"ext\":\"webm\",\"height\":720,\"width\":1280,\"format_note\":\"DASHvideo\",\"container\":\"webm\",\"vcodec\":\"vp8\"},\"170\":{\"ext\":\"webm\",\"height\":1080,\"width\":1920,\"format_note\":\"DASHvideo\",\"container\":\"webm\",\"vcodec\":\"vp8\"},\"218\":{\"ext\":\"webm\",\"height\":480,\"width\":854,\"format_note\":\"DASHvideo\",\"container\":\"webm\",\"vcodec\":\"vp8\"},\"219\":{\"ext\":\"webm\",\"height\":480,\"width\":854,\"format_note\":\"DASHvideo\",\"container\":\"webm\",\"vcodec\":\"vp8\"},\"278\":{\"ext\":\"webm\",\"height\":144,\"format_note\":\"DASHvideo\",\"container\":\"webm\",\"vcodec\":\"vp9\"},\"242\":{\"ext\":\"webm\",\"height\":240,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"243\":{\"ext\":\"webm\",\"height\":360,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"244\":{\"ext\":\"webm\",\"height\":480,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"245\":{\"ext\":\"webm\",\"height\":480,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"246\":{\"ext\":\"webm\",\"height\":480,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"247\":{\"ext\":\"webm\",\"height\":720,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"248\":{\"ext\":\"webm\",\"height\":1080,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"271\":{\"ext\":\"webm\",\"height\":1440,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"272\":{\"ext\":\"webm\",\"height\":2160,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"302\":{\"ext\":\"webm\",\"height\":720,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\",\"fps\":60},\"303\":{\"ext\":\"webm\",\"height\":1080,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\",\"fps\":60},\"308\":{\"ext\":\"webm\",\"height\":1440,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\",\"fps\":60},\"313\":{\"ext\":\"webm\",\"height\":2160,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"315\":{\"ext\":\"webm\",\"height\":2160,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\",\"fps\":60},\"171\":{\"ext\":\"webm\",\"acodec\":\"vorbis\",\"format_note\":\"DASHaudio\",\"abr\":128},\"172\":{\"ext\":\"webm\",\"acodec\":\"vorbis\",\"format_note\":\"DASHaudio\",\"abr\":256},\"249\":{\"ext\":\"webm\",\"format_note\":\"DASHaudio\",\"acodec\":\"opus\",\"abr\":50},\"250\":{\"ext\":\"webm\",\"format_note\":\"DASHaudio\",\"acodec\":\"opus\",\"abr\":70},\"251\":{\"ext\":\"webm\",\"format_note\":\"DASHaudio\",\"acodec\":\"opus\",\"abr\":160},\"_rtmp\":{\"protocol\":\"rtmp\"},\"394\":{\"acodec\":\"none\",\"vcodec\":\"av01.0.05M.08\"},\"395\":{\"acodec\":\"none\",\"vcodec\":\"av01.0.05M.08\"},\"396\":{\"acodec\":\"none\",\"vcodec\":\"av01.0.05M.08\"},\"397\":{\"acodec\":\"none\",\"vcodec\":\"av01.0.05M.08\"}}").getAsJsonObject();
    private static String[] subtitleFormats = {"srv1", "srv2", "srv3", "ttml", "vtt"};
    private HashMap<String, SignatureFunction> playerCache;

    private void addDashMpd() {
        if (video_info == null) {
            return;
        }
        if (video_info.has("dashmpd")) {
            String dashmpd = video_info.get("dashmpd").getAsString();
            if (!dash_mpds.contains(dashmpd)) {
                dash_mpds.add(dashmpd);
            }
        }
    }

    private void addDashMpdPr() {
        if (player_response == null) {
            return;
        }
        if (player_response.has("streamingData")) {
            JsonObject streamingData = player_response.get("streamingData").getAsJsonObject();
            if (streamingData.has("dashManifestUrl")) {
                String dashmpd = streamingData.get("dashManifestUrl").getAsString();
                if (!dash_mpds.contains(dashmpd)) {
                    dash_mpds.add(dashmpd);
                }
            }
        }
    }

    private JsonObject getYtplayerConfig(String video_webpage) {
        final String[] patterns = {";ytplayer\\.config\\s*=\\s*(\\{.+?\\});ytplayer", ";ytplayer\\.config\\s*=\\s*(\\{.+?\\});"};
        String config = Utils.searchRegex(patterns, video_webpage);
        if (config.length() == 0) {
            return null;
        }
        String pl_response = null;
        Matcher m = Pattern.compile(",\"player_response\":\"(\\{.+\\})\"").matcher(config);
        if (m.find()) {
            pl_response = unescapeJava(m.group(1));
            config = config.replaceAll(",\"player_response\":\"(\\{.+\\})\"", "");
        }
        JsonObject ytplayer_config = Utils.parseJsonString(config).getAsJsonObject();
        player_response = extractPlayerResponse(pl_response);
        return ytplayer_config;
    }

    private JsonObject extractPlayerResponse(String player_response) {
        if (player_response == null) {
            return null;
        }
        return Utils.parseJsonString(player_response).getAsJsonObject();
    }

    private String signatureCacheId(String example_sig) {
        return StringUtils.join(example_sig.split("."), ".");
    }

    private SignatureFunction parseSigJs(String jscode) throws JSInterpreter.InterpretException {
        final String[] JS_FN_RE = {"\\b[cs]\\s*&&\\s*[adf]\\.set\\([^,]+\\s*,\\s*encodeURIComponent\\s*\\(\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
                "\\b[a-zA-Z0-9]+\\s*&&\\s*[a-zA-Z0-9]+\\.set\\([^,]+\\s*,\\s*encodeURIComponent\\s*\\(\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
                "\\b(?<sig>[a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\\\"\\\"\\s*\\)",
                "(?<sig>[a-zA-Z0-9$]+)\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\\\"\\\"\\s*\\)",
                "([\"\\'])signature\\1\\s*,\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
                "\\.sig\\|\\|(?<sig>[a-zA-Z0-9$]+)\\(",
                "yt\\.akamaized\\.net/\\)\\s*\\|\\|\\s*.*?\\s*[cs]\\s*&&\\s*[adf]\\.set\\([^,]+\\s*,\\s*(?:encodeURIComponent\\s*\\()?\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
                "\\b[cs]\\s*&&\\s*[adf]\\.set\\([^,]+\\s*,\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
                "\\b[a-zA-Z0-9]+\\s*&&\\s*[a-zA-Z0-9]+\\.set\\([^,]+\\s*,\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
                "\\bc\\s*&&\\s*a\\.set\\([^,]+\\s*,\\s*\\([^)]*\\)\\s*\\(\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
                "\\bc\\s*&&\\s*[a-zA-Z0-9]+\\.set\\([^,]+\\s*,\\s*\\([^)]*\\)\\s*\\(\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
                "\\bc\\s*&&\\s*[a-zA-Z0-9]+\\.set\\([^,]+\\s*,\\s*\\([^)]*\\)\\s*\\(\\s*(?<sig>[a-zA-Z0-9$]+)\\("};
        final String funcname = Utils.searchRegex(JS_FN_RE, jscode, "sig");
        JSInterpreter jsi = new JSInterpreter(jscode);
        final JsonFunction initialFn = jsi.extractFunction(funcname);
        return new SignatureFunction() {
            @Override
            public String apply(String s) throws JSInterpreter.InterpretException {
                JsonArray parameters = new JsonArray();
                parameters.add(s);
                JsonElement res;
                res = initialFn.apply(parameters);
                return res.getAsString();
            }
        };
    }

    private SignatureFunction extractSignatureFunction(String player_url, String example_sig) throws ExtractException {
        Matcher id_m = Pattern.compile(".*?[-.](?<id>[a-zA-Z0-9_-]+)(?:/watch_as3|/html5player(?:-new)?|(?:/[a-z]{2,3}_[A-Z]{2})?/base)?\\.(?<ext>[a-z]+)$").matcher(player_url);
        if (!id_m.find()) {
            throw new ExtractException("Cannot identify player " + player_url);
        }
        String player_type = id_m.group("ext");
        String player_id = id_m.group("id");

        String func_id = player_type + "_" + player_id + signatureCacheId(example_sig);
        // TODO: load function from cache

        SignatureFunction res = null;
        if (player_type != null && player_type.equals("js")) {
            String code = Utils.downloadPlainText(player_url);
            try {
                res = parseSigJs(code);
            } catch (JSInterpreter.InterpretException e) {
                throw new ExtractException(ErrorCode.JSINTERPRETERROR, e.getMessage());
            }
        } else if (player_type != null && player_type.equals("swf")) {
            // TODO: swf interpreter
            throw new NotImplementedException("SWF interpreter not implemented");
        } else {
            throw new ExtractException("invalid player type " + player_type);
        }
        return res;
    }

    private String decryptSignature(String s, String player_url) throws ExtractException {
        if (player_url == null) {
            throw new ExtractException("Cannot decrypt signature without player_url");
        }

        if (player_url.startsWith("//")) {
            player_url = "https:" + player_url;
        } else if (!player_url.matches("https?://")) {
            player_url = "https://www.youtube.com" + player_url;
        }

        SignatureFunction func;
        String playerId = player_url + "_" + signatureCacheId(s);
        if (!playerCache.containsKey(player_url + "_" + signatureCacheId(s))) {
            func = extractSignatureFunction(player_url, s);
            playerCache.put(playerId, func);
        } else {
            func = playerCache.get(playerId);
        }
        Objects.requireNonNull(func);
        try {
            return func.apply(s);
        } catch (JSInterpreter.InterpretException e) {
            throw new ExtractException(ErrorCode.JSINTERPRETERROR, e.getMessage());
        }
    }

    private int extractFilesize(String media_url) {
        return NumberUtils.toInt(Utils.searchRegex("\\bclen[=/](\\d+)", media_url), 0);
    }

    private JsonObject video_info = null;
    private JsonObject player_response = null;
    private ArrayList<String> dash_mpds;

    private ArrayList<YtStream> realExtract(String videoId) throws ExtractException {
        String video_webpage = Utils.downloadWebPage("https://www.youtube.com/watch?v=" + videoId + "&gl=US&hl=en&has_verified=1&bpctr=9999999999");
        String embed_webpage = null;
        String player_url = null;
        boolean is_live = false;
        boolean age_gate;
        dash_mpds = new ArrayList<>();
        ArrayList<YtStream> formats = new ArrayList<>();
        playerCache = new HashMap<>();
        Matcher m;

        if (Pattern.compile("player-age-gate-content>").matcher(video_webpage).find()) {
            age_gate = true;
            embed_webpage = Utils.downloadWebPage("https://www.youtube.com/embed/" + videoId);
            String data = "video_id=" + videoId + "&eurl=https%3A%2F%2Fyoutube.googleapis.com%2Fv%2F" + videoId + "&sts=" + Utils.searchRegex("\"sts\"\\s*:\\s*(\\d+)", embed_webpage);
            String video_info_webpage = Utils.downloadWebPage("https://www.youtube.com/get_video_info?" + data);
            video_info = Utils.parseQueryString(video_info_webpage);
            if (video_info != null && video_info.has("player_response")) {
                player_response = extractPlayerResponse(video_info.get("player_response").getAsString());
                addDashMpd();
            } else {
                player_response = null;
            }
        } else {
            age_gate = false;
            JsonObject ytplayer_config = getYtplayerConfig(video_webpage);
            if (ytplayer_config != null) {
                JsonObject args = ytplayer_config.get("args").getAsJsonObject();
                if (args.has("url_encoded_fmt_stream_map") || args.has("hlsvp")) {
                    video_info = args;
                    addDashMpd();
                }
                if (video_info == null && args.has("ypc_vid")) {
                    // Rental video is not rented but preview is available
                    throw new ExtractException(ErrorCode.RENTAL, "Rental video not supported.");
                }
                if ((args.has("livestream") && args.get("livestream").getAsString().equals("1")) || (args.has("live_playback") && args.get("live_playback").getAsInt() == 1)) {
                    is_live = true;
                }
            }
            addDashMpdPr();
        }

        if (video_info == null && player_response == null) {
            throw new ExtractException(ErrorCode.NODATA, "Unable to extract video data");
        }

        JsonObject video_details = (player_response != null && player_response.has("videoDetails")) ? player_response.get("videoDetails").getAsJsonObject() : null;

        if (!is_live) {
            is_live = (video_details != null && video_details.has("isLive")) && video_details.get("isLive").getAsBoolean();
        }

        if (video_info != null && video_info.has("ypc_video_rental_bar_text") && !video_info.has("author")) {
            throw new ExtractException(ErrorCode.RENTAL, "Rental video not supported.");
        }

        JsonArray streaming_formats = new JsonArray();
        if (player_response.has("streamingData")) {
            JsonObject streamingData = player_response.get("streamingData").getAsJsonObject();
            if (streamingData.has("formats")) {
                for (JsonElement fmt : streamingData.get("formats").getAsJsonArray()) {
                    streaming_formats.add(fmt.getAsJsonObject());
                }
            }
            if (streamingData.has("adaptiveFormats")) {
                JsonArray adaptiveFormats = streamingData.get("adaptiveFormats").getAsJsonArray();
                for (int i = 0; i < adaptiveFormats.size(); i++) {
                    streaming_formats.add(adaptiveFormats.get(i));
                }
            }
        }

        if (video_info != null && video_info.has("conn") && video_info.get("conn").getAsString().startsWith("rtmp")) {
            // rtmp
            JsonObject dct = new JsonObject();
            dct.addProperty("format_id", "_rtmp");
            dct.addProperty("protocol", "rtmp");
            dct.addProperty("url", video_info.get("conn").getAsString());
            formats.add(new YtStream(dct));

        } else if (!is_live && (streaming_formats.size() > 0 || (video_info != null && video_info.has("url_encoded_fmt_stream_map") && video_info.get("url_encoded_fmt_stream_map").getAsString().length() >= 1) || (video_info != null && video_info.has("adaptive_fmts") && video_info.get("url_encoded_fmt_stream_map").getAsString().length() >= 1))) {
            // rtmpe downloads are not supported
            if (video_info != null && video_info.has("url_encoded_fmt_stream_map")) {
                if (video_info.get("url_encoded_fmt_stream_map").getAsString().contains("rtmpe%3Dyes")) {
                    return null;
                }
            }
            if (video_info != null && video_info.has("adaptive_fmts")) {
                if (video_info.get("adaptive_fmts").getAsString().contains("rtmpe%3Dyes")) {
                    return null;
                }
            }

            JsonObject formats_spec = new JsonObject();
            if (video_info != null && video_info.has("fmt_list")) {
                String fmt_list = video_info.get("fmt_list").getAsString();
                for (String fmt : fmt_list.split(",")) {
                    String[] spec = fmt.split("/");
                    if (spec.length > 1) {
                        String[] width_height = spec[1].split("x");
                        if (width_height.length == 2) {
                            JsonObject spec_obj = new JsonObject();
                            spec_obj.addProperty("resolution", spec[1]);
                            spec_obj.addProperty("width", NumberUtils.toInt(width_height[0]));
                            spec_obj.addProperty("height", NumberUtils.toInt(width_height[1]));
                            formats_spec.add(spec[0], spec_obj);
                        }
                    }
                }
            }
            for (JsonElement _fmt : streaming_formats) {
                JsonObject fmt = _fmt.getAsJsonObject();
                if (!fmt.has("itag")) {
                    continue;
                }
                String itag = fmt.get("itag").getAsString();
                String quality = fmt.has("quality") ? fmt.get("quality").getAsString() : null;
                String quality_label = fmt.has("qualityLabel") ? fmt.get("qualityLabel").getAsString() : quality;
                JsonObject spec_obj = new JsonObject();
                spec_obj.addProperty("asr", fmt.has("audioSampleRate") ? fmt.get("audioSampleRate").getAsInt() : 0);
                spec_obj.addProperty("filesize", fmt.has("contentLength") ? fmt.get("contentLength").getAsInt() : 0);
                spec_obj.addProperty("formate_note", quality_label);
                spec_obj.addProperty("fps", fmt.has("fps") ? fmt.get("fps").getAsInt() : 0);
                spec_obj.addProperty("height", fmt.has("height") ? fmt.get("height").getAsInt() : 0);
                spec_obj.addProperty("tbr", itag.equals("43") ? (fmt.has("averageBitrate") ? fmt.get("averageBitrate").getAsFloat() / 1000 : (fmt.has("bitrate") ? fmt.get("bitrate").getAsFloat() / 1000 : 0f)) : 0f);
                spec_obj.addProperty("width", fmt.has("width") ? fmt.get("width").getAsInt() : 0);
                formats_spec.add(itag, spec_obj);
            }
            for (JsonElement _fmt : streaming_formats) {
                JsonObject fmt = _fmt.getAsJsonObject();
                if (fmt.has("drm_families")) {
                    continue;
                }
                String url;
                String cipher = null;
                JsonObject url_data;
                if (!fmt.has("url")) {
                    if (!fmt.has("cipher")) {
                        continue;
                    }
                    cipher = fmt.get("cipher").getAsString();
                    url_data = Utils.parseQueryString(cipher);
                    if (!url_data.has("url")) {
                        continue;
                    }
                    url = url_data.get("url").getAsString();
                } else {
                    url = fmt.get("url").getAsString();
                    try {
                        url_data = Utils.parseQueryString(new URL(fmt.get("url").getAsString()).getQuery());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        url_data = new JsonObject();
                    }
                }
                int stream_type = (url_data != null && url_data.has("stream_type")) ? url_data.get("stream_type").getAsInt() : -1;
                if (stream_type == 3) {
                    continue;
                }
                String format_id = fmt.has("itag") ? fmt.get("itag").getAsString() : ((url_data != null && url_data.has("itag")) ? url_data.get("itag").getAsString() : null);
                if (format_id == null) {
                    continue;
                }
                if (cipher != null) {
                    if (url_data.has("s")) {
                        final String ASSETS_RE = "\"assets\":.+?\"js\":\\s*(\"[^\"]+\")";
                        String jsplayer_url_json = Utils.searchRegex(ASSETS_RE, age_gate ? embed_webpage : video_webpage);
                        if (jsplayer_url_json.length() == 0 && !age_gate) {
                            if (embed_webpage == null) {
                                embed_webpage = Utils.downloadWebPage("https://www.youtube.com/embed/" + videoId);
                            }
                            jsplayer_url_json = Utils.searchRegex(ASSETS_RE, embed_webpage);
                        }

                        player_url = unescapeJava(Utils.parseJsonString(jsplayer_url_json).getAsString());
                        if (player_url == null) {
                            String player_url_json = Utils.searchRegex("ytplayer\\.config.*?\"url\"\\s*:\\s*(\"[^\"]+\")", video_webpage);
                            player_url = new JsonPrimitive(player_url_json).getAsString();
                        }
                    }
                    if (url_data.has("sig")) {
                        url += "&signature=" + url_data.get("sig").getAsString();
                    } else if (url_data.has("s")) {
                        String encrypted_sig = url_data.get("s").getAsString();
                        String signature = null;
                        try {
                            signature = decryptSignature(encrypted_sig, player_url);
                        } catch (ExtractException e) {
                            continue;
                        }
                        String sp = url_data.has("sp") ? url_data.get("sp").getAsString() : "signature";
                        url += "&" + sp + '=' + signature;
                    }
                }
                if (!url.contains("ratebypass")) {
                    url += "&ratebypass=yes";
                }
                JsonObject dct = new JsonObject();
                dct.addProperty("format_id", format_id);
                dct.addProperty("url", url);
                if (YoutubeIE.formats.has(format_id)) {
                    for (Map.Entry<String, JsonElement> entry : YoutubeIE.formats.get(format_id).getAsJsonObject().entrySet()) {
                        dct.add(entry.getKey(), entry.getValue());
                    }
                }
                if (formats_spec.has(format_id)) {
                    for (Map.Entry<String, JsonElement> entry : formats_spec.get(format_id).getAsJsonObject().entrySet()) {
                        dct.add(entry.getKey(), entry.getValue());
                    }
                }
                m = Pattern.compile("^(?<width>\\d+)[xX](?<height>\\d+)$").matcher(url_data.has("size") ? url_data.get("size").getAsString() : "");
                int width = 0;
                int height = 0;
                if (m.find()) {
                    width = NumberUtils.toInt(m.group("width"), 0);
                    height = NumberUtils.toInt(m.group("height"), 0);
                }
                if (width == 0) {
                    width = fmt.has("width") ? fmt.get("width").getAsInt() : 0;
                }
                if (height == 0) {
                    height = fmt.has("height") ? fmt.get("height").getAsInt() : 0;
                }
                int filesize = url_data.has("clen") ? url_data.get("clen").getAsInt() : extractFilesize(url);
                String quality = url_data.has("quality") ? url_data.get("quality").getAsString() : fmt.has("quality") ? fmt.get("quality").getAsString() : null;
                String quality_label = url_data.has("quality_label") ? url_data.get("quality_label").getAsString() : fmt.has("qualityLabel") ? fmt.get("qualityLabel").getAsString() : null;
                float tbr = fmt.has("bitrate") ? fmt.get("bitrate").getAsFloat() / 1000 : (!format_id.equals("43") && fmt.has("bitrate")) ? fmt.get("bitrate").getAsFloat() / 1000 : 0;
                int fps = url_data.has("fps") ? url_data.get("fps").getAsInt() : fmt.has("fps") ? fmt.get("fps").getAsInt() : 0;
                dct.addProperty("filesize", filesize);
                dct.addProperty("tbr", tbr);
                dct.addProperty("width", width);
                dct.addProperty("height", height);
                dct.addProperty("fps", fps);
                dct.addProperty("format_note", quality_label != null ? quality_label : quality);
                String type = url_data.has("type") ? url_data.get("type").getAsString() : fmt.has("mimeType") ? fmt.get("mimeType").getAsString() : null;
                if (type != null) {
                    String[] type_split = type.split(";");
                    String[] kind_ext = type_split[0].split("/");
                    if (kind_ext.length == 2) {
                        String kind = kind_ext[0];
                        dct.addProperty("ext", ExtractorUtils.mimetype2ext(type_split[0]));
                        if (kind.equals("audio") || kind.equals("video")) {
                            String codecs = null;
                            m = Pattern.compile("(?<key>[a-zA-Z_-]+)=(?<quote>[\\\"\\']?)(?<val>.+?)(?=quote)(?:;|$)").matcher(type);
                            while (m.find()) {
                                if (Objects.equals(m.group("key"), "codecs")) {
                                    codecs = m.group("val");
                                    break;
                                }
                            }
                            if (codecs != null) {
                                for (Map.Entry<String, JsonElement> entry : ExtractorUtils.parseCodecs(codecs).entrySet()) {
                                    dct.add(entry.getKey(), entry.getValue());
                                }
                            }
                        }
                    }
                }
                if (!dct.has("acodec")) {
                    dct.addProperty("acodec", "none");
                }
                if (!dct.has("vcodec")) {
                    dct.addProperty("vcodec", "none");
                }
                if (dct.get("acodec").getAsString().equals("none") || dct.get("vcodec").getAsString().equals("none")) {
                    dct.addProperty("http_chunk_size", 10485760);
                }
                if (!dct.get("acodec").getAsString().equals("none") && dct.get("vcodec").getAsString().equals("none")) {
                    dct.addProperty("mediatype", "audio");
                } else if (dct.get("acodec").getAsString().equals("none") && !dct.get("vcodec").getAsString().equals("none")) {
                    dct.addProperty("mediatype", "video");
                } else {
                    dct.addProperty("mediatype", "normal");
                }
                formats.add(new YtStream(dct));
            }
        } else {
            String manifest_url = null;
            if (player_response.has("streamingData")) {
                JsonObject streamingData = player_response.get("streamingData").getAsJsonObject();
                if (streamingData.has("hlsManifestUrl")) {
                    manifest_url = streamingData.get("hlsManifestUrl").getAsString();
                }
            }
            if (manifest_url == null && video_info != null && video_info.has("hlsvp")) {
                manifest_url = video_info.get("hlsvp").getAsString();
            }
            if (manifest_url != null) {
                JsonObject dct = new JsonObject();
                dct.addProperty("url", manifest_url);
            }
            // TODO: add m3u8 support
            throw new NotImplementedException("m3u8 format not implemented");
        }

        // TODO: look for DASH manifest
        // Check for malformed aspect ratio
        m = Pattern.compile("<meta\\s+property=\\\"og:video:tag\\\".*?content=\\\"yt:stretch=(?<w>[0-9]+):(?<h>[0-9]+)\\\">").matcher(video_webpage);
        if (m.find()) {
            float w = NumberUtils.toFloat(m.group("w"));
            float h = NumberUtils.toFloat(m.group("h"));
            if (w > 0 && h > 0) {
                float ratio = w / h;
                for (YtStream fmt : formats) {
                    if (fmt.vcodec.equals("none")) {
                        fmt.stretchedRatio = ratio;
                    }
                }
            }
        }
        if (formats.size() == 0 && (video_info.has("license_info") || (player_response.has("streamingData") && player_response.get("streamingData").getAsJsonObject().has("licenseInfos")))) {
            throw new ExtractException(ErrorCode.DRM);
        }
        return formats;
    }

    public Result extract(String videoId) {
        try {
            ArrayList<YtStream> formats = realExtract(videoId);
            return new Result(formats);
        } catch (ExtractException e) {
            return new Result(e.getErrorCode(), e.getMessage());
        }
    }
}
