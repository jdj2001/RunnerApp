package com.example.runnerapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
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
    private Button pauseButton;
    private Button resumeButton;
    private Chronometer chronometer;
    private long pauseOffset;
    private boolean running;

    private String userId; // Identificador del usuario actual

    private boolean raceStarted = false; // Variable para controlar si la carrera ha comenzado
    private boolean raceFinished = false; // Variable para controlar si la carrera ha finalizado

    private LocationCallback locationCallback;
    private Location lastLocation;
    private float totalDistance;

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
        pauseButton = findViewById(R.id.pauseButton);
        resumeButton = findViewById(R.id.resumeButton);
        chronometer = findViewById(R.id.chronometer);

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

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseChronometer();
            }
        });

        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resumeChronometer();
            }
        });

        // Inicializar botones y sus comportamientos
        startButton.setEnabled(false);
        endButton.setEnabled(false);
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(false);
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
        showStartRaceDialog(); // Mostrar diálogo para iniciar la carrera
    }

    // Método para mostrar el diálogo de inicio de carrera
    private void showStartRaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Iniciar Carrera");
        builder.setMessage("¿Deseas iniciar la carrera ahora?");
        builder.setPositiveButton("Iniciar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startRace(); // Método para iniciar la carrera
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
        if (startLatLng != null) {
            raceStarted = true; // Cambiar estado a carrera iniciada
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            running = true;
            startButton.setEnabled(false);
            endButton.setEnabled(true);
            pauseButton.setEnabled(true);
            resumeButton.setEnabled(false);

            // Solicitar actualizaciones de ubicación periódicas
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(3000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        if (lastLocation != null) {
                            totalDistance += lastLocation.distanceTo(location);
                        }
                        lastLocation = location;
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    // Método para pausar el cronómetro
    private void pauseChronometer() {
        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
            pauseButton.setEnabled(false);
            resumeButton.setEnabled(true);
        }
    }

    // Método para reanudar el cronómetro
    private void resumeChronometer() {
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            running = true;
            pauseButton.setEnabled(true);
            resumeButton.setEnabled(false);
        }
    }

    // Método para finalizar la carrera
    private void endRace() {
        if (raceStarted && !raceFinished) {
            raceFinished = true; // Cambiar estado a carrera finalizada
            chronometer.stop();
            long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
            fusedLocationClient.removeLocationUpdates(locationCallback);

            // Guardar datos de la carrera en Firebase
            saveRaceData(elapsedMillis, totalDistance);

            // Mostrar resumen de la carrera
            showRaceSummaryDialog(elapsedMillis, totalDistance);

            startButton.setEnabled(true);
            endButton.setEnabled(false);
            pauseButton.setEnabled(false);
            resumeButton.setEnabled(false);
        }
    }

    // Método para guardar datos de la carrera en Firebase
    private void saveRaceData(long elapsedMillis, float distance) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("races").child(userId);
        String raceId = databaseReference.push().getKey();
        Race race = new Race(raceId, distance, elapsedMillis);
        if (raceId != null) {
            databaseReference.child(raceId).setValue(race);
        }
    }

    // Método para mostrar resumen de la carrera
    private void showRaceSummaryDialog(long elapsedMillis, float distance) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Resumen de Carrera");
        builder.setMessage("Distancia: " + distance + " metros\nTiempo: " + (elapsedMillis / 1000) + " segundos");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // Regresar a la actividad principal
                startActivity(new Intent(RaceTrackingActivity.this, MainActivity.class));
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
