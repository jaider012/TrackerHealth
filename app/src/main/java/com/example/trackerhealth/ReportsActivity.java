package com.example.trackerhealth;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ReportsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private Spinner timePeriodSpinner;
    private Button exportDataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        // Inicializar componentes
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        timePeriodSpinner = findViewById(R.id.time_period_spinner);
        exportDataButton = findViewById(R.id.export_data_button);

        // Configurar bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_reports);

        // Configurar spinner de períodos de tiempo
        String[] timePeriods = {"Last 7 days", "Last 30 days", "Last 3 months", "Last year"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timePeriods);
        timePeriodSpinner.setAdapter(adapter);

        // Configurar botón de exportación
        exportDataButton.setOnClickListener(v -> {
            Toast.makeText(this, "Data exported successfully", Toast.LENGTH_SHORT).show();
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
            intent = new Intent(this, PhysicalActivityTracker.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.navigation_food) {
            intent = new Intent(this, FoodTrackerActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.navigation_reports) {
            return true;
        }
        
        return false;
    }
}