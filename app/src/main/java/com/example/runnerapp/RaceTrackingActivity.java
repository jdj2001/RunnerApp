package com.example.runnerapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RaceTrackingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker startMarker;
    private Marker endMarker;
    private LatLng startLatLng;
    private LatLng endLatLng;

    private Button startButton;
    private Button endButton;

    private String userId; // Identificador del usuario actual

    private boolean raceStarted = false; // Variable para controlar si la carrera ha comenzado
    private boolean raceFinished = false; // Variable para controlar si la carrera ha finalizado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_tracking);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtener el identificador del usuario actual
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Obtener el SupportMapFragment y ser notificado cuando el mapa esté listo para usarse
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            // Manejar caso nulo si es necesario
        }

        startButton = findViewById(R.id.startButton);
        endButton = findViewById(R.id.endButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener la posición actual del usuario
                if (ContextCompat.checkSelfPermission(RaceTrackingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                markStartLocation(currentLatLng);
                            } else {
                                Toast.makeText(RaceTrackingActivity.this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    ActivityCompat.requestPermissions(RaceTrackingActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        });

        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener la posición actual del usuario
                if (ContextCompat.checkSelfPermission(RaceTrackingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                markEndLocation(currentLatLng);
                            } else {
                                Toast.makeText(RaceTrackingActivity.this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    ActivityCompat.requestPermissions(RaceTrackingActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        });


        // Inicializar botones y sus comportamientos
        startButton.setEnabled(false);
        endButton.setEnabled(false);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Establecer listener para clicks en el mapa
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                showMarkerSelectionDialog(latLng);
            }
        });

        // Verificar permiso de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getLastKnownLocation();
        } else {
            // Solicitar permiso
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                }
            }
        });
    }

    // Método para mostrar el diálogo de selección de marcador (inicio o fin de carrera)
    private void showMarkerSelectionDialog(final LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Punto");
        builder.setMessage("¿Deseas establecer este punto como inicio o fin de la carrera?");
        builder.setPositiveButton("Inicio", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                markStartLocation(latLng); // Método para marcar el inicio de la carrera
            }
        });
        builder.setNegativeButton("Fin", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                markEndLocation(latLng); // Método para marcar el fin de la carrera
            }
        });
        builder.setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Método para marcar el inicio de la carrera
    private void markStartLocation(LatLng latLng) {
        if (startMarker != null) {
            startMarker.remove();
        }
        startMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Inicio de Carrera"));
        startLatLng = latLng;
        startButton.setEnabled(true);
        endButton.setEnabled(false);
    }

    // Método para marcar el fin de la carrera
    private void markEndLocation(LatLng latLng) {
        if (endMarker != null) {
            endMarker.remove();
        }
        endMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Fin de Carrera"));
        endLatLng = latLng;
        endButton.setEnabled(true);
        showStartRaceDialog();
    }

    // Método para mostrar el diálogo de inicio de carrera
    private void showStartRaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Iniciar Carrera");
        builder.setMessage("¿Estás seguro de que quieres iniciar la carrera?");
        builder.setPositiveButton("Iniciar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startRace(); // Método para iniciar la carrera
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Método para iniciar la carrera
    private void startRace() {
        // Implementar lógica para iniciar la carrera, por ejemplo:
        raceStarted = true; // Marcar la carrera como iniciada
        // Mostrar botones para finalizar carrera
        endButton.setVisibility(View.VISIBLE);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishRace(); // Método para finalizar la carrera
            }
        });
    }

    // Método para finalizar la carrera y guardarla
    private void finishRace() {
        // Implementar lógica para finalizar y guardar la carrera
        raceStarted = false;
        raceFinished = true;

        // Guardar la actividad de la carrera
        saveRaceActivity();

        // Navegar a la pantalla de progreso (Actividades del Día)
        Intent intent = new Intent(this, ProgressActivity.class);
        startActivity(intent);
        finish(); // Finalizar la actividad actual si es necesario
    }

    // Método para guardar la actividad de carrera en Firebase
    private void saveRaceActivity() {
        if (startLatLng != null && endLatLng != null) {
            RaceActivity raceActivity = new RaceActivity(startLatLng, endLatLng);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child("race_activities").child(userId);
            ref.push().setValue(raceActivity)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(RaceTrackingActivity.this, "Carrera guardada exitosamente", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RaceTrackingActivity.this, "Error al guardar la carrera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Error: Debes seleccionar el inicio y el fin de la carrera", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    getLastKnownLocation();
                }
            }
        }
    }
}
