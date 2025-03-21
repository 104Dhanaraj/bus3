package com.example.bus.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.example.bus.R;
import com.example.bus.model.Bus;
import com.example.bus.model.Route;
import com.example.bus.model.Stop;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.Polygon;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BusStopsActivity extends AppCompatActivity {
    private static final String TAG = "BusStopsActivity";
    private TextView txtSelectedBus, txtTotalTime;
    private ListView stopListView;
    private List<Stop> stopList;
    private String selectedSource, selectedDestination;
    private Bus selectedBus;
    private StopListAdapter stopListAdapter;
    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;
    private TextToSpeech textToSpeech;
    private MapView mapView;
    private Polyline routeLine;

    private static final float STOP_RADIUS = 150.0f;
    private Set<String> announcedStops = new HashSet<>();
    private Button startButton;
    private int totalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stops);

        // Initialize OSMDroid configuration
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        txtSelectedBus = findViewById(R.id.txt_selected_bus);
        txtTotalTime = findViewById(R.id.txt_total_time);
        stopListView = findViewById(R.id.list_stops);
        startButton = findViewById(R.id.btn_start);
        mapView = findViewById(R.id.map_view);

        // Configure OSMDroid map
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        Intent intent = getIntent();
        selectedSource = intent.getStringExtra("source");
        selectedDestination = intent.getStringExtra("destination");
        String busJson = intent.getStringExtra("selectedBus");
        totalTime = intent.getIntExtra("totalTime", -1);

        selectedBus = new Gson().fromJson(busJson, Bus.class);
        if (selectedBus == null) {
            Toast.makeText(this, "Error: No bus data received!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtSelectedBus.setText("Bus: " + selectedBus.getBusName());

        if (totalTime == -1) {
            totalTime = selectedBus.getTotalTime();
        }
        txtTotalTime.setText("Time: " + totalTime + " min");

        loadStopsForSelectedRoute();
        updateStopListView();
        drawRouteOnMap(); // Draw route and stops

        locationClient = LocationServices.getFusedLocationProviderClient(this);
        startButton.setOnClickListener(v -> startLocationUpdates());
    }

    private void updateStopListView() {
        if (stopListAdapter == null) {
            stopListAdapter = new StopListAdapter(this, stopList);
            stopListView.setAdapter(stopListAdapter);
        } else {
            stopListAdapter.notifyDataSetChanged();
        }
    }

    private List<Route> loadRoutesFromStorage() {
        Gson gson = new Gson();
        String json = getSharedPreferences("BusStops", MODE_PRIVATE).getString("routes", "[]");
        Type routeListType = new TypeToken<ArrayList<Route>>() {}.getType();
        return gson.fromJson(json, routeListType);
    }

    private void loadStopsForSelectedRoute() {
        List<Route> routes = loadRoutesFromStorage();
        stopList = new ArrayList<>();

        for (Route route : routes) {
            if (route.getRouteName().equals(selectedBus.getAssignedRoute())) {
                List<Stop> stops = route.getStops();
                boolean startAdding = false;

                for (Stop stop : stops) {
                    if (stop.getName().equals(selectedSource)) {
                        startAdding = true;
                    }
                    if (startAdding) {
                        stopList.add(stop);
                    }
                    if (stop.getName().equals(selectedDestination)) {
                        break;
                    }
                }
            }
        }
    }

    private void drawRouteOnMap() {
        mapView.getOverlays().clear(); // Clear previous overlays

        routeLine = new Polyline();
        routeLine.setColor(Color.BLUE);
        routeLine.setWidth(8.0f);

        for (Stop stop : stopList) {
            GeoPoint point = new GeoPoint(stop.getLatitude(), stop.getLongitude());
            routeLine.addPoint(point);

            // Draw stop as a red circle (default)
            Polygon stopCircle = new Polygon();
            stopCircle.setPoints(Polygon.pointsAsCircle(point, 100.0));
            stopCircle.setFillColor(Color.RED);
            stopCircle.setStrokeColor(Color.BLACK);
            stopCircle.setStrokeWidth(2.0f);

            mapView.getOverlays().add(stopCircle);
        }

        mapView.getOverlays().add(routeLine);

        if (!stopList.isEmpty()) {
            mapView.getController().setZoom(15.0);
            mapView.getController().animateTo(new GeoPoint(stopList.get(0).getLatitude(), stopList.get(0).getLongitude()));
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    checkNearbyStop(location);
                }
            }
        };

        locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void checkNearbyStop(Location userLocation) {
        for (Stop stop : stopList) {
            float[] distance = new float[1];
            Location.distanceBetween(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    stop.getLatitude(), stop.getLongitude(),
                    distance
            );

            if (distance[0] < STOP_RADIUS && !announcedStops.contains(stop.getName())) {
                announceStop(stop.getName());
                announcedStops.add(stop.getName());
            }
        }
    }

    private void announceStop(String stopName) {
        String announcement = "Approaching " + stopName;
        textToSpeech.speak(announcement, TextToSpeech.QUEUE_FLUSH, null, null);

        runOnUiThread(() -> {
            stopListAdapter.updateSelectedStop(stopName);
            highlightCurrentStopOnMap(stopName);
        });
    }

    private void highlightCurrentStopOnMap(String currentStopName) {
        mapView.getOverlays().clear();

        routeLine = new Polyline();
        routeLine.setColor(Color.BLUE);
        routeLine.setWidth(8.0f);

        for (Stop stop : stopList) {
            GeoPoint point = new GeoPoint(stop.getLatitude(), stop.getLongitude());
            routeLine.addPoint(point);

            int fillColor = stop.getName().equals(currentStopName) ? Color.BLUE : Color.RED;

            Polygon stopCircle = new Polygon();
            stopCircle.setPoints(Polygon.pointsAsCircle(point, 100.0));
            stopCircle.setFillColor(fillColor);
            stopCircle.setStrokeColor(Color.BLACK);
            stopCircle.setStrokeWidth(2.0f);

            mapView.getOverlays().add(stopCircle);
        }

        mapView.getOverlays().add(routeLine);
        mapView.invalidate();
    }
}
