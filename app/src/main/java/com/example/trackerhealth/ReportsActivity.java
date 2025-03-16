package com.example.trackerhealth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ReportsActivity extends AppCompatActivity {

    private TextView caloriesConsumedTextView;
    private TextView caloriesBurnedTextView;
    private TextView netCaloriesTextView;
    private TextView activitySummaryTextView;
    private TextView nutritionSummaryTextView;
    private Button weeklyReportButton;
    private Button monthlyReportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        // Initialize UI components
        caloriesConsumedTextView = findViewById(R.id.calories_consumed_text_view);
        caloriesBurnedTextView = findViewById(R.id.calories_burned_text_view);
        netCaloriesTextView = findViewById(R.id.net_calories_text_view);
        activitySummaryTextView = findViewById(R.id.activity_summary_text_view);
        nutritionSummaryTextView = findViewById(R.id.nutrition_summary_text_view);
        weeklyReportButton = findViewById(R.id.weekly_report_button);
        monthlyReportButton = findViewById(R.id.monthly_report_button);

        weeklyReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadWeeklyReport();
            }
        });

        monthlyReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMonthlyReport();
            }
        });

        // Load default report (daily)
        loadDailyReport();
    }

    private void loadDailyReport() {
        // TODO: Load daily report data from database
        updateReportUI(0, 0, "No activities recorded today", "No meals recorded today");
    }

    private void loadWeeklyReport() {
        // TODO: Load weekly report data from database
        updateReportUI(0, 0, "No activities recorded this week", "No meals recorded this week");
    }

    private void loadMonthlyReport() {
        // TODO: Load monthly report data from database
        updateReportUI(0, 0, "No activities recorded this month", "No meals recorded this month");
    }

    private void updateReportUI(int caloriesConsumed, int caloriesBurned, 
                               String activitySummary, String nutritionSummary) {
        caloriesConsumedTextView.setText(String.valueOf(caloriesConsumed));
        caloriesBurnedTextView.setText(String.valueOf(caloriesBurned));
        netCaloriesTextView.setText(String.valueOf(caloriesConsumed - caloriesBurned));
        activitySummaryTextView.setText(activitySummary);
        nutritionSummaryTextView.setText(nutritionSummary);
    }
} 