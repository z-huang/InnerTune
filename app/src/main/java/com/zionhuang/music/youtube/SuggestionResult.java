package com.zionhuang.music.youtube;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class SuggestionResult {
    private String query;
    private List<String> suggestions;

    public SuggestionResult(String query, List<String> suggestions) {
        this.query = query;
        this.suggestions = suggestions;
    }

    public String getQuery() {
        return query;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public static final JsonDeserializer<SuggestionResult> deserializer = (json, typeOfT, context) -> {
        JsonArray jsonArray = json.getAsJsonArray();
        ArrayList<String> suggestions = new ArrayList<>();
        SuggestionResult result = new SuggestionResult(jsonArray.get(0).getAsString(), suggestions);
        for (JsonElement jsonElement : jsonArray.get(1).getAsJsonArray()) {
            suggestions.add(jsonElement.getAsString());
        }
        return result;
    };
}
