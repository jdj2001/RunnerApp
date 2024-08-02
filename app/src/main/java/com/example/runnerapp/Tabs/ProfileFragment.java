package com.example.runnerapp.Tabs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.runnerapp.Auth.LoginActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.runnerapp.Auth.EditProfileActivity;
import com.example.runnerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView nameTextView, countryTextView, distanceTextView;
    private ImageView profileImageView, countryFlagImageView;
    private Button editButton, logoutButton, viewFriendsButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImageView = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.nameTextView);
        countryTextView = view.findViewById(R.id.countryTextView);
        distanceTextView = view.findViewById(R.id.distanceTextView);
        countryFlagImageView = view.findViewById(R.id.countryFlagImageView);
        editButton = view.findViewById(R.id.editButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        viewFriendsButton = view.findViewById(R.id.viewFriendsButton);

        loadUserProfile();

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        viewFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendsListActivity.class);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String firstName = dataSnapshot.child("firstName").getValue(String.class);
                        String lastName = dataSnapshot.child("lastName").getValue(String.class);
                        String country = dataSnapshot.child("country").getValue(String.class);
                        String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                        Long distanceTraveled = dataSnapshot.child("distanceTraveled").getValue(Long.class);

                        String fullName = firstName + " " + lastName;
                        nameTextView.setText(fullName);

                        countryTextView.setText(country);
                        distanceTextView.setText(distanceTraveled != null ? distanceTraveled + " km" : "0 km");

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(ProfileFragment.this)
                                    .load(profileImageUrl)
                                    .apply(new RequestOptions().placeholder(R.drawable.ic_profile))
                                    .into(profileImageView);
                        } else {
                            profileImageView.setImageResource(R.drawable.ic_profile);
                        }

                        if (country != null) {
                            int flagResId = getFlagResourceId(country);
                            if (flagResId != 0) {
                                countryFlagImageView.setImageResource(flagResId);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private int getFlagResourceId(String countryName) {
        switch (countryName) {
            case "Honduras":
                return R.drawable.flag_honduras;
            case "Canada":
                return R.drawable.flag_canada;
            case "Costa Rica":
                return R.drawable.flag_costa_rica;
            case "Argentina":
                return R.drawable.flag_argentina;
            default:
                return 0;
        }
    }
}

