package com.example.runnerapp.Pestañas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.runnerapp.R;
import com.example.runnerapp.RaceTrackingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private Button startRaceButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        startRaceButton = view.findViewById(R.id.startRaceButton);
        TextView welcomeTextView = view.findViewById(R.id.welcomeTextView);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String welcomeText = "Welcome, " + user.getDisplayName() + "!";
            welcomeTextView.setText(welcomeText);
        }

        startRaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), RaceTrackingActivity.class));
            }
        });

        return view;
    }
}
