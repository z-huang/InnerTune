package com.zionhuang.music.utils;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.UnsupportedEncodingException;

public class JsonArrayRequest extends JsonRequest<JsonArray> {
    public JsonArrayRequest(int method, String url, String requestBody, Response.Listener<JsonArray> listener, Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
    }

    public JsonArrayRequest(String url, Response.Listener<JsonArray> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, null, listener, errorListener);
    }

    public JsonArrayRequest(int method, String url, Response.Listener<JsonArray> listener, Response.ErrorListener errorListener) {
        super(method, url, null, listener, errorListener);
    }

    public JsonArrayRequest(int method, String url, JsonObject jsonRequest, Response.Listener<JsonArray> listener, Response.ErrorListener errorListener) {
        super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener, errorListener);
    }

    public JsonArrayRequest(String url, JsonObject jsonRequest, Response.Listener<JsonArray> listener, Response.ErrorListener errorListener) {
        this(jsonRequest == null ? Method.GET : Method.POST, url, jsonRequest, listener, errorListener);
    }

    @Override
    protected Response<JsonArray> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(JsonParser.parseString(jsonString).getAsJsonArray(),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | IllegalStateException e) {
            return Response.error(new ParseError(e));
        }
    }
}
