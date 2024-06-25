package com.example.runnerapp;

import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RunActivity extends AppCompatActivity {
    private GPSTracker gpsTracker;
    private TextView distanceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        distanceTextView = findViewById(R.id.distanceTextView);
        gpsTracker = new GPSTracker(this);

        // Start tracking location
        trackDistance();
    }

    private void trackDistance() {
        Location startLocation = gpsTracker.getLocation();
        if (startLocation != null) {
            // Simulación de movimiento y cálculo de distancia
            Location endLocation = new Location(startLocation);
            endLocation.setLatitude(startLocation.getLatitude() + 0.01);
            endLocation.setLongitude(startLocation.getLongitude() + 0.01);

            float distance = startLocation.distanceTo(endLocation);
            distanceTextView.setText("Distancia: " + distance + " metros");
        }
    }
}

