package com.chris_guzman.dora;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Copyright (c) 2015 OrderUp. All rights reserved.
 */
public interface MapboxAPI {

    @GET("/{wayPoints}.json")
    Observable<MapboxDirections>getDirections(@Path("wayPoints") String waypoints, @Query("access_token") String accessToken, @Query("alternatives") boolean alternatives);

}

