package com.zionhuang.music.utils;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.io.UnsupportedEncodingException;

public class JsonObjectRequest extends JsonRequest<JsonObject> {
    public JsonObjectRequest(int method, String url, String requestBody, Response.Listener<JsonObject> listener, Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
    }

    public JsonObjectRequest(String url, Response.Listener<JsonObject> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, null, listener, errorListener);
    }

    public JsonObjectRequest(int method, String url, Response.Listener<JsonObject> listener, Response.ErrorListener errorListener) {
        super(method, url, null, listener, errorListener);
    }

    public JsonObjectRequest(int method, String url, JsonObject jsonRequest, Response.Listener<JsonObject> listener, Response.ErrorListener errorListener) {
        super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener, errorListener);
    }

    public JsonObjectRequest(String url, JsonObject jsonRequest, Response.Listener<JsonObject> listener, Response.ErrorListener errorListener) {
        this(jsonRequest == null ? Method.GET : Method.POST, url, jsonRequest, listener, errorListener);
    }

    @Override
    protected Response<JsonObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(JsonParser.parseString(jsonString).getAsJsonObject(),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | IllegalStateException e) {
            return Response.error(new ParseError(e));
        }
    }
}
