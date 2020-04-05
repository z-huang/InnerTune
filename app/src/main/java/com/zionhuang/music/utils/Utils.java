package com.zionhuang.music.utils;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static String downloadWebPage(String url) {
        try {
            return Jsoup.connect(url).timeout(60 * 1000).get().html();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String downloadPlainText(String url) {
        try {
            return Jsoup.connect(url).ignoreContentType(true).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String URLEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF8");
        } catch (UnsupportedEncodingException ignore) {

        }
        return null;
    }

    @Nullable
    public static String URLDecode(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        try {
            return URLDecoder.decode(s, "UTF8");
        } catch (UnsupportedEncodingException ignore) {

        }
        return null;
    }

    public static JsonObject parseQueryString(String qs) {
        if (qs == null || qs.length() == 0) {
            return null;
        }
        JsonObject jsonObject = new JsonObject();
        String[] pairs = qs.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                jsonObject.addProperty(Objects.requireNonNull(URLDecode(pair.substring(0, idx))), URLDecode(pair.substring(idx + 1)));
            } catch (NullPointerException ignored) {

            }
        }
        return jsonObject;
    }

    public static JsonElement parseJsonString(String json) {
        return JsonParser.parseReader(new JsonReader(new StringReader(json)));
    }

    public static String searchRegex(String regex, CharSequence s, String group) {
        Matcher m = Pattern.compile(regex).matcher(s);
        if (m.find()) {
            if (group == null) {
                return m.groupCount() > 0 ? m.group(1) : m.group(0);
            } else {
                return m.group(group);
            }
        }
        return "";
    }

    public static String searchRegex(String regex, CharSequence s) {
        return searchRegex(regex, s, null);
    }

    public static String searchRegex(String[] regexs, CharSequence s, String group) {
        Matcher m;
        for (String regex : regexs) {
            m = Pattern.compile(regex).matcher(s);
            if (m.find()) {
                if (group == null) {
                    return m.groupCount() > 0 ? m.group(1) : m.group(0);
                } else {
                    return m.group(group);
                }
            }
        }
        return "";
    }

    public static String searchRegex(String[] regexs, CharSequence s) {
        return searchRegex(regexs, s, null);
    }

    public static String removeQuotes(String s) {
        if (s == null || s.length() < 2) {
            return s;
        }
        for (char quote : new char[]{'\"', '\''}) {
            if (s.charAt(0) == quote && s.charAt(s.length() - 1) == quote) {
                return s.substring(1, s.length() - 1);
            }
        }
        return s;
    }

    public static String strip(String s, String chars) {
        return s.replaceAll("^[" + chars + "]+|[" + chars + "]+$", "");
    }

    public static String rStrip(String s, String chars) {
        return s.replaceAll("["+chars+"]+$", "");
    }

    public static String[] rPartition(String s, char sep) {
        int len = s.length();
        for (int i = len - 1; i >= 0; i--) {
            if (s.charAt(i) == sep) {
                return new String[]{s.substring(0, i), Character.toString(sep), s.substring(i + 1, len)};
            }
        }
        return new String[]{s, "", ""};
    }
}
