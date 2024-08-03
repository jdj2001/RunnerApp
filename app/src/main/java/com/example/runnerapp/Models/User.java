package com.example.runnerapp.Models;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String country;
    private double weight;
    private String profileImageUrl;
    private double distanceTraveled;
    private double caloriesBurned;
    private Map<String, Boolean> friends;

    public User() {
        this.friends = new HashMap<>();
        this.caloriesBurned = 0.0;
    }

    public User(String userId, String email, String firstName, String lastName, String country, double weight, String profileImageUrl, double distanceTraveled, double caloriesBurned, Map<String, Boolean> friends) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.country = country;
        this.weight = weight;
        this.profileImageUrl = profileImageUrl;
        this.distanceTraveled = distanceTraveled;
        this.caloriesBurned = caloriesBurned;
        this.friends = friends != null ? friends : new HashMap<>();
    }

    // Getters y setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    public void setDistanceTraveled(double distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
    }

    public double getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public Map<String, Boolean> getFriends() {
        return friends;
    }

    public void setFriends(Map<String, Boolean> friends) {
        this.friends = friends;
    }
}
