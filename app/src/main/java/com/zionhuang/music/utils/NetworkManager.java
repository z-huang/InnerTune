package com.zionhuang.music.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class NetworkManager {
    private static volatile NetworkManager mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;


    private NetworkManager(Context context) {
        mRequestQueue = Volley.newRequestQueue(context, 1024 * 1024 * 10);

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap> cache = new LruCache<>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static NetworkManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (NetworkManager.class) {
                if (mInstance == null) {
                    mInstance = new NetworkManager(context);
                }
            }
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        mRequestQueue.add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
