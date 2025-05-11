package com.example.trackerhealth;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WorkoutDetailActivity extends AppCompatActivity {

    private TextView tvWorkoutTitle;
    private TextView tvWorkoutDescription;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);

        // Initialize views
        tvWorkoutTitle = findViewById(R.id.tv_workout_title);
        tvWorkoutDescription = findViewById(R.id.tv_workout_description);
        btnBack = findViewById(R.id.btn_back);

        // Get workout details from intent
        int workoutId = getIntent().getIntExtra("workout_id", -1);
        String workoutType = getIntent().getStringExtra("workout_type");

        // Set workout details
        setupWorkoutDetails(workoutId, workoutType);

        // Setup click listeners
        setupClickListeners();
    }

    private void setupWorkoutDetails(int workoutId, String workoutType) {
        // Here you would typically load workout details from a database
        // For now, we'll use some dummy data based on the workout ID
        String title;
        String description;

        switch (workoutId) {
            case 1:
                title = "Speed & Power Training";
                description = "High-intensity interval training designed to improve your speed and power. " +
                        "Includes sprint intervals and dynamic exercises.";
                break;
            case 2:
                title = "Endurance Run";
                description = "Long-distance running session focused on building stamina and endurance. " +
                        "Maintain a steady pace throughout the workout.";
                break;
            default:
                title = workoutType + " Workout";
                description = "Custom workout program";
                break;
        }

        tvWorkoutTitle.setText(title);
        tvWorkoutDescription.setText(description);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
} 