package com.example.trackerhealth;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Set Dashboard as selected by default
        bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.navigation_dashboard:
                return true;

            case R.id.navigation_activity:
                intent = new Intent(this, PhysicalActivityTracker.class);
                startActivity(intent);
                return true;

            case R.id.navigation_food:
                intent = new Intent(this, FoodTrackerActivity.class);
                startActivity(intent);
                return true;

            case R.id.navigation_reports:
                intent = new Intent(this, ReportsActivity.class);
                startActivity(intent);
                return true;
        }

        return false;
    }
}Ï