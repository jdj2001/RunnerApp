package com.example.runnerapp.Models;

import androidx.annotation.NonNull;

import com.example.runnerapp.Activity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseOperations {

    // Guardar datos de usuario al registrarse
    public static void saveUserData(String userId, String email, String firstName, String lastName, String country, String profileImageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        User user = new User(userId, email, firstName, lastName, country, profileImageUrl);
        databaseReference.setValue(user);
    }


    // Guardar una nueva actividad
    public static void saveUserActivity(String userId, String date, double distance, int time, String route) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("activities").child(userId).push();
        Activity activity = new Activity(date, distance, time, route);
        databaseReference.setValue(activity);
    }

    // Añadir un nuevo amigo
    public static void addFriend(String userId, String friendId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("friends").child(friendId);
        databaseReference.setValue(true);
    }

    // Actualizar el leaderboard
    public static void updateLeaderboard(String country, String userId, double distance) {
        DatabaseReference leaderboardRef = FirebaseDatabase.getInstance().getReference("leaderboard").child(country).child(userId);
        leaderboardRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double currentDistance = 0;
                if (dataSnapshot.exists()) {
                    currentDistance = dataSnapshot.getValue(Double.class);
                }
                leaderboardRef.setValue(currentDistance + distance);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Maneja los posibles errores aquí
            }
        });
    }
}
