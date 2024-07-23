package com.example.runnerapp;

public class Race {
    private String raceId;
    private float distance;
    private String elapsedMillis; // Sigue como String
    private double caloriesBurned;

    public Race(String raceId, float distance, String elapsedMillis, double caloriesBurned) {
        this.raceId = raceId;
        this.distance = distance;
        this.elapsedMillis = elapsedMillis;
        this.caloriesBurned = caloriesBurned;
    }

    // Métodos getters y setters
    public String getRaceId() {
        return raceId;
    }

    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getElapsedMillis() {
        return elapsedMillis;
    }

    public void setElapsedMillis(String elapsedMillis) {
        this.elapsedMillis = elapsedMillis;
    }

    public double getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }
}

