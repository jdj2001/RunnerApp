package com.example.runnerapp.Pestañas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.runnerapp.Activity;
import com.example.runnerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StatisticsFragment extends Fragment {

    private TextView kmsMonthTextView;
    private TextView caloriesBurnedTextView;
    private TextView weekComparisonTextView;
    private TextView monthComparisonTextView;

    private FirebaseAuth mAuth;
    private DatabaseReference userActivitiesRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        kmsMonthTextView = view.findViewById(R.id.kmsMonthTextView);
        caloriesBurnedTextView = view.findViewById(R.id.caloriesBurnedTextView);
        weekComparisonTextView = view.findViewById(R.id.weekComparisonTextView);
        monthComparisonTextView = view.findViewById(R.id.monthComparisonTextView);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            userActivitiesRef = FirebaseDatabase.getInstance().getReference("activities").child(userId);
            loadStatistics();
        }

        return view;
    }

    private void loadStatistics() {
        userActivitiesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalKms = 0;
                int totalCalories = 0;
                double week1Kms = 0;
                double week2Kms = 0;
                double month1Kms = 0;
                double month2Kms = 0;

                // Logic to calculate totalKms, totalCalories, week1Kms, week2Kms, month1Kms, month2Kms

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Activity activity = snapshot.getValue(Activity.class);
                    if (activity != null) {
                        totalKms += activity.getDistance();
                        totalCalories += calculateCalories(activity.getDistance());
                        // Add logic to accumulate kms for week1Kms, week2Kms, month1Kms, month2Kms
                    }
                }

                kmsMonthTextView.setText("Km recorridos este mes: " + totalKms);
                caloriesBurnedTextView.setText("Calorías quemadas: " + totalCalories);
                weekComparisonTextView.setText("Comparativo de km (Semana): " + week1Kms + " vs " + week2Kms);
                monthComparisonTextView.setText("Comparativo de km (Mes): " + month1Kms + " vs " + month2Kms);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private int calculateCalories(double distance) {
        // A simple method to calculate calories burned based on distance
        return (int) (distance * 70); // Assume 70 calories burned per km
    }
}
