package com.example.runnerapp.Tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.runnerapp.Models.Activity;
import com.example.runnerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

    private TextView totalKmsTextView;
    private TextView totalCaloriesTextView;
    private TextView kmsMonthTextView;
    private TextView caloriesMonthTextView;
    private TextView weekComparisonTextView;
    private TextView monthComparisonTextView;

    private FirebaseAuth mAuth;
    private DatabaseReference userActivitiesRef;

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        totalKmsTextView = view.findViewById(R.id.totalKmsTextView);
        totalCaloriesTextView = view.findViewById(R.id.totalCaloriesTextView);
        kmsMonthTextView = view.findViewById(R.id.kmsMonthTextView);
        caloriesMonthTextView = view.findViewById(R.id.caloriesMonthTextView);
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
                double currentWeekKms = 0;
                double previousWeekKms = 0;
                double currentMonthKms = 0;
                double previousMonthKms = 0;

                int currentWeekCalories = 0;
                int previousWeekCalories = 0;
                int currentMonthCalories = 0;
                int previousMonthCalories = 0;

                Calendar calendar = Calendar.getInstance();
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentYear = calendar.get(Calendar.YEAR);
                int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);

                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Activity activity = snapshot.getValue(Activity.class);
                    if (activity != null) {
                        double distance = activity.getDistance();
                        double calories = activity.getCaloriesBurned();
                        totalKms += distance;
                        totalCalories += calories;

                        try {
                            Calendar activityCalendar = Calendar.getInstance();
                            activityCalendar.setTime(sdf.parse(activity.getDate()));
                            int activityMonth = activityCalendar.get(Calendar.MONTH);
                            int activityYear = activityCalendar.get(Calendar.YEAR);
                            int activityWeek = activityCalendar.get(Calendar.WEEK_OF_YEAR);

                            if (activityYear == currentYear && activityMonth == currentMonth) {
                                currentMonthKms += distance;
                                currentMonthCalories += calories;
                                if (activityWeek == currentWeek) {
                                    currentWeekKms += distance;
                                    currentWeekCalories += calories;
                                } else if (activityWeek == currentWeek - 1) {
                                    previousWeekKms += distance;
                                    previousWeekCalories += calories;
                                }
                            } else if (activityYear == currentYear && activityMonth == currentMonth - 1) {
                                previousMonthKms += distance;
                                previousMonthCalories += calories;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                totalKmsTextView.setText(String.format("Km recorridos hasta el momento: %.2f", totalKms));
                totalCaloriesTextView.setText(String.format("Calorías quemadas hasta el momento: %d", totalCalories));
                kmsMonthTextView.setText(String.format("Km recorridos este mes: %.2f", currentMonthKms));
                caloriesMonthTextView.setText(String.format("Calorías quemadas este mes: %d", currentMonthCalories));
                weekComparisonTextView.setText(String.format("Comparativo de km (Semana): %.2f vs %.2f", currentWeekKms, previousWeekKms));
                monthComparisonTextView.setText(String.format("Comparativo de km (Mes): %.2f vs %.2f", currentMonthKms, previousMonthKms));

                String weekCaloriesComparison = String.format("Comparativo de calorías (Semana): %d vs %d", currentWeekCalories, previousWeekCalories);
                String monthCaloriesComparison = String.format("Comparativo de calorías (Mes): %d vs %d", currentMonthCalories, previousMonthCalories);

                weekComparisonTextView.append("\n" + weekCaloriesComparison);
                monthComparisonTextView.append("\n" + monthCaloriesComparison);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}


