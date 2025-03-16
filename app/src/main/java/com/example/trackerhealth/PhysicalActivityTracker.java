package com.example.trackerhealth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trackerhealth.dao.PhysicalActivityDAO;
import com.example.trackerhealth.model.PhysicalActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PhysicalActivityTracker extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private Spinner activityTypeSpinner;
    private EditText durationEditText;
    private EditText distanceEditText;
    private CheckBox useGpsCheckbox;
    private LinearLayout gpsContainer;
    private Button startTrackingButton;
    private Button saveActivityButton;
    
    private PhysicalActivityDAO activityDAO;
    
    // Constantes para SharedPreferences
    private static final String PREF_NAME = "TrackerHealthPrefs";
    private static final String KEY_USER_ID = "user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_tracker);

        // Inicializar base de datos
        activityDAO = new PhysicalActivityDAO(this);
        
        // Inicializar componentes
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        activityTypeSpinner = findViewById(R.id.activity_type_spinner);
        durationEditText = findViewById(R.id.duration_input);
        distanceEditText = findViewById(R.id.distance_input);
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
        if (useGpsCheckbox != null && gpsContainer != null) {
            useGpsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                gpsContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });
        }

        // Configurar botón de tracking
        if (startTrackingButton != null) {
            startTrackingButton.setOnClickListener(v -> {
                if (startTrackingButton.getText().equals(getString(R.string.start_tracking))) {
                    startTrackingButton.setText(R.string.stop_tracking);
                    Toast.makeText(this, "GPS tracking started", Toast.LENGTH_SHORT).show();
                } else {
                    startTrackingButton.setText(R.string.start_tracking);
                    Toast.makeText(this, "GPS tracking stopped", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Configurar botón de guardar
        saveActivityButton.setOnClickListener(v -> {
            saveActivityToDatabase();
        });
    }
    
    /**
     * Guarda la actividad física en la base de datos
     */
    private void saveActivityToDatabase() {
        // Validar que los campos obligatorios estén completos
        if (TextUtils.isEmpty(durationEditText.getText())) {
            durationEditText.setError(getString(R.string.error_field_required));
            durationEditText.requestFocus();
            return;
        }
        
        try {
            // Obtener el ID del usuario de SharedPreferences
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            long userId = prefs.getLong(KEY_USER_ID, -1);
            
            if (userId == -1) {
                Toast.makeText(this, "Error: No hay un usuario logueado", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Obtener los valores de los campos
            String activityType = activityTypeSpinner.getSelectedItem().toString();
            int duration = Integer.parseInt(durationEditText.getText().toString());
            
            // Valores opcionales
            double distance = 0;
            int calories = 0; // Estimación de calorías basada en la duración y tipo de actividad
            String notes = "";
            
            if (distanceEditText != null && !TextUtils.isEmpty(distanceEditText.getText())) {
                distance = Double.parseDouble(distanceEditText.getText().toString());
            }
            
            // Estimación simple de calorías quemadas (esto debería ser más sofisticado en una app real)
            calories = estimateCaloriesBurned(activityType, duration, distance);
            
            // Crear el objeto de actividad
            PhysicalActivity activity = new PhysicalActivity(userId, activityType, duration, calories, distance, notes);
            
            // Guardar en la base de datos
            long activityId = activityDAO.insertActivity(activity);
            
            if (activityId > 0) {
                Toast.makeText(this, "Actividad guardada exitosamente", Toast.LENGTH_SHORT).show();
                // Limpiar campos después de guardar
                clearFields();
            } else {
                Toast.makeText(this, "Error al guardar la actividad", Toast.LENGTH_SHORT).show();
            }
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa valores numéricos válidos", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Estima las calorías quemadas basado en el tipo de actividad, duración y distancia
     */
    private int estimateCaloriesBurned(String activityType, int duration, double distance) {
        // Valores aproximados de calorías quemadas por minuto para diferentes actividades
        int caloriesPerMinute;
        
        switch (activityType.toLowerCase()) {
            case "running":
                caloriesPerMinute = 10;
                break;
            case "cycling":
                caloriesPerMinute = 8;
                break;
            case "swimming":
                caloriesPerMinute = 9;
                break;
            case "gym workout":
                caloriesPerMinute = 7;
                break;
            case "walking":
                caloriesPerMinute = 5;
                break;
            default:
                caloriesPerMinute = 6;
                break;
        }
        
        return caloriesPerMinute * duration;
    }
    
    /**
     * Limpia los campos después de guardar
     */
    private void clearFields() {
        durationEditText.setText("");
        
        if (distanceEditText != null) {
            distanceEditText.setText("");
        }
        
        // Volver al primer elemento del spinner
        activityTypeSpinner.setSelection(0);
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