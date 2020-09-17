package com.zionhuang.music.extractor;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.zionhuang.music.utils.Utils.URLDecode;

/**
 * A wrapper around {@link com.google.gson.JsonElement}.
 * Use Json in a convenience way.
 * No exceptions, no null exceptions.
 *
 * @author Zion Huang
 * @version 1.0
 */
public class JSON implements Iterable<JSON> {
    private class Itr implements Iterator<JSON> {
        int cursor = 0;

        @Override
        public boolean hasNext() {
            return cursor < size();
        }

        @Override
        public JSON next() {
            int i = cursor++;
            if (i >= size()) {
                throw new NoSuchElementException();
            }
            return get(i);
        }
    }

    public static final JSON NULL = new JSON(JsonNull.INSTANCE);
    private JsonElement mValue;

    public JSON(JsonElement element) {
        mValue = element;
    }

    public static JSON createArray() {
        return new JSON(new JsonArray());
    }

    public static JSON createObject() {
        return new JSON(new JsonObject());
    }

    public static JSON parseJsonString(String s) {
        try {
            return new JSON(JsonParser.parseString(s));
        } catch (JsonSyntaxException | JsonIOException e) {
            return NULL;
        }
    }

    @NonNull
    public static JSON parseQueryString(String qs) {
        if (qs == null || qs.isEmpty()) {
            return JSON.NULL;
        }
        JSON res = JSON.createObject();
        String[] pairs = qs.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                res.add(Objects.requireNonNull(URLDecode(pair.substring(0, idx))), URLDecode(pair.substring(idx + 1)));
            } catch (NullPointerException ignored) {
            }
        }
        return res;
    }

    public static JSON fromBoolean(Boolean b) {
        return new JSON(new JsonPrimitive(b));
    }

    public static JSON fromNumber(Number n) {
        return new JSON(new JsonPrimitive(n));
    }

    public static JSON fromChar(Character c) {
        return new JSON(new JsonPrimitive(c.toString()));
    }

    public static JSON fromString(String s) {
        return new JSON(new JsonPrimitive(s));
    }

    public static JSON fromArray(JsonArray array) {
        return new JSON(array);
    }

    public static JSON fromObject(JsonObject object) {
        return new JSON(object);
    }

    public static JSON fromFunction(JSFunction fn) {
        return new JSON(fn);
    }

    public static JSON toJSON(Object o) {
        if (o instanceof JSON) return (JSON) o;
        if (o instanceof Boolean) return fromBoolean((Boolean) o);
        if (o instanceof Number) return fromNumber((Number) o);
        if (o instanceof String) return fromString((String) o);
        if (o instanceof Character) return fromString(o.toString());
        if (o.getClass().isArray()) {
            throw new IllegalArgumentException("Passing array is denied");
        }
        return NULL;
    }

    public static JSON toArray(Object... args) {
        JSON res = createArray();
        for (Object o : args) {
            res.add(toJSON(o));
        }
        return res;
    }

    public boolean isNull() {
        return mValue == JsonNull.INSTANCE;
    }

    public boolean isPrimitive() {
        return mValue instanceof JsonPrimitive;
    }

    public boolean isBoolean() {
        return isPrimitive() && mValue.getAsJsonPrimitive().isBoolean();
    }

    public boolean isNumber() {
        return isPrimitive() && mValue.getAsJsonPrimitive().isNumber();
    }

    public boolean isString() {
        return isPrimitive() && mValue.getAsJsonPrimitive().isString();
    }

    public boolean isArray() {
        return mValue instanceof JsonArray;
    }

    public boolean isObject() {
        return mValue instanceof JsonObject;
    }

    public boolean isFunction() {
        return mValue instanceof JSFunction;
    }

    public boolean getAsBoolean() {
        return getAsBoolean(false);
    }

    public boolean getAsBoolean(boolean defaultValue) {
        return isPrimitive() ? mValue.getAsBoolean() : defaultValue;
    }

    @NonNull
    public Number getAsNumber() {
        return getAsNumber(0);
    }

    public Number getAsNumber(Number defaultValue) {
        if (isPrimitive()) {
            try {
                return mValue.getAsNumber();
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    public int getAsInt() {
        return getAsInt(0);
    }

    public int getAsInt(int defaultValue) {
        return getAsNumber(defaultValue).intValue();
    }

    public float getAsFloat() {
        return getAsFloat(0f);
    }

    public float getAsFloat(float defaultValue) {
        return isNumber() ? getAsNumber(defaultValue).floatValue() : defaultValue;
    }

    @NonNull
    public String getAsString() {
        return getAsString("");
    }

    public String getAsString(String defaultValue) {
        return isPrimitive() ? mValue.getAsString() : defaultValue;
    }

    public JSFunction getAsFunction() {
        return isFunction() ? (JSFunction) mValue : null;
    }

    @Override
    public boolean equals(Object o) {
        return (o == this) || (o instanceof JSON && mValue.equals(((JSON) o).mValue));
    }

    public boolean equals(Number n) {
        return isNumber() && Objects.equals(getAsNumber(), n);
    }

    public boolean equals(String s) {
        return isString() && Objects.equals(getAsString(), s);
    }

    /**
     * JsonArray methods
     */

    public JSON add(JSON element) {
        if (isArray()) {
            mValue.getAsJsonArray().add(element.mValue);
        }
        return this;
    }

    public JSON add(int element) {
        return add(fromNumber(element));
    }

    public JSON add(char c) {
        return add(fromChar(c));
    }

    public void set(int index, JSON element) {
        if (isArray()) {
            if (index < mValue.getAsJsonArray().size()) {
                mValue.getAsJsonArray().set(index, element.mValue);
            }
        }
    }

    public JSON remove(int index) {
        if (isArray()) {
            if (index < mValue.getAsJsonArray().size()) {
                return new JSON(mValue.getAsJsonArray().remove(index));
            }
        }
        return NULL;
    }

    public boolean remove(JSON element) {
        if (isArray()) {
            return mValue.getAsJsonArray().remove(element.mValue);
        }
        return false;
    }

    public boolean contains(JSON element) {
        if (isArray()) {
            return mValue.getAsJsonArray().contains(element.mValue);
        }
        return false;
    }

    public JSON get(int index) {
        if (isArray()) {
            return index < size() ? new JSON(mValue.getAsJsonArray().get(index)) : NULL;
        }
        return NULL;
    }

    public int getInt(int index) {
        return get(index).getAsInt();
    }

    public String getString(int index) {
        return get(index).getAsString();
    }

    @NonNull
    @Override
    public Iterator<JSON> iterator() {
        return new Itr();
    }

    @Override
    public void forEach(@NonNull Consumer<? super JSON> action) {
        if (isArray()) {
            for (JsonElement ele : mValue.getAsJsonArray()) {
                action.accept(new JSON(ele));
            }
        }
    }

    /**
     * JsonObject methods
     */
    public JSON add(String key, Boolean value) {
        return add(key, fromBoolean(value));
    }

    public JSON add(String key, Number value) {
        return add(key, fromNumber(value));
    }

    public JSON add(String key, String value) {
        return add(key, fromString(value));
    }

    public JSON add(String key, JSON value) {
        if (isObject()) {
            mValue.getAsJsonObject().add(key, value.mValue);
        }
        return this;
    }

    public boolean has(String key) {
        if (isObject()) {
            return mValue.getAsJsonObject().has(key);
        }
        return false;
    }

    @NonNull
    public JSON get(String key) {
        if (isObject()) {
            return mValue.getAsJsonObject().has(key) ? new JSON(mValue.getAsJsonObject().get(key)) : NULL;
        }
        return NULL;
    }

    public boolean getBoolean(String key) {
        return get(key).getAsBoolean();
    }

    public Number getNumber(String key) {
        return get(key).getAsNumber();
    }

    public int getInt(String key) {
        return get(key).getAsInt();
    }

    public int getInt(String key, int defaultValue) {
        return get(key).getAsInt(defaultValue);
    }

    public float getFloat(String key) {
        return get(key).getAsFloat();
    }

    public String getString(String key) {
        return get(key).getAsString();
    }

    public String getString(String key, String defaultValue) {
        return get(key).getAsString(defaultValue);
    }

    public JSON getJsonArray(String key) {
        return get(key);
    }

    public void forEach(@NonNull BiConsumer<String, JSON> action) {
        if (isObject()) {
            for (Map.Entry<String, JsonElement> entry : mValue.getAsJsonObject().entrySet()) {
                action.accept(entry.getKey(), new JSON(entry.getValue()));
            }
        }
    }

    /**
     * Both JsonArray and JsonObject methods
     */
    public int size() {
        if (isObject()) {
            return mValue.getAsJsonObject().size();
        }
        if (isArray()) {
            return mValue.getAsJsonArray().size();
        }
        return 0;
    }

    @NonNull
    @Override
    public String toString() {
        return mValue.toString();
    }
}
