package com.example.trackerhealth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.trackerhealth.dao.PhysicalActivityDAO;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RunningDashboardActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    
    // UI components
    private TextView tvWelcomeMessage;
    private CardView cardSpeedPower;
    private CardView cardEnduranceRun;
    private ImageButton btnHome;
    private ImageButton btnStats;
    private ImageButton btnMap;
    private ImageButton btnMessages;
    private ImageButton btnProfile;
    
    // Location tracking
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private GoogleMap mMap;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_dashboard);
        
        // Initialize UI components
        tvWelcomeMessage = findViewById(R.id.welcome_text);
        cardSpeedPower = findViewById(R.id.card_speed_power);
        cardEnduranceRun = findViewById(R.id.card_endurance_run);
        btnHome = findViewById(R.id.btn_home);
        btnStats = findViewById(R.id.btn_stats);
        btnMap = findViewById(R.id.btn_map);
        btnMessages = findViewById(R.id.btn_messages);
        btnProfile = findViewById(R.id.btn_profile);
        
        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();
        
        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Set welcome message with current date
        setWelcomeMessage();
        
        // Setup click listeners
        setupClickListeners();
    }
    
    private void setWelcomeMessage() {
        // Get current user's name - in a real app, this would come from user preferences or database
        String userName = "Brenda";
        
        // Set welcome message
        tvWelcomeMessage.setText(getString(R.string.welcome_back_user, userName));
    }
    
    private void setupClickListeners() {
        // Bottom navigation buttons
        btnHome.setOnClickListener(v -> {
            // Already on home screen
        });
        
        btnStats.setOnClickListener(v -> {
            Toast.makeText(this, "Stats clicked", Toast.LENGTH_SHORT).show();
        });
        
        btnMap.setOnClickListener(v -> {
            // Open map tracking activity
            Intent intent = new Intent(this, MapTrackingActivity.class);
            startActivity(intent);
        });
        
        btnMessages.setOnClickListener(v -> {
            Toast.makeText(this, "Messages clicked", Toast.LENGTH_SHORT).show();
        });
        
        btnProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
        });
        
        // Workout cards
        cardSpeedPower.setOnClickListener(v -> {
            // For demo purposes, create a dummy workout detail
            openWorkoutDetail(1, "Running");
        });
        
        cardEnduranceRun.setOnClickListener(v -> {
            // For demo purposes, create a dummy workout detail
            openWorkoutDetail(2, "Running");
        });
    }
    ƒ
    /**
     * Setup location request parameters
     */
    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(10000) // Update every 10 seconds
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(5000) // Minimum 5 seconds
                .setMaxUpdateDelayMillis(15000) // Maximum delay 15 seconds
                .build();
    }
    
    /**
     * Setup location callback
     */
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update current location
                    currentLocation = location;
                    
                    // Update map with new location
                    if (mMap != null) {
                        updateMapLocation();
                    }
                }
            }
        };
    }
    
    /**
     * Update map with current location
     */
    private void updateMapLocation() {
        if (currentLocation != null && mMap != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            
            // Clear previous markers and add new one
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
            
            // Move camera to current location
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        }
    }
    
    /**
     * Start location updates
     */
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        } else {
            requestLocationPermission();
        }
    }
    
    /**
     * Stop location updates
     */
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
    
    /**
     * Request location permission
     */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Enable my location button if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            requestLocationPermission();
        }
        
        // Set default location (San Francisco) until we get the actual location
        LatLng defaultLocation = new LatLng(37.7749, -122.4194);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));
        
        // Get current location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLocation = location;
                    updateMapLocation();
                }
            });
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                startLocationUpdates();
                
                // Enable my location layer on map
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    }
                }
            } else {
                Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
} 