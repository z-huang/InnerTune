package com.zionhuang.music.utils;

import android.content.Context;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Utils {
    @NonNull
    public static String downloadWebPage(String url) {
        try {
            return Jsoup.connect(url).timeout(60 * 1000).get().html();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
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
        if (s == null || s.isEmpty()) {
            return s;
        }
        try {
            return URLDecoder.decode(s, "UTF8");
        } catch (UnsupportedEncodingException ignore) {

        }
        return null;
    }

    @Nullable
    public static URL parseUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException ignored) {
        }
        return null;
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
        return s.replaceAll("[" + chars + "]+$", "");
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

    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static void replaceFragment(FragmentManager fragmentManager, @IdRes int id, Fragment fragment) {
        replaceFragment(fragmentManager, id, fragment, null, false);
    }

    public static void replaceFragment(FragmentManager fragmentManager, @IdRes int id, Fragment fragment, String tag) {
        replaceFragment(fragmentManager, id, fragment, tag, false);
    }

    public static void replaceFragment(FragmentManager fragmentManager, @IdRes int id, Fragment fragment, String tag, Boolean addToBackStack) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(id, fragment, tag);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    /**
     * Convert duration in seconds to formatted time string
     *
     * @param duration in seconds
     * @return formatted string
     */
    public static String makeTimeString(long duration) {
        int secs, minutes;
        long hours;
        hours = duration / 3600;
        duration %= 3600;
        minutes = (int) (duration / 60);
        duration %= 60;
        secs = (int) duration;
        String durationFormat = (hours == 0L) ? "%2$d:%3$02d" : "%1$d:%2$02d:%3$02d";
        return String.format(durationFormat, hours, minutes, secs);
    }
}
