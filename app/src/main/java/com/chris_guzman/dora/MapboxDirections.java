package com.chris_guzman.dora;

import java.util.ArrayList;

/**
 * Copyright (c) 2015 OrderUp. All rights reserved.
 */
public class MapboxDirections {
    private String error;
    private ArrayList<Routes> routes;

    public ArrayList<Routes> getRoutes() {
        return routes;
    }

    public String getError() {
        return error;
    }
}
