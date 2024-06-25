package com.example.runnerapp;

import com.google.android.gms.maps.model.LatLng;

public class RaceActivity {
    private LatLng startLocation;
    private LatLng endLocation;
    private double distance;
    private long startTime;
    private long endTime;

    // Constructor
    public RaceActivity(LatLng startLocation, LatLng endLocation) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        // Inicializar otros campos según necesites
    }

    // Getters y setters (si es necesario)
    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
    }

    // Otros métodos y campos según necesites
}

