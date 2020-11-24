package com.zionhuang.music.utils;

import android.content.Context;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Utils {
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

    public static String getMaxResThumbnailUrl(ThumbnailDetails thumbnails) {
        Thumbnail timg;
        timg = thumbnails.getMaxres();
        if (timg != null) {
            return timg.getUrl();
        }
        timg = thumbnails.getHigh();
        if (timg != null) {
            return timg.getUrl();
        }
        timg = thumbnails.getMedium();
        if (timg != null) {
            return timg.getUrl();
        }
        timg = thumbnails.getStandard();
        if (timg != null) {
            return timg.getUrl();
        }
        timg = thumbnails.getDefault();
        if (timg != null) {
            return timg.getUrl();
        }
        return null;
    }
}
