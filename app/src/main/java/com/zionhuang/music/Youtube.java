package com.zionhuang.music;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Image;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Youtube {
    private static Youtube instance;
    private Context ctx;
    private static DateFormat df;

    @SuppressLint("SimpleDateFormat")
    private Youtube(Context context) {
        ctx = context;
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static synchronized Youtube getInstance(Context context) {
        if (instance == null) {
            instance = new Youtube(context);
        }
        return instance;
    }

    public static abstract class Item {
        protected String id;
        protected String title;
        protected String channelTitle;
        protected Date publishDate;
        protected String thumbnailURL;

        public String getTitle() {
            return title;
        }

        public abstract String getDescription();

        public abstract String getThumbnailURL();
    }

    public static class Video extends Item {
        Video(JSONObject jsonObject) {
            try {
                id = jsonObject.getJSONObject("id").getString("videoId");
                JSONObject snippet = jsonObject.getJSONObject("snippet");
                title = snippet.getString("title");
                channelTitle = snippet.getString("channelTitle");
                publishDate = df.parse(snippet.getString("publishedAt"));
                thumbnailURL = snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.getErrorOffset();
            }
        }

        @Override
        public String getDescription() {
            return channelTitle;
        }

        @Override
        public String getThumbnailURL() {
            return thumbnailURL;
        }
    }

    public static class Channel extends Item {
        Channel(JSONObject jsonObject) {
            try {
                id = jsonObject.getJSONObject("id").getString("channelId");
                JSONObject snippet = jsonObject.getJSONObject("snippet");
                title = snippet.getString("title");
                channelTitle = snippet.getString("channelTitle");
                publishDate = df.parse(snippet.getString("publishedAt"));
                thumbnailURL = snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.getErrorOffset();
            }
        }

        @Override
        public String getDescription() {
            return channelTitle;
        }

        @Override
        public String getThumbnailURL() {
            return thumbnailURL;
        }
    }

    public static class Playlist extends Item {
        Playlist(JSONObject jsonObject) {
            try {
                id = jsonObject.getJSONObject("id").getString("playlistId");
                JSONObject snippet = jsonObject.getJSONObject("snippet");
                title = snippet.getString("title");
                channelTitle = snippet.getString("channelTitle");
                publishDate = df.parse(snippet.getString("publishedAt"));
                thumbnailURL = snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.getErrorOffset();
            }
        }

        @Override
        public String getDescription() {
            return channelTitle;
        }

        @Override
        public String getThumbnailURL() {
            return thumbnailURL;
        }
    }
}
