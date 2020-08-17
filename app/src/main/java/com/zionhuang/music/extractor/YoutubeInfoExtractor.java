package com.zionhuang.music.extractor;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

import com.zionhuang.music.utils.RegexUtils;
import com.zionhuang.music.utils.Utils;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zionhuang.music.extractor.YtStream.TYPE_AUDIO;
import static com.zionhuang.music.extractor.YtStream.TYPE_NORMAL;
import static com.zionhuang.music.extractor.YtStream.TYPE_VIDEO;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

/**
 * A Java version of youtube_dl (https://github.com/ytdl-org/youtube-dl)
 *
 * @author Zion Huang
 * @version 2020.07.28
 */

public class YoutubeInfoExtractor {
    private static final String TAG = "YoutubeInfoExtractor";
    private static JSON Formats = JSON.parseJsonString("{\"5\":{\"ext\":\"flv\",\"width\":400,\"height\":240,\"acodec\":\"mp3\",\"abr\":64,\"vcodec\":\"h263\"},\"6\":{\"ext\":\"flv\",\"width\":450,\"height\":270,\"acodec\":\"mp3\",\"abr\":64,\"vcodec\":\"h263\"},\"13\":{\"ext\":\"3gp\",\"acodec\":\"aac\",\"vcodec\":\"mp4v\"},\"17\":{\"ext\":\"3gp\",\"width\":176,\"height\":144,\"acodec\":\"aac\",\"abr\":24,\"vcodec\":\"mp4v\"},\"18\":{\"ext\":\"mp4\",\"width\":640,\"height\":360,\"acodec\":\"aac\",\"abr\":96,\"vcodec\":\"h264\"},\"22\":{\"ext\":\"mp4\",\"width\":1280,\"height\":720,\"acodec\":\"aac\",\"abr\":192,\"vcodec\":\"h264\"},\"34\":{\"ext\":\"flv\",\"width\":640,\"height\":360,\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\"},\"35\":{\"ext\":\"flv\",\"width\":854,\"height\":480,\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\"},\"36\":{\"ext\":\"3gp\",\"width\":320,\"acodec\":\"aac\",\"vcodec\":\"mp4v\"},\"37\":{\"ext\":\"mp4\",\"width\":1920,\"height\":1080,\"acodec\":\"aac\",\"abr\":192,\"vcodec\":\"h264\"},\"38\":{\"ext\":\"mp4\",\"width\":4096,\"height\":3072,\"acodec\":\"aac\",\"abr\":192,\"vcodec\":\"h264\"},\"43\":{\"ext\":\"webm\",\"width\":640,\"height\":360,\"acodec\":\"vorbis\",\"abr\":128,\"vcodec\":\"vp8\"},\"44\":{\"ext\":\"webm\",\"width\":854,\"height\":480,\"acodec\":\"vorbis\",\"abr\":128,\"vcodec\":\"vp8\"},\"45\":{\"ext\":\"webm\",\"width\":1280,\"height\":720,\"acodec\":\"vorbis\",\"abr\":192,\"vcodec\":\"vp8\"},\"46\":{\"ext\":\"webm\",\"width\":1920,\"height\":1080,\"acodec\":\"vorbis\",\"abr\":192,\"vcodec\":\"vp8\"},\"59\":{\"ext\":\"mp4\",\"width\":854,\"height\":480,\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\"},\"78\":{\"ext\":\"mp4\",\"width\":854,\"height\":480,\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\"},\"82\":{\"ext\":\"mp4\",\"height\":360,\"format_note\":\"3D\",\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\",\"preference\":-20},\"83\":{\"ext\":\"mp4\",\"height\":480,\"format_note\":\"3D\",\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\",\"preference\":-20},\"84\":{\"ext\":\"mp4\",\"height\":720,\"format_note\":\"3D\",\"acodec\":\"aac\",\"abr\":192,\"vcodec\":\"h264\",\"preference\":-20},\"85\":{\"ext\":\"mp4\",\"height\":1080,\"format_note\":\"3D\",\"acodec\":\"aac\",\"abr\":192,\"vcodec\":\"h264\",\"preference\":-20},\"100\":{\"ext\":\"webm\",\"height\":360,\"format_note\":\"3D\",\"acodec\":\"vorbis\",\"abr\":128,\"vcodec\":\"vp8\",\"preference\":-20},\"101\":{\"ext\":\"webm\",\"height\":480,\"format_note\":\"3D\",\"acodec\":\"vorbis\",\"abr\":192,\"vcodec\":\"vp8\",\"preference\":-20},\"102\":{\"ext\":\"webm\",\"height\":720,\"format_note\":\"3D\",\"acodec\":\"vorbis\",\"abr\":192,\"vcodec\":\"vp8\",\"preference\":-20},\"91\":{\"ext\":\"mp4\",\"height\":144,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":48,\"vcodec\":\"h264\",\"preference\":-10},\"92\":{\"ext\":\"mp4\",\"height\":240,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":48,\"vcodec\":\"h264\",\"preference\":-10},\"93\":{\"ext\":\"mp4\",\"height\":360,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\",\"preference\":-10},\"94\":{\"ext\":\"mp4\",\"height\":480,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":128,\"vcodec\":\"h264\",\"preference\":-10},\"95\":{\"ext\":\"mp4\",\"height\":720,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":256,\"vcodec\":\"h264\",\"preference\":-10},\"96\":{\"ext\":\"mp4\",\"height\":1080,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":256,\"vcodec\":\"h264\",\"preference\":-10},\"132\":{\"ext\":\"mp4\",\"height\":240,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":48,\"vcodec\":\"h264\",\"preference\":-10},\"151\":{\"ext\":\"mp4\",\"height\":72,\"format_note\":\"HLS\",\"acodec\":\"aac\",\"abr\":24,\"vcodec\":\"h264\",\"preference\":-10},\"133\":{\"ext\":\"mp4\",\"height\":240,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"134\":{\"ext\":\"mp4\",\"height\":360,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"135\":{\"ext\":\"mp4\",\"height\":480,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"136\":{\"ext\":\"mp4\",\"height\":720,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"137\":{\"ext\":\"mp4\",\"height\":1080,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"138\":{\"ext\":\"mp4\",\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"160\":{\"ext\":\"mp4\",\"height\":144,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"212\":{\"ext\":\"mp4\",\"height\":480,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"264\":{\"ext\":\"mp4\",\"height\":1440,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"298\":{\"ext\":\"mp4\",\"height\":720,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\",\"fps\":60},\"299\":{\"ext\":\"mp4\",\"height\":1080,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\",\"fps\":60},\"266\":{\"ext\":\"mp4\",\"height\":2160,\"format_note\":\"DASHvideo\",\"vcodec\":\"h264\"},\"139\":{\"ext\":\"m4a\",\"format_note\":\"DASHaudio\",\"acodec\":\"aac\",\"abr\":48,\"container\":\"m4a_dash\"},\"140\":{\"ext\":\"m4a\",\"format_note\":\"DASHaudio\",\"acodec\":\"aac\",\"abr\":128,\"container\":\"m4a_dash\"},\"141\":{\"ext\":\"m4a\",\"format_note\":\"DASHaudio\",\"acodec\":\"aac\",\"abr\":256,\"container\":\"m4a_dash\"},\"256\":{\"ext\":\"m4a\",\"format_note\":\"DASHaudio\",\"acodec\":\"aac\",\"container\":\"m4a_dash\"},\"258\":{\"ext\":\"m4a\",\"format_note\":\"DASHaudio\",\"acodec\":\"aac\",\"container\":\"m4a_dash\"},\"325\":{\"ext\":\"m4a\",\"format_note\":\"DASHaudio\",\"acodec\":\"dtse\",\"container\":\"m4a_dash\"},\"328\":{\"ext\":\"m4a\",\"format_note\":\"DASHaudio\",\"acodec\":\"ec-3\",\"container\":\"m4a_dash\"},\"167\":{\"ext\":\"webm\",\"height\":360,\"width\":640,\"format_note\":\"DASHvideo\",\"container\":\"webm\",\"vcodec\":\"vp8\"},\"168\":{\"ext\":\"webm\",\"height\":480,\"width\":854,\"format_note\":\"DASHvideo\",\"container\":\"webm\",\"vcodec\":\"vp8\"},\"169\":{\"ext\":\"webm\",\"height\":720,\"width\":1280,\"format_note\":\"DASHvideo\",\"container\":\"webm\",\"vcodec\":\"vp8\"},\"170\":{\"ext\":\"webm\",\"height\":1080,\"width\":1920,\"format_note\":\"DASHvideo\",\"container\":\"webm\",\"vcodec\":\"vp8\"},\"218\":{\"ext\":\"webm\",\"height\":480,\"width\":854,\"format_note\":\"DASHvideo\",\"container\":\"webm\",\"vcodec\":\"vp8\"},\"219\":{\"ext\":\"webm\",\"height\":480,\"width\":854,\"format_note\":\"DASHvideo\",\"container\":\"webm\",\"vcodec\":\"vp8\"},\"278\":{\"ext\":\"webm\",\"height\":144,\"format_note\":\"DASHvideo\",\"container\":\"webm\",\"vcodec\":\"vp9\"},\"242\":{\"ext\":\"webm\",\"height\":240,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"243\":{\"ext\":\"webm\",\"height\":360,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"244\":{\"ext\":\"webm\",\"height\":480,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"245\":{\"ext\":\"webm\",\"height\":480,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"246\":{\"ext\":\"webm\",\"height\":480,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"247\":{\"ext\":\"webm\",\"height\":720,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"248\":{\"ext\":\"webm\",\"height\":1080,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"271\":{\"ext\":\"webm\",\"height\":1440,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"272\":{\"ext\":\"webm\",\"height\":2160,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"302\":{\"ext\":\"webm\",\"height\":720,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\",\"fps\":60},\"303\":{\"ext\":\"webm\",\"height\":1080,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\",\"fps\":60},\"308\":{\"ext\":\"webm\",\"height\":1440,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\",\"fps\":60},\"313\":{\"ext\":\"webm\",\"height\":2160,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\"},\"315\":{\"ext\":\"webm\",\"height\":2160,\"format_note\":\"DASHvideo\",\"vcodec\":\"vp9\",\"fps\":60},\"171\":{\"ext\":\"webm\",\"acodec\":\"vorbis\",\"format_note\":\"DASHaudio\",\"abr\":128},\"172\":{\"ext\":\"webm\",\"acodec\":\"vorbis\",\"format_note\":\"DASHaudio\",\"abr\":256},\"249\":{\"ext\":\"webm\",\"format_note\":\"DASHaudio\",\"acodec\":\"opus\",\"abr\":50},\"250\":{\"ext\":\"webm\",\"format_note\":\"DASHaudio\",\"acodec\":\"opus\",\"abr\":70},\"251\":{\"ext\":\"webm\",\"format_note\":\"DASHaudio\",\"acodec\":\"opus\",\"abr\":160},\"_rtmp\":{\"protocol\":\"rtmp\"},\"394\":{\"acodec\":\"none\",\"vcodec\":\"av01.0.05M.08\"},\"395\":{\"acodec\":\"none\",\"vcodec\":\"av01.0.05M.08\"},\"396\":{\"acodec\":\"none\",\"vcodec\":\"av01.0.05M.08\"},\"397\":{\"acodec\":\"none\",\"vcodec\":\"av01.0.05M.08\"}}");
    private HashMap<String, SignatureFunction> playerCache = new HashMap<>();

    private static class ExtractException extends Exception {
        ExtractException(String msg) {
            super(msg);
        }
    }


    private void addDashMpd(ArrayList<String> dashMpdList, JSON videoInfo) {
        String dashMpd = videoInfo.getString("dashmpd");
        if (!dashMpd.isEmpty() && !dashMpdList.contains(dashMpd)) {
            dashMpdList.add(dashMpd);
        }
    }

    private void addDashMpdPr(ArrayList<String> dashMpdList, JSON playerResponse) {
        String dashMpd = playerResponse.get("streamingData").get("dashManifestUrl").getAsString();
        if (!dashMpd.isEmpty() && !dashMpdList.contains(dashMpd)) {
            dashMpdList.add(dashMpd);
        }
    }

    @NonNull
    private Pair<JSON, String> getYtPlayerConfig(String videoWebPage) {
        final String[] patterns = {";ytplayer\\.config\\s*=\\s*(\\{.+?\\});ytplayer", ";ytplayer\\.config\\s*=\\s*(\\{.+?\\});"};
        String config = RegexUtils.search(patterns, videoWebPage);
        if (config.isEmpty()) {
            return new Pair<>(JSON.NULL, null);
        }
        String plResponse = null;
        // pull out player response lest the string be too long to parse
        Matcher m = Pattern.compile(",\"player_response\":\"(\\{.+\\})\"").matcher(config);
        if (m.find()) {
            plResponse = unescapeJava(m.group(1));
            config = config.replaceAll(",\"player_response\":\"(\\{.+\\})\"", "");
        }
        JSON ytPlayerConfig = JSON.parseJsonString(config);
        return new Pair<>(ytPlayerConfig, plResponse);
    }

    @NonNull
    private JSON extractPlayerResponse(String plResponseStr, ArrayList<String> dashMpdList) {
        JSON plResponse = plResponseStr == null ? JSON.NULL : JSON.parseJsonString(plResponseStr);
        addDashMpdPr(dashMpdList, plResponse);
        return plResponse;
    }

    private String signatureCacheId(String exampleSig) {
        return StringUtils.join(exampleSig.split("."), ".");
    }

    private static final String[] JsFnRE = {
            "\\b[cs]\\s*&&\\s*[adf]\\.set\\([^,]+\\s*,\\s*encodeURIComponent\\s*\\(\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
            "\\b[a-zA-Z0-9]+\\s*&&\\s*[a-zA-Z0-9]+\\.set\\([^,]+\\s*,\\s*encodeURIComponent\\s*\\(\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
            "(?:\\b|[^a-zA-Z0-9$])(?<sig>[a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)",
            "(?<sig>[a-zA-Z0-9$]+)\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\\\"\\\"\\s*\\)",
            "([\"\\'])signature\\1\\s*,\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
            "\\.sig\\|\\|(?<sig>[a-zA-Z0-9$]+)\\(",
            "yt\\.akamaized\\.net/\\)\\s*\\|\\|\\s*.*?\\s*[cs]\\s*&&\\s*[adf]\\.set\\([^,]+\\s*,\\s*(?:encodeURIComponent\\s*\\()?\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
            "\\b[cs]\\s*&&\\s*[adf]\\.set\\([^,]+\\s*,\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
            "\\b[a-zA-Z0-9]+\\s*&&\\s*[a-zA-Z0-9]+\\.set\\([^,]+\\s*,\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
            "\\bc\\s*&&\\s*a\\.set\\([^,]+\\s*,\\s*\\([^)]*\\)\\s*\\(\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
            "\\bc\\s*&&\\s*[a-zA-Z0-9]+\\.set\\([^,]+\\s*,\\s*\\([^)]*\\)\\s*\\(\\s*(?<sig>[a-zA-Z0-9$]+)\\(",
            "\\bc\\s*&&\\s*[a-zA-Z0-9]+\\.set\\([^,]+\\s*,\\s*\\([^)]*\\)\\s*\\(\\s*(?<sig>[a-zA-Z0-9$]+)\\("
    };

    private SignatureFunction parseSigJs(String jsCode) throws JSInterpreter.InterpretException {
        final String funcName = RegexUtils.search(JsFnRE, jsCode, "sig");
        JSInterpreter jsi = new JSInterpreter(jsCode);
        final JSFunction initialFn = jsi.extractFunction(funcName);
        return s -> initialFn.apply(JSON.toArray(s)).getAsString();
    }

    private final static String[] PlayerInfoRE = {"/(?<id>[a-zA-Z0-9_-]{8,})/player_ias\\.vflset(?:/[a-zA-Z]{2,3}_[a-zA-Z]{2,3})?/base\\.(?<ext>[a-z]+)$", "\\b(?<id>vfl[a-zA-Z0-9_-]+)\\b.*?\\.(?<ext>[a-z]+)$"};

    private Pair<String, String> extractPlayerInfo(String playerUrl) throws ExtractException {
        for (String regex : PlayerInfoRE) {
            Matcher m = Pattern.compile(regex).matcher(playerUrl);
            if (m.find()) {
                return new Pair<>(m.group("ext"), m.group("id"));
            }
        }
        throw new ExtractException("Cannot identify player " + playerUrl);
    }

    @NonNull
    private SignatureFunction extractSignatureFunction(String playerUrl, String exampleSig) throws ExtractException {
        Pair<String, String> playerInfo = extractPlayerInfo(playerUrl);
        String playerType = playerInfo.first;
        String playerId = playerInfo.second;

        String func_id = playerType + "$" + playerId + signatureCacheId(exampleSig);
        // TODO: load function from cache

        SignatureFunction res;
        if ("js".equals(playerType)) {
            String code = Utils.downloadPlainText(playerUrl);
            try {
                res = parseSigJs(code);
            } catch (JSInterpreter.InterpretException e) {
                throw new ExtractException(e.getMessage());
            }
        } else if ("swf".equals(playerType)) {
            // TODO: swf interpreter
            throw new NotImplementedException("SWF interpreter not implemented");
        } else {
            throw new ExtractException("invalid player type " + playerType);
        }
        return res;
    }

    private String decryptSignature(String s, String playerUrl) throws ExtractException {
        if (playerUrl.isEmpty()) {
            throw new ExtractException("Cannot decrypt signature without player url");
        }

        if (playerUrl.startsWith("//")) {
            playerUrl = "https:" + playerUrl;
        } else if (!playerUrl.matches("https?://")) {
            playerUrl = "https://www.youtube.com" + playerUrl;
        }

        SignatureFunction func;
        String playerId = playerUrl + "$" + signatureCacheId(s);
        if (!playerCache.containsKey(playerId)) {
            func = extractSignatureFunction(playerUrl, s);
            playerCache.put(playerId, func);
        } else {
            func = playerCache.get(playerId);
        }
        Objects.requireNonNull(func);
        try {
            return func.apply(s);
        } catch (JSInterpreter.InterpretException e) {
            throw new ExtractException(e.getMessage());
        }
    }

    private int extractFileSize(String mediaUrl) {
        return NumberUtils.toInt(RegexUtils.search("\\bclen[=/](\\d+)", mediaUrl));
    }


    private ExtractedInfo realExtract(String videoId) {
        ExtractedInfo res = new ExtractedInfo();
        JSON videoInfo = JSON.NULL;
        JSON playerResponse = JSON.NULL;
        ArrayList<String> dashMpdList = new ArrayList<>();
        String videoWebPage = Utils.downloadWebPage("https://www.youtube.com/watch?v=" + videoId + "&gl=US&hl=en&has_verified=1&bpctr=9999999999");
        String embedWebPage = "";
        String playerUrl = "";
        boolean isLive = false;
        boolean ageGate;
        ArrayList<YtStream> formats = new ArrayList<>();
        Matcher m;

        if (RegexUtils.find("player-age-gate-content>", videoWebPage)) {
            ageGate = true;
            embedWebPage = Utils.downloadWebPage("https://www.youtube.com/embed/" + videoId);
            String VideoInfoWebPage = Utils.downloadWebPage("https://www.youtube.com/get_video_info?" +
                    "video_id=" + videoId +
                    "&eurl=https%3A%2F%2Fyoutube.googleapis.com%2Fv%2F" + videoId +
                    "&sts=" + RegexUtils.search("\"sts\"\\s*:\\s*(\\d+)", embedWebPage));
            videoInfo = JSON.parseQueryString(VideoInfoWebPage);
            playerResponse = extractPlayerResponse(videoInfo.getString("player_response"), dashMpdList);
            addDashMpd(dashMpdList, videoInfo);
        } else {
            ageGate = false;
            Pair<JSON, String> ytPlayerConfig = getYtPlayerConfig(videoWebPage);
            if (!Objects.requireNonNull(ytPlayerConfig.first).isNull()) {
                JSON args = ytPlayerConfig.first.get("args");
                if (args.has("url_encoded_fmt_stream_map") || args.has("hlsvp")) {
                    videoInfo = args;
                    addDashMpd(dashMpdList, videoInfo);
                }
                if (videoInfo.isNull() && args.has("ypc_vid")) {
                    // Rental video is not rented but preview is available
                    return new ExtractedInfo(ErrorCode.RENTAL, "Rental video not supported");
                }
                if (args.get("livestream").equals("1") || args.get("live_playback").equals(1)) {
                    isLive = true;
                }
                if (playerResponse.isNull()) {
                    playerResponse = extractPlayerResponse(ytPlayerConfig.second, dashMpdList);
                }
            }
        }

        if (videoInfo.isNull() && playerResponse.isNull()) {
            return new ExtractedInfo(ErrorCode.NO_INFO, "Unable to extract video data");
        }

        JSON videoDetails = playerResponse.get("videoDetails");

        if (!isLive) {
            isLive = videoDetails.getBoolean("isLive");
        }

        if (videoInfo.has("ypc_video_rental_bar_text") && !videoInfo.has("author")) {
            return new ExtractedInfo(ErrorCode.RENTAL, "Rental video not supported");
        }

        JSON streamingFormats = JSON.createArray();
        playerResponse.get("streamingData").get("formats").forEach((Consumer<JSON>) streamingFormats::add);
        playerResponse.get("streamingData").get("adaptiveFormats").forEach((Consumer<JSON>) streamingFormats::add);

        if (videoInfo.has("conn") && videoInfo.getString("conn").startsWith("rtmp")) {
            // rtmp
            formats.add(new YtStream(JSON.createObject()
                    .add("format_id", "_rtmp")
                    .add("protocol", "rtmp")
                    .add("url", videoInfo.getString("conn")))
            );
        } else if (!isLive && (streamingFormats.size() > 0 || !videoInfo.getString("url_encoded_fmt_stream_map").isEmpty() || !videoInfo.getString("adaptive_fmts").isEmpty())) {
            if ((videoInfo.getString("url_encoded_fmt_stream_map") + ',' + videoInfo.getString("adaptive_fmts")).contains("rtmpe%3Dyes")) {
                return new ExtractedInfo(ErrorCode.RTMPE, "rtmpe downloads are not supported");
            }

            JSON formatsSpec = JSON.createObject();
            if (videoInfo.has("fmt_list")) {
                for (String fmt : videoInfo.getString("fmt_list").split(",")) {
                    String[] spec = fmt.split("/");
                    if (spec.length > 1) {
                        String[] widthHeight = spec[1].split("x");
                        if (widthHeight.length == 2) {
                            formatsSpec.add(spec[0], JSON.createObject()
                                    .add("resolution", spec[1])
                                    .add("width", NumberUtils.toInt(widthHeight[0]))
                                    .add("height", NumberUtils.toInt(widthHeight[1]))
                            );
                        }
                    }
                }
            }
            for (JSON fmt : streamingFormats) {
                if (!fmt.has("itag")) {
                    continue;
                }
                String itag = fmt.getString("itag");
                String quality = fmt.getString("quality");
                String qualityLabel = fmt.has("qualityLabel") ? fmt.getString("qualityLabel") : quality;
                formatsSpec.add(itag, JSON.createObject()
                        .add("asr", fmt.getInt("audioSampleRate"))
                        .add("filesize", fmt.getInt("contentLength"))
                        .add("formate_note", qualityLabel)
                        .add("fps", fmt.getInt("fps"))
                        .add("height", fmt.getInt("height"))
                        .add("tbr", !itag.equals("43") ? (fmt.has("averageBitrate") ? fmt.getFloat("averageBitrate") : fmt.getFloat("bitrate")) / 1000 : 0f)
                        .add("width", fmt.getInt("width"))
                );
            }
            for (JSON fmt : streamingFormats) {
                if (fmt.has("drmFamilies") || fmt.has("drm_families")) {
                    continue;
                }
                String url = fmt.getString("url");
                String cipher = "";
                JSON urlData;
                if (url.isEmpty()) {
                    cipher = fmt.has("cipher") ? fmt.getString("cipher") : fmt.getString("signatureCipher");
                    if (cipher.isEmpty()) {
                        continue;
                    }
                    urlData = JSON.parseQueryString(cipher);
                    url = urlData.getString("url");
                    if (url.isEmpty()) {
                        continue;
                    }
                } else {
                    try {
                        urlData = JSON.parseQueryString(new URL(fmt.getString("url")).getQuery());
                    } catch (MalformedURLException e) {
                        urlData = JSON.NULL;
                    }
                }

                int streamType = urlData.getInt("stream_type", -1);
                if (streamType == 3) {
                    // Unsupported FORMAT_STREAM_TYPE_OTF
                    continue;
                }
                String formatId = fmt.has("itag") ? fmt.getString("itag") : urlData.getString("itag");
                if (formatId.isEmpty()) {
                    continue;
                }
                if (!cipher.isEmpty()) {
                    if (urlData.has("s")) {
                        final String ASSETS_RE = "\"assets\":.+?\"js\":\\s*(\"[^\"]+\")";
                        String jsPlayerUrlJson = RegexUtils.search(ASSETS_RE, ageGate ? embedWebPage : videoWebPage);
                        if (jsPlayerUrlJson.isEmpty() && !ageGate) {
                            if (embedWebPage.isEmpty()) {
                                embedWebPage = Utils.downloadWebPage("https://www.youtube.com/embed/" + videoId);
                            }
                            jsPlayerUrlJson = RegexUtils.search(ASSETS_RE, embedWebPage);
                        }
                        playerUrl = unescapeJava(JSON.parseJsonString(jsPlayerUrlJson).getAsString());
                        if (playerUrl.isEmpty()) {
                            String playerUrlJson = RegexUtils.search("ytplayer\\.config.*?\"url\"\\s*:\\s*(\"[^\"]+\")", videoWebPage);
                            playerUrl = unescapeJava(JSON.parseJsonString(playerUrlJson).getAsString());
                        }
                    }
                    if (urlData.has("sig")) {
                        url += "&signature=" + urlData.getString("sig");
                    } else if (urlData.has("s")) {
                        String encryptedSig = urlData.getString("s");
                        String signature;
                        try {
                            signature = decryptSignature(encryptedSig, playerUrl);
                        } catch (ExtractException e) {
                            Log.w(TAG, "Failed to decrypt signature of the video");
                            e.printStackTrace();
                            continue;
                        }
                        String sp = urlData.getString("sp", "signature");
                        url += "&" + sp + '=' + signature;
                    }
                }
                if (!url.contains("ratebypass")) {
                    url += "&ratebypass=yes";
                }
                JSON dct = JSON.createObject();
                dct.add("format_id", formatId);
                dct.add("url", url);
                if (Formats.has(formatId)) {
                    Formats.get(formatId).forEach((BiConsumer<String, JSON>) dct::add);
                }
                if (formatsSpec.has(formatId)) {
                    formatsSpec.get(formatId).forEach((BiConsumer<String, JSON>) dct::add);
                }
                m = Pattern.compile("^(?<width>\\d+)[xX](?<height>\\d+)$").matcher(urlData.getString("size"));
                int width = 0, height = 0;
                if (m.find()) {
                    width = NumberUtils.toInt(m.group("width"));
                    height = NumberUtils.toInt(m.group("height"));
                }
                if (width == 0) {
                    width = fmt.getInt("width");
                }
                if (height == 0) {
                    height = fmt.getInt("height");
                }
                int fileSize = urlData.has("clen") ? urlData.getInt("clen") : extractFileSize(url);
                String quality = urlData.has("quality") ? urlData.getString("quality") : fmt.getString("quality");
                String quality_label = urlData.has("quality_label") ? urlData.getString("quality_label") : fmt.getString("qualityLabel");
                float tbr = !formatId.equals("43") ? (urlData.has("bitrate") ? urlData.getFloat("bitrate") : fmt.getFloat("bitrate")) / 1000 : 0f;
                int fps = urlData.has("fps") ? urlData.getInt("fps") : fmt.getInt("fps");
                if (fileSize != 0) dct.add("filesize", fileSize);
                if (tbr != 0) dct.add("tbr", tbr);
                if (width != 0) dct.add("width", width);
                if (height != 0) dct.add("height", height);
                if (fps != 0) dct.add("fps", fps);
                if (!quality_label.isEmpty()) {
                    dct.add("format_note", quality_label);
                } else if (!quality.isEmpty()) {
                    dct.add("format_note", quality);
                }

                String type = urlData.has("type") ? urlData.getString("type") : fmt.getString("mimeType");
                if (!type.isEmpty()) {
                    String[] typeSplit = type.split(";");
                    String[] kindExt = typeSplit[0].split("/");
                    if (kindExt.length == 2) {
                        String kind = kindExt[0];
                        dct.add("ext", ExtractorUtils.mimeType2ext(typeSplit[0]));
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
                                Pair<String, String> codecPair = ExtractorUtils.parseCodecs(codecs);
                                dct.add("vcodec", codecPair.first);
                                dct.add("acodec", codecPair.second);
                            }
                        }
                    }
                }
                if (dct.getString("acodec").isEmpty() || dct.getString("vcodec").isEmpty()) {
                    // Youtube throttles chunks >~10M
                    dct.add("http_chunk_size", 10485760);
                }
                if (!dct.getString("acodec").isEmpty() && dct.getString("vcodec").isEmpty()) {
                    dct.add("mediaType", TYPE_AUDIO);
                } else if (dct.getString("acodec").isEmpty() && !dct.getString("vcodec").isEmpty()) {
                    dct.add("mediaType", TYPE_VIDEO);
                } else {
                    dct.add("mediaType", TYPE_NORMAL);
                }
                formats.add(new YtStream(dct));
            }
        } else {
            String manifestUrl = playerResponse.get("streamingData").getString("hlsManifestUrl", videoInfo.getString("hlsvp"));
            if (!manifestUrl.isEmpty()) {
                // TODO: add m3u8 support
                throw new NotImplementedException("m3u8 format not implemented");
            } else {
                return new ExtractedInfo(ErrorCode.NO_INFO, "No conn, hlsvp, hlsManifestUrl or url_encoded_fmt_stream_map information found in video info");
            }
        }

        String videoTitle = videoInfo.has("title") ? videoInfo.getString("title") : videoDetails.getString("title");
        String channel = videoInfo.has("author") ? videoInfo.getString("author") : videoDetails.getString("author");
        int videoDuration = videoInfo.has("length_seconds") ? videoInfo.getInt("length_seconds") : videoDetails.getInt("lengthSeconds");
        res.setTitle(videoTitle);
        res.setChannel(channel);
        res.setDuration(videoDuration);

        // TODO: look for DASH manifest

        // Check for malformed aspect ratio
        m = Pattern.compile("<meta\\s+property=\\\"og:video:tag\\\".*?content=\\\"yt:stretch=(?<w>[0-9]+):(?<h>[0-9]+)\\\">").matcher(videoWebPage);
        if (m.find()) {
            float w = NumberUtils.toFloat(m.group("w"));
            float h = NumberUtils.toFloat(m.group("h"));
            if (w > 0 && h > 0) {
                float ratio = w / h;
                for (YtStream fmt : formats) {
                    if (fmt.vCodec.equals("none")) {
                        fmt.stretchedRatio = ratio;
                    }
                }
            }
        }
        if (formats.size() == 0 && (videoInfo.has("license_info") || (playerResponse.get("streamingData").has("licenseInfos")))) {
            return new ExtractedInfo(ErrorCode.DRM, "The video is drm protected");
        }
        res.setFormats(formats);
        return res;
    }

    @WorkerThread
    public ExtractedInfo extract(String videoId) {
        try {
            return realExtract(videoId);
        } catch (Exception e) {
            return new ExtractedInfo(ErrorCode.UNEXPECTED, e.getMessage());
        }
    }
}
