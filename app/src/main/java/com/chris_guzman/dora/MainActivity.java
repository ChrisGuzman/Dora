package com.chris_guzman.dora;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxDM mapboxDM;
    private Geometry mGeometry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) super.findViewById(R.id.mapview);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        LatLng towsonMall = new LatLng(39.403057, -76.601754);
        LatLng innerHarbor = new LatLng(39.285762, -76.608490);
        mapView.setCenterCoordinate(towsonMall);
        mapView.addMarker(new MarkerOptions().position(towsonMall));
        mapView.addMarker(new MarkerOptions().position(innerHarbor));
        mapView.setZoomLevel(10);
        mapView.onCreate(savedInstanceState);
        MapboxAPI apiClient = MapboxClient.getClient(this);
        mapboxDM = MapboxDM.init(apiClient);
        mapboxDM.getDirections(towsonMall, innerHarbor, this.getResources().getString(R.string.access_token))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MapboxDirections>() {
                    @Override
                    public void call(MapboxDirections mapboxDirections) {
                        MainActivity.this.onSuccess(mapboxDirections);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MainActivity.this.onError(throwable);
                    }
                });
    }

    private void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    private void onSuccess(MapboxDirections mapboxDirections) {
        if (mapboxDirections != null && mapboxDirections.getRoutes() != null && !mapboxDirections.getRoutes().isEmpty()) {
            Routes routes = mapboxDirections.getRoutes().get(0);
            mGeometry = routes.getGeometry();
            new DrawGeoJSON().execute();
        }
    }

    private class DrawGeoJSON extends AsyncTask<Void, Void, List<LatLng>> {
        @Override
        protected List<LatLng> doInBackground(Void... voids) {

            ArrayList<LatLng> points = new ArrayList<LatLng>();

            ArrayList coordinates = mGeometry.getCoordinates();
            for (int lc = 0; lc < coordinates.size(); lc++) {
                ArrayList<Double> coordinate = (ArrayList) coordinates.get(lc);
                LatLng latLng = new LatLng(coordinate.get(1), coordinate.get(0));
                points.add(latLng);
            }

            return points;
        }

        @Override
        protected void onPostExecute(List<LatLng> points) {
            super.onPostExecute(points);

            if (points.size() > 0) {
                LatLng[] pointsArray = points.toArray(new LatLng[points.size()]);

                // Draw Points on MapView
                mapView.addPolyline(new PolylineOptions()
                        .add(pointsArray)
                        .color(Color.parseColor("#3bb2d0"))
                        .width(2));
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause()  {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}
