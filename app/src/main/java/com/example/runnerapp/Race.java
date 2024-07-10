package com.example.runnerapp;

public class Race {
    private String raceId;
    private float distance;
    private long elapsedMillis;

    public Race(String raceId, float distance, long elapsedMillis) {
        this.raceId = raceId;
        this.distance = distance;
        this.elapsedMillis = elapsedMillis;
    }

    public String getRaceId() {
        return raceId;
    }

    public float getDistance() {
        return distance;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }
}
