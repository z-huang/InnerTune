package com.zionhuang.music.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;

import okhttp3.OkHttpClient;

@GlideModule
public class OkHttpGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, Registry registry) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
        registry.append(GlideUrl.class, InputStream.class, factory);
    }
}
