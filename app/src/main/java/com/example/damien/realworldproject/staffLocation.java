package com.example.damien.realworldproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
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
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Timer;
import java.util.TimerTask;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;


public class staffLocation extends AppCompatActivity implements OnMapReadyCallback{

    //public static final always put on the top
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

    public static final String DESTINATION_SOURCE_ID = "destination-source-id";
    public static final String ICON_LAYER_ID = "icon-layer-id";
    public static final String RED_PIN_ICON_ID = "red-pin-icon-id";
    public static final String STAFF_SOURCE_ID = "staff-source-id";
    public static final String ICON_LAYER_ID2 = "icon-layer-id2";
    public static final String BLUE_PIN_ICON_ID = "blue-pin-icon-id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_staff_location);

        Bundle i = getIntent().getExtras();

        //pass to track staff location activity
        //testing the variables
        id = i.getInt(appointment.EXTRA_SERVICE_ID);
        destLatitude = i.getDouble(appointment.EXTRA_LATITUDE);
        destLongtitude = i.getDouble(appointment.EXTRA_LONGITUDE);

        latLng = new LatLng(destLatitude,destLongtitude);

        //Testing working normal or totally crashed
        //currently no crashed
        Toast.makeText(staffLocation.this, destLatitude + " + " + destLongtitude, Toast.LENGTH_SHORT).show();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        //end of testing here

        mapView = findViewById(R.id.staffMap);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

    }

    private void startTimer(Style style) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new Background(style).execute();
            }
        },0, 3000);
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
//                IconFactory iconFactory = IconFactory.getInstance(staffLocation.this);
//                Icon icon = iconFactory.fromResource(R.drawable.blue_marker);
//                destinationMarker = map.addMarker(new MarkerOptions()
//                        .icon(icon)
//                        .position(latLng)
//                        .setTitle("Your Destination"));
                initSource(style);
                initLayers(style);
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 4000);
                startTimer(style);
            }
        });
    }

    private void initSource(@NonNull Style loadedMapStyle) {

        Feature feature = Feature.fromGeometry(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()));
        GeoJsonSource geoJsonSource = new GeoJsonSource(DESTINATION_SOURCE_ID, feature);
        loadedMapStyle.addSource(geoJsonSource);

//        Add the LineLayer to the map. This layer will display the directions route.
    }

    /**
     * Add the route and marker icon layers to the map
     */
    private void initLayers(@NonNull Style loadedMapStyle) {

        loadedMapStyle.addImage(RED_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                getResources().getDrawable(R.drawable.red_marker1)));

        loadedMapStyle.addLayer(new SymbolLayer(ICON_LAYER_ID, DESTINATION_SOURCE_ID).withProperties(
                iconImage(RED_PIN_ICON_ID),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[] {0f, -9f})));
    }


    public class Background extends AsyncTask<Void, Void, LatLng> {
        private static final String LIBRARY = "com.mysql.jdbc.Driver";
        private Style style;

        private static final String USERNAME = "sql12372307";
        private static final String DB_NAME = "sql12372307";
        private static final String PASSWORD = "LYyljvuyn8";
        private static final String SERVER = "sql12.freemysqlhosting.net";

        private Connection conn;
        private PreparedStatement stmt;

        public Background(Style style) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            this.style = style;
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
            else {
//                IconFactory iconFactory = IconFactory.getInstance(ViewMap.this);
//                Icon icon = iconFactory.fromResource(R.drawable.red_marker);
//                MarkerOptions markerOptions = new MarkerOptions()
//                        .position(latLng1)
//                        .setTitle("Staff Current Location")
//                        .icon(icon);
//                staffLocationMarker = map.addMarker(markerOptions);

                GeoJsonSource geoJsonSource = style.getSourceAs(STAFF_SOURCE_ID);
                if (geoJsonSource == null) {
                    Feature feature = Feature.fromGeometry(Point.fromLngLat(latLng1.getLongitude(), latLng1.getLatitude()));
                    geoJsonSource = new GeoJsonSource(STAFF_SOURCE_ID, feature);
                    style.addSource(geoJsonSource);

                    style.addImage(BLUE_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                            getResources().getDrawable(R.drawable.blue_marker1)));

                    style.addLayer(new SymbolLayer(ICON_LAYER_ID2, STAFF_SOURCE_ID).withProperties(
                            iconImage(BLUE_PIN_ICON_ID),
                            iconIgnorePlacement(true),
                            iconAllowOverlap(true),
                            iconOffset(new Float[]{0f, -9f})));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .zoom(16)
                            .target(latLng1)
                            .build();
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 4000);
                }
                else {
                    Feature feature = Feature.fromGeometry(Point.fromLngLat(latLng1.getLongitude(), latLng1.getLatitude()));
                    geoJsonSource.setGeoJson(feature);
                }
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
            closeConn();
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
