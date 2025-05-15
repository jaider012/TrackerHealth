package com.example.trackerhealth;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trackerhealth.dao.MealDAO;
import com.example.trackerhealth.dao.PhysicalActivityDAO;
import com.example.trackerhealth.dao.UserDAO;
import com.example.trackerhealth.helpers.ChartHelper;
import com.example.trackerhealth.model.Meal;
import com.example.trackerhealth.model.PhysicalActivity;
import com.example.trackerhealth.model.User;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private Spinner timePeriodSpinner;
    private Button exportDataButton;
    private TextView totalActivitiesValue;
    private TextView totalDistanceValue;
    private TextView avgCaloriesValue;
    private TextView totalMealsValue;
    private BarChart activityChart;
    private PieChart nutritionChart;
    private LineChart caloriesChart;
    
    // DAOs
    private UserDAO userDAO;
    private MealDAO mealDAO;
    private PhysicalActivityDAO activityDAO;
    
    // Current user
    private User currentUser;
    private long userId = 1; // Default, should be replaced with actual logged-in user ID
    
    // Data lists
    private List<PhysicalActivity> activities;
    private List<Meal> meals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        // Initialize DAOs
        userDAO = new UserDAO(this);
        mealDAO = new MealDAO(this);
        activityDAO = new PhysicalActivityDAO(this);
        
        // Get current user (for now using userId = 1, should be replaced with user session management)
        currentUser = userDAO.getUserById(userId);
        
        // If no user found, create a test user
        if (currentUser == null) {
            // This is just for testing, in a real app you would redirect to login
            User testUser = new User("Test User", "test@example.com", "password");
            userId = userDAO.insertUser(testUser);
            currentUser = userDAO.getUserById(userId);
        }

        // Initialize components
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        timePeriodSpinner = findViewById(R.id.time_period_spinner);
        exportDataButton = findViewById(R.id.export_data_button);
        totalActivitiesValue = findViewById(R.id.total_activities_value);
        totalDistanceValue = findViewById(R.id.total_distance_value);
        avgCaloriesValue = findViewById(R.id.avg_calories_value);
        totalMealsValue = findViewById(R.id.total_meals_value);
        activityChart = findViewById(R.id.activity_chart);
        nutritionChart = findViewById(R.id.nutrition_chart);
        caloriesChart = findViewById(R.id.calories_chart);

        // Configure bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_reports);

        // Configure time period spinner
        String[] timePeriods = {"Last 7 days", "Last 30 days", "Last 3 months", "Last year"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timePeriods);
        timePeriodSpinner.setAdapter(adapter);
        
        // Set up time period change listener
        timePeriodSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                updateReports(position);
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Configure export button
        exportDataButton.setOnClickListener(v -> {
            exportData();
        });
        
        // Load initial data (default to last 7 days)
        updateReports(0);
    }
    
    /**
     * Updates the reports based on the selected time period
     * @param periodIndex Index of the selected time period (0: 7 days, 1: 30 days, 2: 3 months, 3: 1 year)
     */
    private void updateReports(int periodIndex) {
        // Calculate date range based on selected period
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        
        switch (periodIndex) {
            case 0: // Last 7 days
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case 1: // Last 30 days
                calendar.add(Calendar.DAY_OF_YEAR, -30);
                break;
            case 2: // Last 3 months
                calendar.add(Calendar.MONTH, -3);
                break;
            case 3: // Last year
                calendar.add(Calendar.YEAR, -1);
                break;
        }
        
        Date startDate = calendar.getTime();
        
        // Format dates for database queries
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);
        
        // Get activities in date range
        activities = activityDAO.getActivitiesByDateRange(userId, startDateStr, endDateStr);
        
        // Calculate total activities and distance
        int totalActivities = activities.size();
        double totalDistance = 0;
        
        for (PhysicalActivity activity : activities) {
            totalDistance += activity.getDistance();
        }
        
        // Update activity stats
        totalActivitiesValue.setText(String.valueOf(totalActivities));
        totalDistanceValue.setText(String.format(Locale.getDefault(), "%.1f", totalDistance));
        
        // Get meals in date range
        meals = new ArrayList<>();
        
        // Calculate date range
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(endDate);
        
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        
        // For each day in range, get meals
        while (!currentDate.before(startCal)) {
            String currentDateStr = dateFormat.format(currentDate.getTime());
            List<Meal> dailyMeals = mealDAO.getMealsForDate(userId, currentDateStr);
            meals.addAll(dailyMeals);
            currentDate.add(Calendar.DAY_OF_YEAR, -1);
        }
        
        // Calculate total meals and average calories
        int totalMeals = meals.size();
        int totalCalories = 0;
        
        for (Meal meal : meals) {
            totalCalories += meal.getCalories();
        }
        
        int avgCalories = totalMeals > 0 ? totalCalories / totalMeals : 0;
        
        // Update meal stats
        totalMealsValue.setText(String.valueOf(totalMeals));
        avgCaloriesValue.setText(String.valueOf(avgCalories));
        
        // Update charts
        updateCharts();
    }
    
    /**
     * Updates all charts with the current data
     */
    private void updateCharts() {
        // Activity chart
        ChartHelper.setupActivityChart(activityChart, activities);
        
        // Nutrition chart
        ChartHelper.setupNutritionChart(nutritionChart, meals);
        
        // Calories chart
        ChartHelper.setupCaloriesChart(caloriesChart, meals);
    }
    
    /**
     * Exports the data to a CSV file
     */
    private void exportData() {
        // Here we would implement actual data export functionality
        // For now, just show a message
        Toast.makeText(this, "Data exported successfully", Toast.LENGTH_SHORT).show();
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