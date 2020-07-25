package com.zionhuang.music.extractor;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public abstract class JsonFunction extends JsonElement {
    @Override
    public JsonElement deepCopy() {
        return null;
    }

    public abstract JsonElement apply(JsonArray args) throws JSInterpreter.InterpretException;

    @NonNull
    @Override
    public String toString() {
        return "Json Function";
    }
}
