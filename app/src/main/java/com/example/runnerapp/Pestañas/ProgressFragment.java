package com.example.runnerapp.Pestañas;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnerapp.R;
import com.example.runnerapp.Models.Activity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressFragment extends Fragment {

    private RecyclerView progressRecyclerView;
    private ProgressAdapter progressAdapter;
    private List<Activity> activities;
    private DatabaseReference activitiesRef;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        progressRecyclerView = view.findViewById(R.id.progressRecyclerView);
        progressRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        activities = new ArrayList<>();
        progressAdapter = new ProgressAdapter(activities);
        progressRecyclerView.setAdapter(progressAdapter);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            activitiesRef = FirebaseDatabase.getInstance().getReference("activities").child(currentUser.getUid());
            loadActivities();
        } else {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadActivities() {
        activitiesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                activities.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Activity activity = snapshot.getValue(Activity.class);
                    if (activity != null) {
                        activities.add(activity);
                    }
                }
                // Notificar al adaptador sobre los datos actualizados
                progressAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error al cargar actividades", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


