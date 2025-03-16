package com.example.trackerhealth;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FoodTrackerActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private Spinner mealTypeSpinner;
    private Button takePhotoButton;
    private Button saveMealButton;
    private ImageView foodPhotoPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_tracker);

        // Inicializar componentes
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        mealTypeSpinner = findViewById(R.id.meal_type_spinner);
        takePhotoButton = findViewById(R.id.take_photo_button);
        saveMealButton = findViewById(R.id.save_meal_button);
        foodPhotoPreview = findViewById(R.id.food_photo_preview);

        // Configurar bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_food);

        // Configurar spinner de tipos de comida
        String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mealTypes);
        mealTypeSpinner.setAdapter(adapter);

        // Configurar botón de foto
        takePhotoButton.setOnClickListener(v -> {
            // Aquí se abriría la cámara (en una implementación real)
            Toast.makeText(this, "Camera functionality would open here", Toast.LENGTH_SHORT).show();
        });

        // Configurar botón de guardar
        saveMealButton.setOnClickListener(v -> {
            Toast.makeText(this, "Meal saved successfully", Toast.LENGTH_SHORT).show();
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
            return true;
        } else if (itemId == R.id.navigation_reports) {
            intent = new Intent(this, ReportsActivity.class);
            startActivity(intent);
            return true;
        }
        
        return false;
    }
}