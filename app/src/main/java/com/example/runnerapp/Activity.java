package com.example.runnerapp;


public class Activity {
    private String date;
    private double distance;
    private int time;
    private String route;

    public Activity() {
        // Default constructor required for calls to DataSnapshot.getValue(Activity.class)
    }

    public Activity(String date, double distance, int time, String route) {
        this.date = date;
        this.distance = distance;
        this.time = time;
        this.route = route;
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

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }
}
