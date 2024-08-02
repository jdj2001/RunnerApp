package com.example.runnerapp.Models;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.runnerapp.Models.User;

import java.util.HashMap;

public class FirebaseOperations {

    public static void saveUserData(String userId, String email, String firstName, String lastName, String country, double weight, String profileImageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        User user = new User(userId, email, firstName, lastName, country, weight, profileImageUrl, 0.0, 0.0, new HashMap<>());
        databaseReference.setValue(user);
    }

    /*public static void saveUserData(String userId, String email, String firstName, String lastName, String country, double weight, String profileImageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        User user = new User(userId, email, firstName, lastName, country, weight, profileImageUrl, 0.0, new HashMap<>());
        databaseReference.setValue(user);
    }*/

    public static void saveUserActivity(String userId, String date, double distance, int timeInMilliseconds,
                                        String route, double caloriesBurned, String raceId, String elapsedMillis) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("activities").child(userId).push();
        String formattedTime = formatTime(timeInMilliseconds);
        Activity activity = new Activity(date, distance, formattedTime, route, caloriesBurned, raceId, elapsedMillis);
        databaseReference.setValue(activity);
    }

    private static String formatTime(int timeInMilliseconds) {
        int hours = (timeInMilliseconds / 3600000);
        int minutes = (timeInMilliseconds % 3600000) / 60000;
        int seconds = (timeInMilliseconds % 60000) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /*public static void addFriend(String currentUserId, String friendId, OnFriendAddedListener listener) {
        DatabaseReference currentUserFriendsRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("friends").child(friendId);
        DatabaseReference friendUserFriendsRef = FirebaseDatabase.getInstance().getReference("users").child(friendId).child("friends").child(currentUserId);

        currentUserFriendsRef.setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                friendUserFriendsRef.setValue(true).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        listener.onFriendAdded(true, "Amigo agregado exitosamente");
                    } else {
                        listener.onFriendAdded(false, "Error al agregar amigo");
                    }
                });
            } else {
                listener.onFriendAdded(false, "Error al agregar amigo");
            }
        });
    }

    public interface OnFriendAddedListener {
        void onFriendAdded(boolean success, String message);
    }*/

    public static void addFriend(String currentUserId, String friendId, OnFriendAddedListener listener) {
        DatabaseReference currentUserFriendsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUserId).child("friends").child(friendId);
        DatabaseReference friendUserFriendsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(friendId).child("friends").child(currentUserId);

        currentUserFriendsRef.setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                friendUserFriendsRef.setValue(true).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        removeFriendRequest(currentUserId, friendId);
                        listener.onFriendAdded(true, "Amigo agregado exitosamente");
                    } else {
                        listener.onFriendAdded(false, "Error al agregar amigo");
                    }
                });
            } else {
                listener.onFriendAdded(false, "Error al agregar amigo");
            }
        });
    }

    private static void removeFriendRequest(String currentUserId, String friendId) {
        DatabaseReference currentUserRequestRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUserId).child("friendRequests").child(friendId);
        currentUserRequestRef.removeValue();

        DatabaseReference friendRequestRef = FirebaseDatabase.getInstance().getReference("users")
                .child(friendId).child("friendRequests").child(currentUserId);
        friendRequestRef.removeValue();
    }

    public static void sendFriendRequest(String currentUserId, String friendId, OnFriendRequestSentListener listener) {
        DatabaseReference friendRequestRef = FirebaseDatabase.getInstance().getReference("users")
                .child(friendId).child("friendRequests").child(currentUserId);
        friendRequestRef.setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onFriendRequestSent(true, "Solicitud de amistad enviada");
            } else {
                listener.onFriendRequestSent(false, "Error al enviar solicitud de amistad");
            }
        });
    }

    public interface OnFriendAddedListener {
        void onFriendAdded(boolean success, String message);
    }

    public interface OnFriendRequestSentListener {
        void onFriendRequestSent(boolean success, String message);
    }

    public interface UserCountryCallback {
        void onCallback(String country);
    }

    public static void getUserCountry(final UserCountryCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null && user.getCountry() != null) {
                            callback.onCallback(user.getCountry());
                        } else {
                            callback.onCallback("Unknown");
                        }
                    } else {
                        callback.onCallback("Unknown");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    callback.onCallback("Unknown");
                }
            });
        } else {
            callback.onCallback("Unknown");
        }
    }

    public static void updateLeaderboard(final String userId, final double distance) {
        getUserCountry(new UserCountryCallback() {
            @Override
            public void onCallback(String country) {
                if (!"Unknown".equals(country)) {
                    DatabaseReference leaderboardRef = FirebaseDatabase.getInstance().getReference("leaderboard").child(country).child(userId);
                    leaderboardRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            double currentDistance = 0;
                            if (dataSnapshot.exists()) {
                                Double distanceValue = dataSnapshot.getValue(Double.class);
                                if (distanceValue != null) {
                                    currentDistance = distanceValue;
                                }
                            }
                            leaderboardRef.setValue(currentDistance + distance);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {

                }
            }
        });
    }

}