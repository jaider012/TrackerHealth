package com.example.trackerhealth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PhysicalActivityTracker extends AppCompatActivity {

    private Spinner activityTypeSpinner;
    private EditText durationEditText;
    private EditText caloriesEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_tracker);

        // Initialize UI components
        activityTypeSpinner = findViewById(R.id.activity_type_spinner);
        durationEditText = findViewById(R.id.duration_edit_text);
        caloriesEditText = findViewById(R.id.calories_edit_text);
        saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveActivityData();
            }
        });
    }

    private void saveActivityData() {
        String activityType = activityTypeSpinner.getSelectedItem().toString();
        String duration = durationEditText.getText().toString();
        String calories = caloriesEditText.getText().toString();

        // Validate inputs
        if (activityType.isEmpty() || duration.isEmpty() || calories.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Save activity data to database

        Toast.makeText(this, "Activity saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
} 