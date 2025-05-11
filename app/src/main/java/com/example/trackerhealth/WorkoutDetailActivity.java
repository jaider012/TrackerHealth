package com.example.trackerhealth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trackerhealth.model.PhysicalActivity;
import com.example.trackerhealth.dao.PhysicalActivityDAO;
import com.example.trackerhealth.util.LocationTrackingUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String EXTRA_ACTIVITY_ID = "activity_id";
    private static final String EXTRA_ACTIVITY_TYPE = "activity_type";

    private TextView tvDistance;
    private TextView tvCalories;
    private TextView tvHeartRate;
    private TextView tvProgressPercentage;
    private Button btnRunning;
    private Button btnSwimming;
    private Button btnCycling;
    private ImageButton backButton;
    private FloatingActionButton fabEdit;
    private FloatingActionButton fabSave;
    private EditText etNotes;
    private EditText etDuration;
    private EditText etDistance;
    private EditText etCalories;
    
    private GoogleMap mMap;
    private long activityId;
    private String activityType;
    private PhysicalActivity activity;
    private PhysicalActivityDAO activityDAO;
    private boolean isEditMode = false;
    
    public static void start(AppCompatActivity context, long activityId, String activityType) {
        Intent intent = new Intent(context, WorkoutDetailActivity.class);
        intent.putExtra(EXTRA_ACTIVITY_ID, activityId);
        intent.putExtra(EXTRA_ACTIVITY_TYPE, activityType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);
        
        activityDAO = new PhysicalActivityDAO(this);
        
        // Get activity info from intent
        activityId = getIntent().getLongExtra(EXTRA_ACTIVITY_ID, -1);
        activityType = getIntent().getStringExtra(EXTRA_ACTIVITY_TYPE);
        
        // Initialize UI components
        initializeViews();
        
        // Set activity type buttons click listeners
        setupActivityTypeButtons();
        
        // Back button listener
        backButton.setOnClickListener(v -> finish());
        
        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, R.string.map_not_available, Toast.LENGTH_SHORT).show();
        }
        
        // Load activity data
        loadActivityData();
        
        // Set workout progress
        setupWorkoutProgress();
        
        // Setup edit mode
        setupEditMode();
    }
    
    private void initializeViews() {
        tvDistance = findViewById(R.id.tv_distance);
        tvCalories = findViewById(R.id.tv_calories);
        tvHeartRate = findViewById(R.id.tv_heart_rate);
        tvProgressPercentage = findViewById(R.id.tv_progress_percentage);
        btnRunning = findViewById(R.id.btn_running);
        btnSwimming = findViewById(R.id.btn_swimming);
        btnCycling = findViewById(R.id.btn_cycling);
        backButton = findViewById(R.id.back_button);
        fabEdit = findViewById(R.id.fab_edit);
        fabSave = findViewById(R.id.fab_save);
        etNotes = findViewById(R.id.et_notes);
        etDuration = findViewById(R.id.et_duration);
        etDistance = findViewById(R.id.et_distance);
        etCalories = findViewById(R.id.et_calories);
    }
    
    private void setupEditMode() {
        fabEdit.setOnClickListener(v -> {
            isEditMode = true;
            updateEditMode();
        });
        
        fabSave.setOnClickListener(v -> {
            if (saveChanges()) {
                isEditMode = false;
                updateEditMode();
            }
        });
        
        updateEditMode();
    }
    
    private void updateEditMode() {
        int viewMode = isEditMode ? View.VISIBLE : View.GONE;
        int editMode = isEditMode ? View.GONE : View.VISIBLE;
        
        // Toggle visibility of views
        tvDistance.setVisibility(editMode);
        tvCalories.setVisibility(editMode);
        etDistance.setVisibility(viewMode);
        etCalories.setVisibility(viewMode);
        etDuration.setVisibility(viewMode);
        etNotes.setVisibility(viewMode);
        
        fabEdit.setVisibility(editMode);
        fabSave.setVisibility(viewMode);
        
        // Update EditText fields when entering edit mode
        if (isEditMode && activity != null) {
            etDistance.setText(String.valueOf(activity.getDistance()));
            etCalories.setText(String.valueOf(activity.getCaloriesBurned()));
            etDuration.setText(String.valueOf(activity.getDuration()));
            etNotes.setText(activity.getNotes());
        }
    }
    
    private boolean saveChanges() {
        if (activity == null) return false;
        
        try {
            // Update activity object with new values
            activity.setDistance(Double.parseDouble(etDistance.getText().toString()));
            activity.setCaloriesBurned(Integer.parseInt(etCalories.getText().toString()));
            activity.setDuration(Integer.parseInt(etDuration.getText().toString()));
            activity.setNotes(etNotes.getText().toString());
            
            // Save to database
            int result = activityDAO.update(activity);
            
            if (result > 0) {
                Toast.makeText(this, "Activity updated successfully", Toast.LENGTH_SHORT).show();
                loadActivityData(); // Refresh displayed data
                return true;
            } else {
                Toast.makeText(this, "Failed to update activity", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    
    private void setupActivityTypeButtons() {
        // Default selection based on activity type
        updateActivityTypeSelection(activityType);
        
        btnRunning.setOnClickListener(v -> updateActivityTypeSelection("Running"));
        btnSwimming.setOnClickListener(v -> updateActivityTypeSelection("Swimming"));
        btnCycling.setOnClickListener(v -> updateActivityTypeSelection("Cycling"));
    }
    
    private void updateActivityTypeSelection(String type) {
        // Reset all buttons to outline style
        btnRunning.setBackgroundResource(R.drawable.rounded_button_outline);
        btnRunning.setTextColor(getResources().getColor(android.R.color.black));
        
        btnSwimming.setBackgroundResource(R.drawable.rounded_button_outline);
        btnSwimming.setTextColor(getResources().getColor(android.R.color.black));
        
        btnCycling.setBackgroundResource(R.drawable.rounded_button_outline);
        btnCycling.setTextColor(getResources().getColor(android.R.color.black));
        
        // Highlight selected button
        switch (type.toLowerCase()) {
            case "running":
                btnRunning.setBackgroundResource(R.drawable.rounded_button_filled);
                btnRunning.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "swimming":
                btnSwimming.setBackgroundResource(R.drawable.rounded_button_filled);
                btnSwimming.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "cycling":
                btnCycling.setBackgroundResource(R.drawable.rounded_button_filled);
                btnCycling.setTextColor(getResources().getColor(android.R.color.white));
                break;
        }
        
        if (activity != null && isEditMode) {
            activity.setActivityType(type);
        }
    }
    
    private void loadActivityData() {
        if (activityId != -1) {
            // Load activity from database
            activity = activityDAO.getActivityById(activityId);
            
            if (activity != null) {
                // Update metrics
                tvDistance.setText(String.format(Locale.getDefault(), "%.1fkm", activity.getDistance()));
                tvCalories.setText(String.format(Locale.getDefault(), "%dkcal", activity.getCaloriesBurned()));
                
                // Get heart rate from notes if available (example format in notes: "heartRate:95")
                String heartRate = activity.getInfoFromNotes("heartRate");
                if (!heartRate.isEmpty()) {
                    tvHeartRate.setText(String.format("%sbpm", heartRate));
                } else {
                    // Default value if not available
                    tvHeartRate.setText("--bpm");
                }
                
                // Update activity type selection
                updateActivityTypeSelection(activity.getActivityType());
            }
        }
    }
    
    private void setupWorkoutProgress() {
        // Calculate progress percentage based on current date vs. month days
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        int progressPercentage = (currentDay * 100) / maxDays;
        tvProgressPercentage.setText(String.format(Locale.getDefault(), "%d%%", progressPercentage));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        
        if (activity != null) {
            if (activity.hasLocationData()) {
                // Show the starting point marker
                LatLng startLocation = new LatLng(activity.getLatitude(), activity.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(startLocation)
                        .title(activity.getActivityType())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                
                // Load route data if available
                loadRouteData();
            } else {
                // No location data, show a default location or a message
                Toast.makeText(this, "No GPS data available for this activity", Toast.LENGTH_SHORT).show();
                
                // Center map on a default location
                LatLng defaultLocation = new LatLng(37.7749, -122.4194); // San Francisco by default
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
            }
        }
    }
    
    private void loadRouteData() {
        if (activity != null && activityId != -1) {
            // Try to load GPS route data
            List<android.location.Location> routeLocations = LocationTrackingUtil.loadRouteData(this, activityId);
            
            if (routeLocations != null && !routeLocations.isEmpty()) {
                // Convert locations to LatLng for map display
                List<LatLng> routePoints = new ArrayList<>();
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                
                for (android.location.Location location : routeLocations) {
                    LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
                    routePoints.add(point);
                    boundsBuilder.include(point);
                }
                
                // Draw the polyline for the route
                mMap.addPolyline(new PolylineOptions()
                        .addAll(routePoints)
                        .width(8)
                        .color(0x7F7052E7)); // Semi-transparent purple
                
                try {
                    // Animate camera to show the entire route with padding
                    LatLngBounds bounds = boundsBuilder.build();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                } catch (Exception e) {
                    // Fallback if bounds calculation fails
                    if (!routePoints.isEmpty()) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(routePoints.get(0), 14));
                    }
                }
            } else {
                // No route data, just show the starting point
                LatLng location = new LatLng(activity.getLatitude(), activity.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));
            }
        }
    }
} 