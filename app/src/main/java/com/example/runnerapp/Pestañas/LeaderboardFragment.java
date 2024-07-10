package com.example.runnerapp.Pestañas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnerapp.Models.User;
import com.example.runnerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderboardFragment extends Fragment {

    private TextView leaderboardTitle;
    private RecyclerView leaderboardRecyclerView;
    private LeaderboardAdapter leaderboardAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private List<User> leaderboardUsers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        leaderboardTitle = view.findViewById(R.id.leaderboardTitle);
        leaderboardRecyclerView = view.findViewById(R.id.leaderboardRecyclerView);
        leaderboardUsers = new ArrayList<>();
        leaderboardAdapter = new LeaderboardAdapter(leaderboardUsers);

        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        leaderboardRecyclerView.setAdapter(leaderboardAdapter);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            usersRef = FirebaseDatabase.getInstance().getReference("users");

            // Load leaderboard users
            loadLeaderboardUsers();
        }

        return view;
    }

    private void loadLeaderboardUsers() {
        usersRef.orderByChild("country").equalTo(getUserCountry()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                leaderboardUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        leaderboardUsers.add(user);
                    }
                }

                // Sort leaderboardUsers based on some metric (e.g., distance traveled)
                Collections.sort(leaderboardUsers, new Comparator<User>() {
                    @Override
                    public int compare(User u1, User u2) {
                        // Here you can implement comparison logic based on distance or any other metric
                        // For example:
                        // return Double.compare(u2.getDistanceTraveled(), u1.getDistanceTraveled());
                        return 0; // Replace with actual comparison logic
                    }
                });

                leaderboardAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private String getUserCountry() {
        // Replace with actual logic to get the current user's country
        return "YourUserCountry"; // For testing purposes
    }
}
