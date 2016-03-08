package com.chris_guzman.dora;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxDM mapboxDM;
    private Geometry mGeometry;
    private LatLng startingLine = new LatLng(39.403057, -76.601754);
    private LatLng innerHarbor = new LatLng(39.285762, -76.608490);
    private BehaviorSubject<LatLng> driverPosObs = BehaviorSubject.create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) super.findViewById(R.id.mapview);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        boolean mock = true;
        if (mock) {
            startingLine = new LatLng(39.403057, -76.601754);
        }
        else {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.d("CHRIS", "can't do that");
                return;
            }
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            startingLine = new LatLng(latitude, longitude);
        }
        mapView.setCenterCoordinate(startingLine);
        mapView.addMarker(new MarkerOptions().position(startingLine));
        mapView.addMarker(new MarkerOptions().position(innerHarbor));
        mapView.setZoomLevel(10);
        mapView.onCreate(savedInstanceState);
        MapboxAPI apiClient = MapboxClient.getClient(this);
        mapboxDM = MapboxDM.init(apiClient);
        mapboxDM.getDirections(startingLine, innerHarbor, this.getResources().getString(R.string.access_token))
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
            Routes route = mapboxDirections.getRoutes().get(0);
            if (route.getGeometry() != null && !route.getGeometry().getCoordinates().isEmpty()) {
                mGeometry = route.getGeometry();
                new DrawGeoJSON().execute();
                startDriverTrackingForNav(mGeometry.getCoordinates());
            }
            else {
                Toast.makeText(this, "Unexpected error", Toast.LENGTH_LONG).show();
            }
        }
        else if (mapboxDirections != null && mapboxDirections.getError() != null) {
            //Mapbox will return a 200 with an error string if the request is valid, but it is unable to find a route.
            Toast.makeText(this, mapboxDirections.getError(), Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "Unexpected error", Toast.LENGTH_LONG).show();
        }

    }

    private void startDriverTrackingForNav(final ArrayList coordinates) {
            Toast.makeText(this, "Getting your location...", Toast.LENGTH_LONG).show();
            final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            private int idx = 0;

            @Override
            public void run() {
                if (idx < coordinates.size()) {
                    ArrayList<Double> coordinate = (ArrayList) coordinates.get(idx);
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .zoom(16)
                            .target(new LatLng(coordinate.get(1), coordinate.get(0)))
                            .bearing(180)                               // Sets the orientation of the camera to look south
                            .tilt(85)                                   // Sets the tilt of the camera to 20 degrees
                            .build();

                    mapView.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 250, null);
                    idx++;
                    handler.postDelayed(this, 250);
                }
            }
        }, 2000);
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
