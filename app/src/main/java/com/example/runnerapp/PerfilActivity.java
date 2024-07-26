package com.example.runnerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.runnerapp.Models.FirebaseOperations;
import com.example.runnerapp.Models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PerfilActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView countryTextView;
    private TextView distanceTextView;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        countryTextView = findViewById(R.id.countryTextView);
        distanceTextView = findViewById(R.id.distanceTextView);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        loadUserProfile();
    }

    private void loadUserProfile() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        nameTextView.setText(user.getFirstName() + " " + user.getLastName());
                        emailTextView.setText(user.getEmail());
                        countryTextView.setText(user.getCountry());
                        distanceTextView.setText("Km recorridos: " + user.getDistanceTraveled());

                        Glide.with(PerfilActivity.this)
                                .load(user.getProfileImageUrl())
                                .placeholder(R.drawable.ic_profile)
                                .into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

