package com.example.trackerhealth;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PhysicalActivityTracker extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private Spinner activityTypeSpinner;
    private CheckBox useGpsCheckbox;
    private LinearLayout gpsContainer;
    private Button startTrackingButton;
    private Button saveActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_tracker);

        // Inicializar componentes
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        activityTypeSpinner = findViewById(R.id.activity_type_spinner);
        useGpsCheckbox = findViewById(R.id.use_gps_checkbox);
        gpsContainer = findViewById(R.id.gps_container);
        startTrackingButton = findViewById(R.id.start_tracking_button);
        saveActivityButton = findViewById(R.id.save_activity_button);

        // Configurar bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_activity);

        // Configurar spinner de actividades
        String[] activityTypes = {"Running", "Walking", "Cycling", "Swimming", "Gym workout", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, activityTypes);
        activityTypeSpinner.setAdapter(adapter);

        // Configurar checkbox de GPS
        useGpsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            gpsContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Configurar botón de tracking
        startTrackingButton.setOnClickListener(v -> {
            if (startTrackingButton.getText().equals(getString(R.string.start_tracking))) {
                startTrackingButton.setText(R.string.stop_tracking);
                Toast.makeText(this, "GPS tracking started", Toast.LENGTH_SHORT).show();
            } else {
                startTrackingButton.setText(R.string.start_tracking);
                Toast.makeText(this, "GPS tracking stopped", Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar botón de guardar
        saveActivityButton.setOnClickListener(v -> {
            Toast.makeText(this, "Activity saved successfully", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        
        int itemId = item.getItemId();
        
        if (itemId == R.id.navigation_dashboard) {
            intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.navigation_activity) {
            return true;
        } else if (itemId == R.id.navigation_food) {
            intent = new Intent(this, FoodTrackerActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.navigation_reports) {
            intent = new Intent(this, ReportsActivity.class);
            startActivity(intent);
            return true;
        }
        
        return false;
    }
}