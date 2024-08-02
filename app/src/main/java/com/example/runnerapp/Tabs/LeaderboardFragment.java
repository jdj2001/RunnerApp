package com.example.runnerapp.Tabs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnerapp.Adapters.LeaderboardAdapter;
import com.example.runnerapp.R;
import com.example.runnerapp.Models.User;
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
    private DatabaseReference friendsRef;

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
            friendsRef = FirebaseDatabase.getInstance().getReference("friends").child(currentUser.getUid());

            loadLeaderboardUsers();
        }

        return view;
    }

    private void loadLeaderboardUsers() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            DatabaseReference userRef = usersRef.child(currentUserId).child("friends");
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Obtener la lista de IDs de amigos
                    List<String> friendIds = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        friendIds.add(snapshot.getKey());
                    }

                    // Agregar el ID del usuario logueado a la lista
                    friendIds.add(currentUserId);

                    // Cargar los datos de los usuarios (incluidos los amigos)
                    usersRef.orderByChild("userId").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            leaderboardUsers.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);
                                if (user != null && friendIds.contains(user.getUserId())) {
                                    leaderboardUsers.add(user);
                                }
                            }

                            // Ordenar la lista por distancia recorrida
                            Collections.sort(leaderboardUsers, new Comparator<User>() {
                                @Override
                                public int compare(User u1, User u2) {
                                    return Double.compare(u2.getDistanceTraveled(), u1.getDistanceTraveled());
                                }
                            });

                            leaderboardAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Manejar el error
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Manejar el error
                }
            });
        }
    }


    public interface UserCountryCallback {
        void onCallback(String country);
    }


    private void getUserCountry(final UserCountryCallback callback) {
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


}

