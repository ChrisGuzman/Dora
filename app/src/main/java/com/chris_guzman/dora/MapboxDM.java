package com.chris_guzman.dora;

import com.mapbox.mapboxsdk.geometry.LatLng;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Copyright (c) 2015 OrderUp. All rights reserved.
 */
public class MapboxDM {
    private MapboxAPI client;

    public Observable<MapboxDirections> getDirections(LatLng start, LatLng end, String accessToken){
        String wayPoints = start.getLongitude() + "," + start.getLatitude() + ";" + end.getLongitude() + "," + end.getLatitude();
        return client.getDirections(wayPoints, accessToken, false)
                .subscribeOn(Schedulers.io());
    }

    /* Singleton */
    private static MapboxDM instance;

    public static MapboxDM init(MapboxAPI client) {
        instance = new MapboxDM();
        instance.client = client;
        return instance;
    }

    public static MapboxDM getInstance() {
        return instance;
    }

    private MapboxDM() {

    }
}
