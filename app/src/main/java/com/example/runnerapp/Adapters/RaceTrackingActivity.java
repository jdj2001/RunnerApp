package com.example.runnerapp.Adapters;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.runnerapp.Models.Activity;
import com.example.runnerapp.Models.Race;
import com.example.runnerapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RaceTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "RaceTrackingActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Chronometer chronometer;
    private TextView distanceTextView;

    private List<LatLng> routePoints = new ArrayList<>();

    private long pauseOffset;
    private boolean running;
    private float totalDistance;
    private long elapsedMillis;
    private String userId;

    private Location lastLocation;
    private GoogleMap mMap;
    private Marker destinationMarker;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_tracking);

        chronometer = findViewById(R.id.chronometer);
        distanceTextView = findViewById(R.id.distanceTextView);
        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        startButton.setOnClickListener(v -> {
            totalDistance = 0;
            startChronometer();
            startLocationUpdates();
        });

        stopButton.setOnClickListener(v -> {
            stopChronometer();
            stopLocationUpdates();
            saveRaceData();
            Toast.makeText(RaceTrackingActivity.this, "Carrera detenida", Toast.LENGTH_SHORT).show();
            finish();
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            initializeMap();
        }
    }

    private void initializeMap() {
        if (mMap == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.mapContainer);
            if (mapFragment == null) {
                mapFragment = new SupportMapFragment();
                fragmentManager.beginTransaction().replace(R.id.mapContainer, mapFragment).commit();
            }
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getLastLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        mMap.setOnMapLongClickListener(latLng -> {
            if (destinationMarker != null) {
                destinationMarker.remove();
            }
            destinationMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Destino"));
            drawRouteToDestination(latLng);
        });
    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                lastLocation = location;
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            } else {
                startLocationUpdates();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error al obtener la última ubicación", e);
        });
    }

    private void drawRouteToDestination(LatLng destination) {
        if (lastLocation != null) {
            LatLng startLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            mMap.addPolyline(new PolylineOptions().add(startLatLng, destination));
        }
    }

    private void startChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();
        running = true;
    }

    private void stopChronometer() {
        chronometer.stop();
        elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
        running = false;
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    if (lastLocation != null) {
                        float distance = lastLocation.distanceTo(location);
                        if (distance > 0.5) {
                            totalDistance += distance;
                        }
                    }
                    lastLocation = location;
                    routePoints.add(new LatLng(location.getLatitude(), location.getLongitude()));

                    updateUI();
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }


    private void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void updateUI() {
        distanceTextView.setText(String.format(Locale.getDefault(), "%.2f metros", totalDistance));
    }

    private double calculateCaloriesBurned(float distanceInKm) {
        double MET = 8;
        double weight = 70;
        return MET * weight * distanceInKm;
    }

    private void saveRaceData() {
        DatabaseReference racesRef = FirebaseDatabase.getInstance().getReference().child("races").child(userId);
        String raceId = racesRef.push().getKey();

        if (raceId != null) {
            double distanceInKm = totalDistance / 1000.0; // Convertir a kilómetros
            double caloriesBurned = calculateCaloriesBurned((float) distanceInKm);
            String formattedTime = formatElapsedTime(elapsedMillis);

            Gson gson = new Gson();
            String routeJson = gson.toJson(routePoints);

            Race race = new Race(raceId, (float) distanceInKm, formattedTime, caloriesBurned);
            racesRef.child(raceId).setValue(race)
                    .addOnSuccessListener(aVoid -> Toast.makeText(RaceTrackingActivity.this, "Datos de carrera guardados", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(RaceTrackingActivity.this, "Error al guardar datos de carrera", Toast.LENGTH_SHORT).show());

            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            Activity activity = new Activity(date, distanceInKm, formattedTime, routeJson, caloriesBurned, raceId, formattedTime);
            DatabaseReference activitiesRef = FirebaseDatabase.getInstance().getReference().child("activities").child(userId);
            activitiesRef.child(raceId).setValue(activity);
        }
    }


    private String formatElapsedTime(long elapsedMillis) {
        long seconds = (elapsedMillis / 1000) % 60;
        long minutes = (elapsedMillis / (1000 * 60)) % 60;
        long hours = (elapsedMillis / (1000 * 60 * 60)) % 24;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}



