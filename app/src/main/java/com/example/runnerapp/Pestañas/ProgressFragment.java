package com.example.runnerapp.Pestañas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnerapp.Activity;
import com.example.runnerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProgressFragment extends Fragment {

    private RecyclerView progressRecyclerView;
    private ProgressAdapter progressAdapter;
    private List<Activity> activityList;
    private DatabaseReference activitiesRef;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        progressRecyclerView = view.findViewById(R.id.progressRecyclerView);
        progressRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar la lista de actividades
        activityList = new ArrayList<>();

        // Inicializar Firebase Auth y referencia a Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            activitiesRef = FirebaseDatabase.getInstance().getReference("activities").child(currentUser.getUid());
            loadActivities();
        }

        progressAdapter = new ProgressAdapter(activityList);
        progressRecyclerView.setAdapter(progressAdapter);

        return view;
    }

    private void loadActivities() {
        // Consultar actividades de la base de datos
        activitiesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                activityList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Activity activity = snapshot.getValue(Activity.class);
                    if (activity != null) {
                        activityList.add(activity);
                    }
                }
                progressAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar error de consulta
            }
        });
    }
}
