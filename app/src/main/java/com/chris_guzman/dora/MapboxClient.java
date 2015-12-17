package com.chris_guzman.dora;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;

/**
 * Copyright (c) 2015 OrderUp. All rights reserved.
 */
public class MapboxClient {
    static MapboxAPI serviceInstance;

    public static MapboxAPI getClient(final Context context) {
        if (serviceInstance == null) {
            serviceInstance = newClient(context, "https://api.mapbox.com/v4/directions/mapbox.driving/");
        }
        return serviceInstance;
    }

    public static MapboxAPI newClient(final Context context, String apiEnpoint) {

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);

        return new RestAdapter.Builder()
                .setEndpoint(apiEnpoint)
                .setLogLevel(retrofit.RestAdapter.LogLevel.FULL).setLog(new AndroidLog("RETRO"))
                .setClient(new OkClient(okHttpClient))
                .build()
                .create(MapboxAPI.class);
    }
}
