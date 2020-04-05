package com.zionhuang.music.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

public class Youtube {
    private static Youtube instance;
    private static final String ROOT_URL = "https://www.googleapis.com/youtube/v3/";
    private static final String API_KEY = "API_KEY";
    private static DateFormat df;

    @SuppressLint("SimpleDateFormat")
    private Youtube(Context context) {
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static synchronized Youtube getInstance(Context context) {
        if (instance == null) {
            instance = new Youtube(context);
        }
        return instance;
    }

    public static class Request {
        public interface Listener<T> {
            void onResponse(T response);

            void onError(Exception e);
        }

        public static class Parameter {
            private HashMap<String, Object> map;

            public Parameter() {
                map = new HashMap<>();
            }

            private void set(String key, Object value) {
                // don't replace value
                if (!map.containsKey(key)) {
                    map.put(key, value);
                }
            }

            public Parameter setKey(String key) {
                set("key", key);
                return this;
            }

            public Parameter setPart(String part) {
                set("part", part);
                return this;
            }

            public Parameter setSafeSearch(String type) {
                set("safeSearch", type);
                return this;
            }

            public Parameter setMaxResults(int maxResults) {
                set("maxResults", maxResults);
                return this;
            }

            public Parameter setQuery(String query) {
                try {
                    set("q", URLEncoder.encode(query, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // Theoretically, exception only happens when the encode type given is wrong.
                }
                return this;
            }

            public Parameter setPageToken(String token) {
                set("pageToken", token);
                return this;
            }

            @NonNull
            public String toString() {
                if (map.size() == 0) {
                    return "";
                }
                StringBuilder result = new StringBuilder();
                Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
                Map.Entry<String, Object> entry;
                while (iterator.hasNext()) {
                    entry = iterator.next();
                    result.append(entry.getKey()).append("=").append(entry.getValue().toString()).append("&");
                }
                result.setLength(result.length() - 1);
                return result.toString();
            }
        }

        public static void Suggest(RequestQueue requestQueue, String query, final Listener<JSONArray> listener) {
            try {
                String url = "https://clients1.google.com/complete/search?client=firefox&ds=yt&q=" + URLEncoder.encode(query, "UTF-8");
                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(com.android.volley.Request.Method.GET, url, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        listener.onResponse(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error);
                    }
                });
                requestQueue.add(jsonArrayRequest);
            } catch (UnsupportedEncodingException ignored) {

            }
        }

        public static void Search(RequestQueue requestQueue, Parameter parameter, final Listener<JSONObject> listener) {
            String url = ROOT_URL + "search?" + parameter.setKey(API_KEY).setPart("snippet").setSafeSearch("moderate").setMaxResults(20).toString();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    listener.onResponse(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    listener.onError(error);
                }
            });
            requestQueue.add(jsonObjectRequest);
        }
    }

    public static class Item {
        public static abstract class Base {
        }

        public static abstract class ItemBase extends Base {
            protected String id;
            protected String title;
            protected String channelTitle;
            protected Date publishDate;
            protected String thumbnailURL;

            public String getId() {
                return id;
            }

            public String getTitle() {
                return title;
            }

            public String getChannelTitle() {
                return channelTitle;
            }

            public abstract String getDescription();

            public abstract String getThumbnailURL();
        }

        public static class Loader extends Base {

        }

        public static class Video extends ItemBase {
            public Video(JSONObject jsonObject) {
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

        public static class Channel extends ItemBase {
            public Channel(JSONObject jsonObject) {
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

        public static class Playlist extends ItemBase {
            public Playlist(JSONObject jsonObject) {
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
}
