package com.example.runnerapp.Tabs;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.runnerapp.R;
import com.example.runnerapp.Adapters.RaceTrackingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    private Button startRaceButton;
    private Button userWeightButton;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        startRaceButton = view.findViewById(R.id.startRaceButton);
        userWeightButton = view.findViewById(R.id.userWeightButton);
        TextView welcomeTextView = view.findViewById(R.id.welcomeTextView);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String welcomeText = "Hola, " + user.getDisplayName() + "!";
            welcomeTextView.setText(welcomeText);
        }

        startRaceButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                checkLocationEnabledAndStart();
            }
        });

        userWeightButton.setOnClickListener(v -> showWeightDialog());

        return view;
    }

    private void checkLocationEnabledAndStart() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled || !isNetworkEnabled) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Habilitar Ubicación")
                    .setMessage("La ubicación está desactivada. ¿Deseas activarla?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            startRaceTrackingActivity();
        }
    }

    private void startRaceTrackingActivity() {
        Intent intent = new Intent(getActivity(), RaceTrackingActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                checkLocationEnabledAndStart();
            } else {
                Toast.makeText(getActivity(), "Permisos de ubicación denegados", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showWeightDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double currentWeight = 0.0;
                    if (snapshot.exists() && snapshot.hasChild("weight")) {
                        currentWeight = snapshot.child("weight").getValue(Double.class);
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Actualizar Peso");

                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_weight, null);
                    EditText currentWeightEditText = dialogView.findViewById(R.id.currentWeightEditText);
                    EditText newWeightEditText = dialogView.findViewById(R.id.newWeightEditText);

                    currentWeightEditText.setText(String.valueOf(currentWeight));

                    builder.setView(dialogView)
                            .setPositiveButton("Actualizar", (dialog, which) -> {
                                String newWeightStr = newWeightEditText.getText().toString().trim();
                                if (!newWeightStr.isEmpty()) {
                                    double newWeight = Double.parseDouble(newWeightStr);
                                    userRef.child("weight").setValue(newWeight)
                                            .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Peso actualizado", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error al actualizar el peso", Toast.LENGTH_SHORT).show());
                                }
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                            .show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Error al obtener el peso actual", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}


