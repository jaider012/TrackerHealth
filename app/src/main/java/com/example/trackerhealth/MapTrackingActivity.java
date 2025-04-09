package com.example.trackerhealth;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.trackerhealth.dao.PhysicalActivityDAO;
import com.example.trackerhealth.model.PhysicalActivity;
import com.example.trackerhealth.util.LocationUtils;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int UPDATE_INTERVAL = 5000; // 5 seconds
    private static final int FASTEST_INTERVAL = 3000; // 3 seconds
    
    private static final float MIN_ACCURACY_THRESHOLD = 20.0f; // metros
    private static final int LOCATION_BUFFER_SIZE = 100; // Máximo número de ubicaciones a mantener
    
    // UI components
    private TextView tvDistance;
    private TextView tvElapsedTime;
    private ImageButton btnStartPause;
    private ImageButton btnBack;
    private ImageView userAvatar;
    
    // Location tracking
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private GoogleMap mMap;
    private Location currentLocation;
    private List<LatLng> routePoints;
    
    // Activity tracking
    private boolean isTracking = false;
    private float distanceTraveled = 0;
    private long startTimeMillis = 0;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    
    // Variables adicionales para mejorar el tracking
    private List<LatLng> filteredRoutePoints = new ArrayList<>();
    private boolean hasGpsFix = false;
    private long lastLocationUpdateTime = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_tracking);
        
        // Initialize UI components
        tvDistance = findViewById(R.id.tv_distance);
        tvElapsedTime = findViewById(R.id.tv_elapsed_time);
        btnStartPause = findViewById(R.id.start_pause_btn);
        btnBack = findViewById(R.id.btn_back);
        userAvatar = findViewById(R.id.user_avatar);
        
        // Initialize route points list
        routePoints = new ArrayList<>();
        
        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();
        
        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_tracking_view);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Setup timer runnable
        setupTimerRunnable();
        
        // Setup click listeners
        setupClickListeners();
    }
    
    private void setupTimerRunnable() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millisecondsElapsed = SystemClock.elapsedRealtime() - startTimeMillis;
                updateElapsedTimeText(millisecondsElapsed);
                timerHandler.postDelayed(this, 1000);
            }
        };
    }
    
    private void updateElapsedTimeText(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
        
        tvElapsedTime.setText(String.format(Locale.getDefault(), 
                "%02d:%02d:%02d", hours, minutes, seconds));
    }
    
    private void setupClickListeners() {
        btnStartPause.setOnClickListener(v -> {
            if (isTracking) {
                pauseTracking();
            } else {
                startTracking();
            }
        });
        
        btnBack.setOnClickListener(v -> {
            if (isTracking) {
                // Confirm before exiting
                Toast.makeText(this, "Stop tracking to exit", Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        });
    }
    
    private void startTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            
            isTracking = true;
            startTimeMillis = SystemClock.elapsedRealtime();
            timerHandler.postDelayed(timerRunnable, 0);
            
            // Change button icon to pause
            btnStartPause.setImageResource(android.R.drawable.ic_media_pause);
            
            // Show user avatar
            userAvatar.setVisibility(View.VISIBLE);
            
            // Clear previous route if any
            if (mMap != null) {
                mMap.clear();
                routePoints.clear();
                distanceTraveled = 0;
                updateDistanceText();
            }
            
            // Start location updates
            startLocationUpdates();
            
        } else {
            requestLocationPermission();
        }
    }
    
    private void pauseTracking() {
        isTracking = false;
        
        // Stop timer
        timerHandler.removeCallbacks(timerRunnable);
        
        // Change button icon to play
        btnStartPause.setImageResource(android.R.drawable.ic_media_play);
        
        // Save the activity
        saveActivity();
        
        // Stop location updates
        stopLocationUpdates();
    }
    
    private void saveActivity() {
        if (distanceTraveled > 0) {
            // Create a new physical activity
            PhysicalActivity activity = new PhysicalActivity();
            activity.setDistance(distanceTraveled / 1000); // Convert to kilometers
            
            // Calculate calories burned (simple estimate based on distance)
            int caloriesBurned = (int)(distanceTraveled * 0.06); // ~60 calories per km
            activity.setCaloriesBurned(caloriesBurned);
            
            // Add any additional data
            String notes = "heartRate:75"; // Placeholder heart rate data
            activity.setNotes(notes);
            
            // Save to database
            PhysicalActivityDAO dao = new PhysicalActivityDAO(this);
            long activityId = dao.addActivity(activity);
            
            if (activityId > 0) {
                Toast.makeText(this, "Activity saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save activity", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void updateDistanceText() {
        // Display distance in km (with 2 decimal places)
        float distanceKm = distanceTraveled / 1000;
        tvDistance.setText(String.format(Locale.getDefault(), "%.2f km", distanceKm));
    }
    
    /**
     * Setup location request parameters
     */
    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(UPDATE_INTERVAL)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
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
                    // Only process location updates if tracking is active
                    if (isTracking) {
                        lastLocationUpdateTime = System.currentTimeMillis();
                        
                        // Verificar si la ubicación tiene buena precisión
                        if (isValidLocation(location)) {
                            // Si es la primera ubicación buena, indicar que tenemos GPS fix
                            if (!hasGpsFix) {
                                hasGpsFix = true;
                                Toast.makeText(MapTrackingActivity.this, "GPS signal acquired", Toast.LENGTH_SHORT).show();
                            }
                            
                            // Calculate distance
                            if (currentLocation != null) {
                                // Usar el método Haversine para mayor precisión en largas distancias
                                float distance = LocationUtils.calculateDistance(
                                        currentLocation.getLatitude(), currentLocation.getLongitude(),
                                        location.getLatitude(), location.getLongitude());
                                
                                // Verificar que la distancia sea razonable
                                if (distance > 0 && distance < 100) { // Ignorar saltos mayores a 100m
                                    distanceTraveled += distance;
                                    updateDistanceText();
                                }
                            }
                            
                            // Update current location
                            currentLocation = location;
                            
                            // Update map
                            updateMapWithLocation(location);
                        } else {
                            // Location is not valid enough
                            Log.d("MapTracking", "Ignoring low quality location");
                        }
                    }
                }
            }
        };
    }
    
    /**
     * Verifica si una ubicación es válida y precisa
     */
    private boolean isValidLocation(Location location) {
        // Verificar si la ubicación tiene buena precisión
        if (location.hasAccuracy() && location.getAccuracy() > MIN_ACCURACY_THRESHOLD) {
            return false;
        }
        
        // Verificar velocidad de movimiento (si la ubicación tiene velocidad)
        if (location.hasSpeed() && location.getSpeed() > 30) { // Más de 30 m/s (108 km/h)
            return false;
        }
        
        return true;
    }
    
    /**
     * Update map with the new location and draw route
     */
    private void updateMapWithLocation(Location location) {
        if (mMap != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            
            // Add point to route
            routePoints.add(latLng);
            
            // Limit memory usage by keeping only a certain number of points
            if (routePoints.size() > LOCATION_BUFFER_SIZE) {
                // Keep only the newest points
                routePoints = new ArrayList<>(routePoints.subList(
                        routePoints.size() - LOCATION_BUFFER_SIZE, 
                        routePoints.size()));
            }
            
            // Agregar punto al trazado filtrado solo si estamos en movimiento
            if (location.hasSpeed() && location.getSpeed() > 0.5 || // Si hay velocidad reportada > 0.5 m/s
                    (filteredRoutePoints.isEmpty() || // O si es el primer punto
                    !filteredRoutePoints.isEmpty() && 
                        distanceBetweenPoints(filteredRoutePoints.get(filteredRoutePoints.size() - 1), latLng) > 5)) { // O si nos movimos más de 5m
                
                filteredRoutePoints.add(latLng);
            }
            
            // Draw route - solo usamos los puntos filtrados para dibujar
            if (filteredRoutePoints.size() > 1) {
                // Draw the filtered route
                mMap.clear();
                
                // Usar un color vibrante para la ruta
                int routeColor = Color.rgb(66, 133, 244); // Azul Google
                
                mMap.addPolyline(new PolylineOptions()
                        .addAll(filteredRoutePoints)
                        .width(12) // Línea un poco más gruesa
                        .color(routeColor)
                        .jointType(JointType.ROUND) // Juntas redondeadas
                        .geodesic(true)); // Seguir curvatura de la Tierra
            }
            
            // Add marker at current location - usar un icono personalizado o de menor tamaño
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            
            if (routePoints.size() > 1) {
                // Si hay más de un punto, orientar el marcador en la dirección del movimiento
                LatLng prevPoint = routePoints.get(routePoints.size() - 2);
                float bearing = bearingBetweenPoints(prevPoint, latLng);
                markerOptions.rotation(bearing);
                
                // Actualizar la cámara para seguir la dirección del movimiento
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(latLng)
                                .zoom(17f)
                                .bearing(bearing)
                                .tilt(45) // Ángulo de inclinación para mejor visualización
                                .build()
                ));
            } else {
                // Move camera to follow user - vista simple
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));
            }
            
            mMap.addMarker(markerOptions);
        }
    }
    
    /**
     * Calcula la distancia entre dos puntos LatLng
     */
    private float distanceBetweenPoints(LatLng point1, LatLng point2) {
        return LocationUtils.calculateDistance(
                point1.latitude, point1.longitude,
                point2.latitude, point2.longitude);
    }
    
    /**
     * Calcula el rumbo (bearing) entre dos puntos para orientación del marcador
     */
    private float bearingBetweenPoints(LatLng start, LatLng end) {
        double startLat = Math.toRadians(start.latitude);
        double startLng = Math.toRadians(start.longitude);
        double endLat = Math.toRadians(end.latitude);
        double endLng = Math.toRadians(end.longitude);
        
        double dLng = endLng - startLng;
        
        double y = Math.sin(dLng) * Math.cos(endLat);
        double x = Math.cos(startLat) * Math.sin(endLat) -
                   Math.sin(startLat) * Math.cos(endLat) * Math.cos(dLng);
        
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (float) ((bearing + 360) % 360);
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
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION
        );
    }
    
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Enable my location layer if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            
            // Get initial location
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));
                }
            });
        } else {
            requestLocationPermission();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    }
                }
                
                if (isTracking) {
                    startLocationUpdates();
                }
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission is required for tracking", Toast.LENGTH_SHORT).show();
                isTracking = false;
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        if (isTracking) {
            startLocationUpdates();
            timerHandler.postDelayed(timerRunnable, 0);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        if (isTracking) {
            // Just stop location updates and timer, but keep tracking state
            stopLocationUpdates();
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
} 