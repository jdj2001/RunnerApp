package com.example.runnerapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ProgressActivity extends AppCompatActivity {

    private TextView totalDistanceTextView;
    private TextView totalTimeTextView;
    private TextView averagePaceTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        totalDistanceTextView = findViewById(R.id.totalDistanceTextView);
        totalTimeTextView = findViewById(R.id.totalTimeTextView);
        averagePaceTextView = findViewById(R.id.averagePaceTextView);

        // Aquí puedes obtener los datos del usuario, como la distancia total, tiempo total y ritmo promedio,
        // desde Firebase o desde una base de datos local según tu implementación.

        // Ejemplo de actualización de TextViews (sustituye con tus datos reales)
        totalDistanceTextView.setText("5000 m");
        totalTimeTextView.setText("25 min");
        averagePaceTextView.setText("5 min/km");
    }
}
