package com.chris_guzman.dora;

/**
 * Copyright (c) 2015 OrderUp. All rights reserved.
 */
public class Routes {
    private long distance;
    private long duration;
    private String summary;
    private Geometry geometry;

    public long getDistance() {
        return distance;
    }

    public long getDuration() {
        return duration;
    }

    public String getSummary() {
        return summary;
    }

    public Geometry getGeometry() {
        return geometry;
    }
}
