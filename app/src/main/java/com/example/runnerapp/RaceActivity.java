package com.example.runnerapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.location.Location;

public class RaceActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Chronometer chronometer;
    private TextView distanceTextView;
    private long pauseOffset;
    private boolean running;
    private float totalDistance;
    private long elapsedMillis;
    private String userId;
    private Location lastLocation;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race);

        chronometer = findViewById(R.id.chronometer);
        distanceTextView = findViewById(R.id.distanceTextView);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        startChronometer();
        startLocationUpdates();
    }

    private void startChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        running = true;
    }

    private void pauseChronometer() {
        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
        }
    }

    private void resumeChronometer() {
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            running = true;
        }
    }

    private void stopChronometer() {
        if (running) {
            chronometer.stop();
            elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (lastLocation != null) {
                        totalDistance += lastLocation.distanceTo(location);
                    }
                    lastLocation = location;
                    updateUI();
                }
            }
        };

        // Verifica los permisos antes de solicitar actualizaciones de ubicación.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si no tienes los permisos, solicítalos al usuario.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Si tienes permisos, solicita actualizaciones de ubicación.
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, procede con la solicitud de ubicaciones.
                startLocationUpdates();
            } else {
                // Permiso denegado, maneja la situación (puedes mostrar un mensaje o deshabilitar funcionalidades dependientes de la ubicación).
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void updateUI() {
        distanceTextView.setText(getString(R.string.distance_text, totalDistance));
    }

    private void saveRaceData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("races").child(userId);
        String raceId = databaseReference.push().getKey();
        Race race = new Race(raceId, totalDistance, elapsedMillis);
        if (raceId != null) {
            databaseReference.child(raceId).setValue(race);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopChronometer();
        stopLocationUpdates();
        saveRaceData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeChronometer();
        startLocationUpdates();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopChronometer();
        stopLocationUpdates();
        saveRaceData();
        startActivity(new Intent(RaceActivity.this, MainActivity.class));
        finish();
    }
}
