package com.example.runnerapp.Models;

public class Activity {
    private String date;
    private double distance;
    private String time;
    private String route;
    private double caloriesBurned;
    private String raceId;
    private String elapsedMillis;

    public Activity() {}

    public Activity(String date, double distance, String time, String route, double caloriesBurned, String raceId, String elapsedMillis) {
        this.date = date;
        this.distance = distance;
        this.time = time;
        this.route = route;
        this.caloriesBurned = caloriesBurned;
        this.raceId = raceId;
        this.elapsedMillis = elapsedMillis;
    }

    // Getters y setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public double getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public String getRaceId() {
        return raceId;
    }

    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }

    public String getElapsedMillis() {
        return elapsedMillis;
    }

    public void setElapsedMillis(String elapsedMillis) {
        this.elapsedMillis = elapsedMillis;
    }
}
