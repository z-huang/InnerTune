package com.zionhuang.music.extractor;

import androidx.annotation.NonNull;

import com.google.gson.JsonElement;

public abstract class JSFunction extends JsonElement {
    public abstract JSON apply(JSON args) throws JSInterpreter.InterpretException;

    @Override
    public JsonElement deepCopy() {
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return "[Json Function]";
    }
}
