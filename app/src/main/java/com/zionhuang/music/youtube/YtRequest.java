package com.zionhuang.music.youtube;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zionhuang.music.utils.JsonArrayRequest;
import com.zionhuang.music.utils.JsonObjectRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class YtRequest {
    private static final String ROOT_URL = "https://www.googleapis.com/youtube/v3/";
    private static final String API_KEY = "API_KEY";

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

    public static void suggest(RequestQueue requestQueue, String query, final YtRequest.Listener<JsonArray> listener) {
        try {
            String url = "https://clients1.google.com/complete/search?client=firefox&ds=yt&q=" + URLEncoder.encode(query, "UTF-8");
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(com.android.volley.Request.Method.GET, url, listener::onResponse, listener::onError);
            requestQueue.add(jsonArrayRequest);
        } catch (UnsupportedEncodingException ignored) {

        }
    }

    public static void search(RequestQueue requestQueue, Parameter parameter, final YtRequest.Listener<JsonObject> listener) {
        String url = ROOT_URL + "search?" + parameter.setKey(API_KEY).setPart("snippet").setSafeSearch("moderate").setMaxResults(20).toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, listener::onResponse, listener::onError);
        requestQueue.add(jsonObjectRequest);
    }
}
