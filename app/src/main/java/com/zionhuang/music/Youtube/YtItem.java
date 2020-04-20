package com.zionhuang.music.Youtube;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class YtItem {
    public static abstract class Base {
    }

    public static abstract class BaseItem extends Base {
        private String idTag;
        protected String id;
        protected String title;
        protected String channelTitle;
        protected String publishDate;
        protected String thumbnailUrl;

        public BaseItem(String id, String title, String channelTitle, String publishedDate, String thumbnailUrl) {
            this.id = id;
            this.title = title;
            this.channelTitle = channelTitle;
            this.publishDate = publishedDate;
            this.thumbnailUrl = thumbnailUrl;
        }

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

    public static class Video extends BaseItem {
        Video(String id, String title, String channelTitle, String publishedDate, String thumbnailUrl) {
            super(id, title, channelTitle, publishedDate, thumbnailUrl);
        }

        @Override
        public String getDescription() {
            return channelTitle;
        }

        @Override
        public String getThumbnailURL() {
            return thumbnailUrl;
        }
    }

    public static class Channel extends BaseItem {
        Channel(String id, String title, String channelTitle, String publishedDate, String thumbnailUrl) {
            super(id, title, channelTitle, publishedDate, thumbnailUrl);
        }

        @Override
        public String getDescription() {
            return channelTitle;
        }

        @Override
        public String getThumbnailURL() {
            return thumbnailUrl;
        }
    }

    public static class Playlist extends BaseItem {
        Playlist(String id, String title, String channelTitle, String publishedDate, String thumbnailUrl) {
            super(id, title, channelTitle, publishedDate, thumbnailUrl);
        }

        @Override
        public String getDescription() {
            return channelTitle;
        }

        @Override
        public String getThumbnailURL() {
            return thumbnailUrl;
        }
    }

    public static class YtItemDeserializer implements JsonDeserializer<Base> {
        @Override
        public BaseItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonObject snippet = jsonObject.get("snippet").getAsJsonObject();
            String title = snippet.get("title").getAsString();
            String channelTitle = snippet.get("channelTitle").getAsString();
            String publishDate = snippet.get("publishedAt").getAsString();
            String thumbnailUrl = snippet.get("thumbnails").getAsJsonObject().get("medium").getAsJsonObject().get("url").getAsString();
            String kind = jsonObject.get("id").getAsJsonObject().get("kind").getAsString();
            String id;
            switch (kind) {
                case "youtube#video":
                    id = jsonObject.get("id").getAsJsonObject().get("videoId").getAsString();
                    return new Video(id, title, channelTitle, publishDate, thumbnailUrl);
                case "youtube#playlist":
                    id = jsonObject.get("id").getAsJsonObject().get("playlistId").getAsString();
                    return new Playlist(id, title, channelTitle, publishDate, thumbnailUrl);
                case "youtube#channel":
                    id = jsonObject.get("id").getAsJsonObject().get("channelId").getAsString();
                    return new Channel(id, title, channelTitle, publishDate, thumbnailUrl);
            }
            return null;
        }
    }
}
