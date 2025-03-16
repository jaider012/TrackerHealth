package com.example.trackerhealth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class FoodTrackerActivity extends AppCompatActivity {

    private Spinner mealTypeSpinner;
    private EditText foodNameEditText;
    private EditText caloriesEditText;
    private EditText proteinEditText;
    private EditText carbsEditText;
    private EditText fatsEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_tracker);

        // Initialize UI components
        mealTypeSpinner = findViewById(R.id.meal_type_spinner);
        foodNameEditText = findViewById(R.id.food_name_edit_text);
        caloriesEditText = findViewById(R.id.calories_edit_text);
        proteinEditText = findViewById(R.id.protein_edit_text);
        carbsEditText = findViewById(R.id.carbs_edit_text);
        fatsEditText = findViewById(R.id.fats_edit_text);
        saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFoodData();
            }
        });
    }

    private void saveFoodData() {
        String mealType = mealTypeSpinner.getSelectedItem().toString();
        String foodName = foodNameEditText.getText().toString();
        String calories = caloriesEditText.getText().toString();
        String protein = proteinEditText.getText().toString();
        String carbs = carbsEditText.getText().toString();
        String fats = fatsEditText.getText().toString();

        // Validate inputs
        if (mealType.isEmpty() || foodName.isEmpty() || calories.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Save food data to database

        Toast.makeText(this, "Food entry saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
} 