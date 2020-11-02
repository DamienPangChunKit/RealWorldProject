package com.example.damien.realworldproject;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.traffic.TrafficPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Timer;
import java.util.TimerTask;


public class staffLocation extends AppCompatActivity implements OnMapReadyCallback{

    //public static final always put on the top
    public static final int VIEW_CUSTOMER_DESTINATION = 38;
    public static final int VIEW_STAFF_LOCATION = 37;

    private int id;

    //main variables
    private Double destLatitude;
    private Double destLongtitude;
    private LatLng latLng;

    private MapView mapView;
    private MapboxMap map;
    private Marker destinationMarker;
    private Marker staffLocationMarker;
    private TrafficPlugin trafficPlugin;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_staff_location);

        Bundle i = getIntent().getExtras();

        id = i.getInt(String.valueOf(1));
        //pass to track staff location activity
        //testing the variables
        destLatitude = i.getDouble(appointment.EXTRA_LATITUDE);
        destLongtitude = i.getDouble(appointment.EXTRA_LONGTITUDE);

        latLng = new LatLng(destLatitude,destLongtitude);

        //Testing working normal or totally crashed
        //currently no crashed
        Toast.makeText(staffLocation.this, destLatitude + " + " + destLongtitude, Toast.LENGTH_SHORT).show();
        //end of testing here

        mapView = findViewById(R.id.staffMap);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new Background().execute();
            }
        },0, 10000);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        map = mapboxMap;
        map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @SuppressLint("MissingPermission")
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                trafficPlugin = new TrafficPlugin(mapView, map, style);
                trafficPlugin.setVisibility(true);
                LocationComponent locationComponent = map.getLocationComponent();
                locationComponent.activateLocationComponent(staffLocation.this, style);
                locationComponent.setLocationComponentEnabled(true);
                locationComponent.setCameraMode(CameraMode.TRACKING_GPS);

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .zoom(16)
                        .target(latLng)
                        .build();
                IconFactory iconFactory = IconFactory.getInstance(staffLocation.this);
                Icon icon = iconFactory.fromResource(R.drawable.blue_marker);
                destinationMarker = map.addMarker(new MarkerOptions()
                        .icon(icon)
                        .position(latLng)
                        .setTitle("Customer Destination"));

                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 4000);
                startTimer();
            }
        });
    }

    public class Background extends AsyncTask<Void, Void, LatLng> {
        private static final String LIBRARY = "com.mysql.jdbc.Driver";

        private static final String USERNAME = "sql12372307";
        private static final String DB_NAME = "sql12372307";
        private static final String PASSWORD = "LYyljvuyn8";
        private static final String SERVER = "sql12.freemysqlhosting.net";

        private Connection conn;
        private PreparedStatement stmt;

        public Background() {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        @Override
        protected void onPostExecute(LatLng latLng1) {
            super.onPostExecute(latLng1);
            if (latLng1 == null) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .zoom(16)
                        .target(latLng)
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 4000);
                Toast.makeText(staffLocation.this, "Staff haven't start his/her journey", Toast.LENGTH_SHORT).show();
            }
            else if (staffLocationMarker == null) {
                IconFactory iconFactory = IconFactory.getInstance(staffLocation.this);
                Icon icon = iconFactory.fromResource(R.drawable.red_marker);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng1)
                        .setTitle("Staff Current Location")
                        .icon(icon);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .zoom(16)
                        .target(latLng1)
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 4000);
                staffLocationMarker = map.addMarker(markerOptions);
            }
            else if (staffLocationMarker.getPosition() != latLng1) {
                staffLocationMarker.setPosition(latLng1);
            }
        }

        @Override
        protected LatLng doInBackground(Void... voids) {
            conn = connectDB();
            if (conn == null) {
                return null;
            }
            LatLng latLng1 = null;
            try {
                String query = "SELECT latitude, longitude FROM gps_tracking WHERE service_id=? ORDER BY id DESC LIMIT 1";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, id);
                ResultSet result = stmt.executeQuery();
                if (result.next()) {
                    latLng1 = new LatLng(result.getDouble(1), result.getDouble(2));
                }
                result.close();
            }
            catch (Exception e) {
                Log.e("ERROR MySQL Statement", e.getMessage());
            }
            return latLng1;
        }
        private Connection connectDB() {
            try {
                Class.forName(LIBRARY);
                return DriverManager.getConnection("jdbc:mysql://" + SERVER + "/" + DB_NAME, USERNAME, PASSWORD);
            }
            catch (Exception e) {
                Log.e("Error on Connection", e.getMessage());
                return null;
            }
        }

        public void closeConn () {
            try { stmt.close(); } catch (Exception e) { /* ignored */ }
            try { conn.close(); } catch (Exception e) { /* ignored */ }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
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
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (timer != null)
            timer.cancel();
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}
