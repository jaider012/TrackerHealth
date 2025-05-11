package com.example.trackerhealth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackerhealth.adapters.ActivityAdapter;
import com.example.trackerhealth.dao.PhysicalActivityDAO;
import com.example.trackerhealth.database.DatabaseHelper;
import com.example.trackerhealth.model.PhysicalActivity;
import com.example.trackerhealth.util.LocationUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;

public class PhysicalActivityTracker extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private Spinner activityTypeSpinner;
    private EditText durationEditText;
    private EditText distanceEditText;
    private CheckBox useGpsCheckbox;
    private LinearLayout gpsContainer;
    private Button startTrackingButton;
    private Button saveActivityButton;
    private Button takePhotoButton;
    private ImageView activityPhotoPreview;
    private TextView locationStatusTextView;
    private TextView currentSpeedTextView;
    private TextView totalDistanceTextView;
    private TextView noRecentActivitiesText;
    private RecyclerView recentActivitiesRecyclerView;
    
    private PhysicalActivityDAO activityDAO;
    
    // Constantes para SharedPreferences
    private static final String PREF_NAME = "TrackerHealthPrefs";
    private static final String KEY_USER_ID = "user_id";
    
    // Constantes para permisos y códigos de solicitud
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_STORAGE_PERMISSION = 102;
    private static final int REQUEST_LOCATION_PERMISSION = 103;
    
    // Variables para manejo de imágenes
    private String currentPhotoPath;
    private Uri photoUri;
    
    // Variables para ubicación
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean isTrackingLocation = false;
    private Location lastLocation;
    private float totalDistance = 0;
    private double currentLatitude;
    private double currentLongitude;
    private long startTimeMillis;
    private List<Location> locationHistory = new ArrayList<>(); // Lista para almacenar el historial de ubicaciones

    // Constantes para la precisión del GPS
    private static final float MIN_ACCURACY_THRESHOLD = 20.0f; // metros
    private static final float MIN_DISTANCE_BETWEEN_UPDATES = 5.0f; // metros
    private static final long GPS_TIMEOUT = 30000; // 30 segundos para timeout del GPS
    
    // Variables adicionales para GPS
    private Handler gpsTimeoutHandler = new Handler(Looper.getMainLooper());
    private boolean hasGpsFix = false;
    private long lastGpsUpdateTime = 0;
    private int consecutiveInvalidLocations = 0;
    private final int MAX_INVALID_LOCATIONS = 3;

    // ID de usuario actual (en un app real se tomaría del login)
    private long currentUserId = 1;

    private ActivityAdapter activityAdapter;
    private List<PhysicalActivity> activityList;

    private PhysicalActivity currentEditingActivity = null;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_tracker);
        
        // Verificar y recrear la tabla de actividades físicas si es necesario
        DatabaseHelper.getInstance(this).verifyPhysicalActivitiesTable();
        
        // Verificar que existe un usuario por defecto
        checkAndCreateDefaultUser();
        
        try {
        // Inicializar lista de actividades (antes de usarla)
        activityList = new ArrayList<>();

        // Verificar y reparar base de datos
        if (!com.example.trackerhealth.util.DatabaseUtils.verifyAllTables(this)) {
            Log.w("PhysicalActivityTracker", "Database verification failed, attempting repair");
            
            // Intentar recrear la tabla de actividades
            if (com.example.trackerhealth.util.DatabaseUtils.recreatePhysicalActivitiesTable(this)) {
                Log.d("PhysicalActivityTracker", "Physical activities table recreated successfully");
            } else {
                Log.e("PhysicalActivityTracker", "Failed to recreate physical activities table");
            }
        }
        
        // Asegurar que existe el usuario
        if (!com.example.trackerhealth.util.DatabaseUtils.ensureUserExists(this, 1)) {
            Log.e("PhysicalActivityTracker", "Failed to ensure user exists");
            Toast.makeText(this, "Error: Could not create default user", Toast.LENGTH_LONG).show();
        }
        
        // Intentar crear una actividad de prueba para verificar si funciona
        long testActivityId = com.example.trackerhealth.util.DatabaseUtils.createTestActivity(this);
        if (testActivityId > 0) {
            Log.d("PhysicalActivityTracker", "Test activity created successfully with ID: " + testActivityId);
        } else {
            Log.e("PhysicalActivityTracker", "Failed to create test activity");
        }
        
        // Inicializar DAO y componentes regulares
        activityDAO = new PhysicalActivityDAO(this);
        
        // Inicializar componentes UI
        initializeViews();

        // Inicializar el cliente de ubicación fusionada
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();
        
        // Configurar listeners y recyclerView
        setupListeners();
        setupRecyclerView();
        
        // Cargar actividades recientes
        loadRecentActivities();
        
        Log.d("PhysicalActivityTracker", "Initializing successful");
        } catch (Exception e) {
            Log.e("PhysicalActivityTracker", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            
            // Manejar la excepción según corresponda
            if (e instanceof Resources.NotFoundException) {
                // Error de recursos
                Toast.makeText(this, "Resource not found", Toast.LENGTH_SHORT).show();
            } else if (e instanceof NullPointerException) {
                // Referencia nula
                Toast.makeText(this, "Null reference error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Verifica si existe el usuario por defecto y lo crea si es necesario
     */
    private void checkAndCreateDefaultUser() {
        try {
            SQLiteDatabase db = DatabaseHelper.getInstance(this).getReadableDatabase();
            String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS + 
                    " WHERE " + DatabaseHelper.KEY_USER_ID + " = ?";
            
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId)});
            
            boolean userExists = false;
            if (cursor != null && cursor.moveToFirst()) {
                userExists = cursor.getInt(0) > 0;
                cursor.close();
            }
            
            if (!userExists) {
                Log.d("PhysicalActivityTracker", "Creating default user");
                
                db = DatabaseHelper.getInstance(this).getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.KEY_USER_ID, currentUserId);
                values.put(DatabaseHelper.KEY_USER_NAME, "Default User");
                values.put(DatabaseHelper.KEY_USER_EMAIL, "user@example.com");
                values.put(DatabaseHelper.KEY_USER_PASSWORD, "password");
                values.put(DatabaseHelper.KEY_USER_HEIGHT, 170);
                values.put(DatabaseHelper.KEY_USER_WEIGHT, 70);
                values.put(DatabaseHelper.KEY_USER_AGE, 30);
                values.put(DatabaseHelper.KEY_USER_GENDER, "Other");
                
                long userId = db.insert(DatabaseHelper.TABLE_USERS, null, values);
                
                if (userId > 0) {
                    Log.d("PhysicalActivityTracker", "Default user created successfully");
                } else {
                    Log.e("PhysicalActivityTracker", "Failed to create default user");
                }
            } else {
                Log.d("PhysicalActivityTracker", "Default user already exists");
            }
        } catch (Exception e) {
            Log.e("PhysicalActivityTracker", "Error checking/creating default user: " + e.getMessage(), e);
        }
    }

    /**
     * Inicializa todas las vistas de manera segura
     */
    private void initializeViews() {
        try {
            // Inicializar vistas principales
            activityTypeSpinner = findViewById(R.id.activity_type_spinner);
            durationEditText = findViewById(R.id.duration_input);
            distanceEditText = findViewById(R.id.distance_input);
            useGpsCheckbox = findViewById(R.id.use_gps_checkbox);
            saveActivityButton = findViewById(R.id.save_activity_button);
            
            // Inicializar RecyclerView y sus componentes
            recentActivitiesRecyclerView = findViewById(R.id.recent_activities_recycler_view);
            noRecentActivitiesText = findViewById(R.id.no_recent_activities_text);
            
            // Configurar RecyclerView
            activityList = new ArrayList<>();
            activityAdapter = new ActivityAdapter(this, activityList, new ActivityAdapter.OnActivityActionListener() {
                @Override
                public void onActivityClick(PhysicalActivity activity) {
                    // Implementar acción al hacer clic
                }

                @Override
                public void onEditActivity(PhysicalActivity activity) {
                    startEditingActivity(activity);
                }

                @Override
                public void onDeleteActivity(PhysicalActivity activity) {
                    showDeleteConfirmationDialog(activity);
                }
            });
            recentActivitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            recentActivitiesRecyclerView.setAdapter(activityAdapter);
            
            // Cargar actividades recientes
            loadRecentActivities();
            
            // Configurar Spinner de tipos de actividad
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.activity_types, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            activityTypeSpinner.setAdapter(adapter);
            
        } catch (Exception e) {
            Log.e("PhysicalActivityTracker", "Error initializing views: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Configura los listeners para los componentes interactivos de manera segura
     */
    private void setupListeners() {
        try {
            // Configurar bottom navigation
            if (bottomNavigationView != null) {
                bottomNavigationView.setOnNavigationItemSelectedListener(this);
                bottomNavigationView.setSelectedItemId(R.id.navigation_activity);
            }
        
        // Configurar checkbox de GPS
        if (useGpsCheckbox != null && gpsContainer != null) {
            useGpsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                gpsContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (isChecked && !checkLocationPermission()) {
                    requestLocationPermission();
                }
            });
        }

        // Configurar botón de tracking
        if (startTrackingButton != null) {
            startTrackingButton.setOnClickListener(v -> {
                if (!isTrackingLocation) {
                    if (checkLocationPermission()) {
                        startLocationTracking();
                    } else {
                        requestLocationPermission();
                    }
                } else {
                    stopLocationTracking();
                }
            });
        }
        
        // Configurar botón de foto
        if (takePhotoButton != null) {
                takePhotoButton.setOnClickListener(v -> showImageSourceDialog());
        }

        // Configurar botón de guardar
            if (saveActivityButton != null) {
        saveActivityButton.setOnClickListener(v -> {
                    if (isEditing) {
                        updateActivity();
                    } else {
            saveActivityToDatabase();
                    }
                });
            }
            
            Log.d("PhysicalActivityTracker", "Listeners setup successfully");
        } catch (Exception e) {
            Log.e("PhysicalActivityTracker", "Error setting up listeners: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up interactive components", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Configura los parámetros de solicitud de ubicación
     */
    private void createLocationRequest() {
        // Ajustar la frecuencia de actualización según el tipo de actividad
        int updateInterval = useGpsCheckbox.isChecked() ? 3000 : 10000; // 3 segundos o 10 segundos
        
        locationRequest = new LocationRequest.Builder(updateInterval)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(1000) // Mínimo 1 segundo entre actualizaciones
            .setMaxUpdateDelayMillis(updateInterval * 2) // Máximo retraso
            .setMinUpdateDistanceMeters(MIN_DISTANCE_BETWEEN_UPDATES) // Solo actualizar si nos movemos al menos 5 metros
            .build();
    }
    
    /**
     * Configura el callback para recibir actualizaciones de ubicación
     */
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                
                if (locationResult.getLastLocation() != null) {
                    Location currentLocation = locationResult.getLastLocation();
                    
                    // Resetear el timeout del GPS ya que recibimos una ubicación
                    gpsTimeoutHandler.removeCallbacksAndMessages(null);
                    lastGpsUpdateTime = SystemClock.elapsedRealtime();
                    
                    // Verificar si la ubicación es válida
                    if (isValidLocation(currentLocation)) {
                        // Restablecer contador de ubicaciones inválidas
                        consecutiveInvalidLocations = 0;
                        
                        // Si aún no teníamos señal GPS, notificar que ahora la tenemos
                        if (!hasGpsFix) {
                            hasGpsFix = true;
                            Toast.makeText(PhysicalActivityTracker.this, "GPS signal acquired", Toast.LENGTH_SHORT).show();
                        }
                        
                        // Actualizar latitud y longitud actuales
                        currentLatitude = currentLocation.getLatitude();
                        currentLongitude = currentLocation.getLongitude();
                        
                        // Guardar ubicación en el historial
                        locationHistory.add(currentLocation);
                        
                        // Actualizar interfaz de usuario con datos de ubicación
                        updateLocationUI(currentLocation);
                        
                        // Calcular distancia si hay una ubicación anterior
                        if (lastLocation != null) {
                            // Usar el método Haversine para mayor precisión en largas distancias
                            float distance = LocationUtils.calculateDistance(
                                    lastLocation.getLatitude(), lastLocation.getLongitude(),
                                    currentLocation.getLatitude(), currentLocation.getLongitude());
                            
                            // Solo añadir la distancia si es razonable (evitar saltos del GPS)
                            if (distance > 0 && distance < 100) { // Ignorar saltos mayores a 100m
                                totalDistance += distance / 1000; // Convertir a kilómetros
                            }
                        }
                        
                        // Guardar ubicación actual como última ubicación
                        lastLocation = currentLocation;
                    } else {
                        // Incrementar contador de ubicaciones inválidas
                        consecutiveInvalidLocations++;
                        
                        // Si tenemos demasiadas ubicaciones inválidas seguidas, notificar al usuario
                        if (consecutiveInvalidLocations >= MAX_INVALID_LOCATIONS) {
                            hasGpsFix = false;
                            Toast.makeText(PhysicalActivityTracker.this, 
                                    "GPS signal lost. Moving to more open area may help", 
                                    Toast.LENGTH_SHORT).show();
                            consecutiveInvalidLocations = 0; // Resetear contador para no spammear
                        }
                    }
                    
                    // Configurar un timeout para detectar pérdida prolongada de señal GPS
                    gpsTimeoutHandler.postDelayed(() -> {
                        // Si pasó el tiempo de timeout sin actualizaciones válidas
                        if (SystemClock.elapsedRealtime() - lastGpsUpdateTime > GPS_TIMEOUT) {
                            hasGpsFix = false;
                            Toast.makeText(PhysicalActivityTracker.this, 
                                    "GPS signal lost for too long. Check device settings.", 
                                    Toast.LENGTH_LONG).show();
                        }
                    }, GPS_TIMEOUT);
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
            Log.d("GPS", "Location discarded due to poor accuracy: " + location.getAccuracy() + "m");
            return false;
        }
        
        // Verificar velocidad de movimiento (si la ubicación tiene velocidad)
        if (location.hasSpeed() && location.getSpeed() > 30) { // Más de 30 m/s (108 km/h)
            Log.d("GPS", "Location discarded due to unrealistic speed: " + location.getSpeed() + "m/s");
            return false;
        }
        
        // Verificar que la ubicación no sea muy antigua
        long locationTime = location.getTime();
        long currentTime = System.currentTimeMillis();
        if (currentTime - locationTime > 60000) { // Más de 1 minuto de antigüedad
            Log.d("GPS", "Location discarded due to age: " + (currentTime - locationTime) + "ms old");
            return false;
        }
        
        return true;
    }
    
    /**
     * Actualiza la UI con la información de ubicación
     */
    private void updateLocationUI(Location location) {
        if (location != null && locationStatusTextView != null) {
            // Mostrar coordenadas
            String locationText = String.format(Locale.getDefault(), 
                    "Lat: %.6f, Lng: %.6f", 
                    location.getLatitude(), 
                    location.getLongitude());
            locationStatusTextView.setText(locationText);
            
            // Mostrar velocidad actual
            if (currentSpeedTextView != null) {
                float speedKmh = location.hasSpeed() ? 
                        location.getSpeed() * 3.6f : 0; // m/s a km/h
                String speedText = String.format(Locale.getDefault(), 
                        "%.1f km/h", speedKmh);
                currentSpeedTextView.setText(speedText);
            }
            
            // Mostrar distancia total
            if (totalDistanceTextView != null) {
                String distanceText = String.format(Locale.getDefault(), 
                        "%.2f km", totalDistance);
                totalDistanceTextView.setText(distanceText);
                
                // Actualizar también el campo de distancia
                if (distanceEditText != null) {
                    distanceEditText.setText(String.format(Locale.getDefault(), "%.2f", totalDistance));
                }
            }
            
            // Calcular duración si estamos rastreando
            if (isTrackingLocation && durationEditText != null) {
                long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
                int minutes = (int) (elapsedMillis / 60000); // Convertir a minutos
                durationEditText.setText(String.valueOf(minutes));
            }
        }
    }
    
    /**
     * Inicia el seguimiento de ubicación
     */
    private void startLocationTracking() {
        if (checkLocationPermission()) {
            // Redefinir los parámetros de ubicación según el estado actual
            createLocationRequest();
            
            // Iniciar actualizaciones de ubicación
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
            
            // Actualizar estado y UI
            isTrackingLocation = true;
            startTimeMillis = System.currentTimeMillis();
            totalDistance = 0;
            lastLocation = null;
            hasGpsFix = false;
            consecutiveInvalidLocations = 0;
            
            // Limpiar historial de ubicaciones anterior
            locationHistory.clear();
            
            // Cambiar texto del botón
            startTrackingButton.setText(R.string.stop_tracking);
            Toast.makeText(this, "GPS tracking started", Toast.LENGTH_SHORT).show();
            
            // Configurar un timeout para adquisición inicial de GPS
            gpsTimeoutHandler.postDelayed(() -> {
                if (!hasGpsFix && isTrackingLocation) {
                    Toast.makeText(PhysicalActivityTracker.this, 
                            "Having trouble getting GPS signal. Make sure you're outside or near a window.", 
                            Toast.LENGTH_LONG).show();
                }
            }, 15000); // 15 segundos para adquirir señal GPS inicial
        } else {
            requestLocationPermission();
        }
    }
    
    /**
     * Detiene el seguimiento de ubicación
     */
    private void stopLocationTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        
        // Actualizar estado y UI
        isTrackingLocation = false;
        startTrackingButton.setText(R.string.start_tracking);
        Toast.makeText(this, "GPS tracking stopped", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Verifica si tenemos el permiso de ubicación
     */
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Solicita el permiso de ubicación
     */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, 
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 
                REQUEST_LOCATION_PERMISSION);
    }

    /**
     * Muestra un diálogo para elegir la fuente de la imagen (cámara o galería)
     */
    private void showImageSourceDialog() {
        final CharSequence[] options = {"Tomar foto", "Elegir de la galería", "Cancelar"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Añadir foto");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Tomar foto")) {
                if (checkCameraPermission()) {
                    dispatchTakePictureIntent();
                } else {
                    requestCameraPermission();
                }
            } else if (options[item].equals("Elegir de la galería")) {
                if (checkStoragePermission()) {
                    pickImageFromGallery();
                } else {
                    requestStoragePermission();
                }
            } else if (options[item].equals("Cancelar")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    
    /**
     * Inicia la cámara para tomar una foto
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Crear el archivo donde se guardará la foto
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error al crear el archivo de imagen", Toast.LENGTH_SHORT).show();
            }
            
            // Si el archivo se creó correctamente, proceder con la captura
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        "com.example.trackerhealth.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    
    /**
     * Inicia la galería para seleccionar una imagen
     */
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }
    
    /**
     * Crea un archivo temporal para guardar la imagen
     */
    private File createImageFile() throws IOException {
        // Crear un nombre de archivo único
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "ACTIVITY_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefijo
                ".jpg",         // sufijo
                storageDir      // directorio
        );
        
        // Guardar la ruta del archivo para su uso posterior
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    /**
     * Resetea la vista previa de la imagen y las variables asociadas
     */
    private void resetPhotoPreview() {
        if (activityPhotoPreview != null) {
            activityPhotoPreview.setImageResource(android.R.drawable.ic_menu_camera);
            activityPhotoPreview.setScaleType(ImageView.ScaleType.CENTER);
        }
        photoUri = null;
        currentPhotoPath = null;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // La foto se ha guardado en photoUri, mostrar vista previa
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    activityPhotoPreview.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                // Imagen seleccionada de la galería
                photoUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    activityPhotoPreview.setImageBitmap(bitmap);
                    // La imagen de la galería no tiene currentPhotoPath, solo photoUri
                    currentPhotoPath = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    /**
     * Verificación y solicitud de permisos
     */
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, 
                new String[]{android.Manifest.permission.CAMERA}, 
                REQUEST_CAMERA_PERMISSION);
    }
    
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, 
                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 
                REQUEST_STORAGE_PERMISSION);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Se requiere permiso de cámara para tomar fotos", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(this, "Se requiere permiso de almacenamiento para acceder a la galería", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si se concede el permiso, iniciar el seguimiento
                if (isTrackingLocation) {
                    startLocationTracking();
                }
            } else {
                Toast.makeText(this, "Se requiere permiso de ubicación para rastrear tu actividad", Toast.LENGTH_SHORT).show();
                useGpsCheckbox.setChecked(false);
            }
        }
    }
    
    /**
     * Guarda una nueva actividad física en la base de datos con fallback
     */
    private void saveActivityToDatabase() {
        try {
            // 1. Validar campos obligatorios
            if (TextUtils.isEmpty(durationEditText.getText())) {
                durationEditText.setError("Duration is required");
                durationEditText.requestFocus();
                Log.e("PhysicalActivityTracker", "Validation error: Duration is required");
                return;
            }
            
            Log.d("PhysicalActivityTracker", "Starting to save activity to database");

            // 2. Obtener valores de los campos
            String activityType = activityTypeSpinner.getSelectedItem().toString();
            int duration = Integer.parseInt(durationEditText.getText().toString().trim());
            double distance = 0;

            Log.d("PhysicalActivityTracker", "Initial values: type=" + activityType + ", duration=" + duration);

            // 3. Obtener distancia si está disponible
            if (!TextUtils.isEmpty(distanceEditText.getText())) {
                distance = Double.parseDouble(distanceEditText.getText().toString().trim());
                Log.d("PhysicalActivityTracker", "Distance from input: " + distance);
            } else if (useGpsCheckbox.isChecked() && totalDistance > 0) {
                distance = totalDistance;
                Log.d("PhysicalActivityTracker", "Distance from GPS: " + distance);
            }

            // 4. Calcular calorías quemadas
            int calories = estimateCaloriesBurned(activityType, duration, distance);
            Log.d("PhysicalActivityTracker", "Calories calculated: " + calories);

            // 5. Construir notas y metadata
            StringBuilder notesBuilder = new StringBuilder();
            
            // Añadir información de GPS si está disponible
            if (useGpsCheckbox.isChecked() && currentLatitude != 0 && currentLongitude != 0) {
                notesBuilder.append("gpsTracking:true");
                notesBuilder.append(",totalDistance:").append(String.format(Locale.US, "%.2f", totalDistance));
                Log.d("PhysicalActivityTracker", "GPS info added to notes");
            }

            // Añadir información de foto si existe
            if (photoUri != null) {
                if (notesBuilder.length() > 0) {
                    notesBuilder.append(",");
                }
                notesBuilder.append("photoPath:").append(photoUri.toString());
                Log.d("PhysicalActivityTracker", "Photo info added to notes: " + photoUri);
            }

            String notes = notesBuilder.toString();
            Log.d("PhysicalActivityTracker", "Final notes: " + notes);

            // 6. Crear objeto de actividad
            PhysicalActivity activity = new PhysicalActivity(
                currentUserId,
                activityType,
                duration,
                calories,
                distance,
                notes
            );

            // 7. Establecer fecha actual
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDate = sdf.format(new Date());
            activity.setDate(currentDate);
            Log.d("PhysicalActivityTracker", "Activity date set: " + currentDate);

            // 8. Establecer coordenadas GPS si están disponibles
            if (useGpsCheckbox.isChecked() && currentLatitude != 0 && currentLongitude != 0) {
                activity.setLatitude(currentLatitude);
                activity.setLongitude(currentLongitude);
                Log.d("PhysicalActivityTracker", "GPS coordinates set: " + currentLatitude + "," + currentLongitude);
            }
            
            // 9. Guardar en la base de datos
            Log.d("PhysicalActivityTracker", "About to insert activity into database using DAO");
            long activityId = activityDAO.insertActivity(activity);
            Log.d("PhysicalActivityTracker", "DAO insert result: " + activityId);

            // 10. Si falla, intentar guardar directamente con SQLite
            if (activityId <= 0) {
                Log.w("PhysicalActivityTracker", "DAO insert failed, trying direct SQLite insert");
                
                // Crear ContentValues
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.KEY_ACTIVITY_USER_ID_FK, activity.getUserId());
                values.put(DatabaseHelper.KEY_ACTIVITY_TYPE, activity.getActivityType());
                values.put(DatabaseHelper.KEY_ACTIVITY_DURATION, activity.getDuration());
                values.put(DatabaseHelper.KEY_ACTIVITY_CALORIES, activity.getCaloriesBurned());
                values.put(DatabaseHelper.KEY_ACTIVITY_DISTANCE, activity.getDistance());
                values.put(DatabaseHelper.KEY_ACTIVITY_DATE, activity.getDate());
                values.put(DatabaseHelper.KEY_ACTIVITY_NOTES, activity.getNotes());
                values.put(DatabaseHelper.KEY_ACTIVITY_LATITUDE, activity.getLatitude());
                values.put(DatabaseHelper.KEY_ACTIVITY_LONGITUDE, activity.getLongitude());
                
                // Intentar insertar directamente
                SQLiteDatabase db = DatabaseHelper.getInstance(this).getWritableDatabase();
                db.beginTransaction();
                try {
                    activityId = db.insert(DatabaseHelper.TABLE_PHYSICAL_ACTIVITIES, null, values);
            if (activityId > 0) {
                        db.setTransactionSuccessful();
                        Log.d("PhysicalActivityTracker", "Direct SQLite insert succeeded with ID: " + activityId);
                    } else {
                        Log.e("PhysicalActivityTracker", "Direct SQLite insert also failed");
                    }
                } catch (Exception e) {
                    Log.e("PhysicalActivityTracker", "Error in direct SQLite insert: " + e.getMessage(), e);
                } finally {
                    db.endTransaction();
                }
            }

            if (activityId > 0) {
                // 11. Procesar datos adicionales
                String successMessage = "Activity saved successfully (ID: " + activityId + ")";

                // Guardar ruta GPS si está disponible
                if (useGpsCheckbox.isChecked() && !locationHistory.isEmpty()) {
                    Log.d("PhysicalActivityTracker", "Saving route data with " + locationHistory.size() + " points");
                    String routeFilePath = LocationUtils.saveRouteData(this, activityId, locationHistory);
                    if (routeFilePath != null) {
                        successMessage += " with GPS route";
                        Log.d("PhysicalActivityTracker", "Route saved to: " + routeFilePath);
                    }
                }
                
                // Confirmar si se guardó con foto
                if (photoUri != null) {
                    successMessage += " with photo";
                }
                
                // 12. Mostrar mensaje de éxito
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
                Log.i("PhysicalActivityTracker", successMessage);
                
                // 13. Limpiar y resetear
                if (isTrackingLocation) {
                    stopLocationTracking();
                }
                clearInputFields();
                resetPhotoPreview();
                
                // 14. Recargar lista de actividades
                loadRecentActivities();
            } else {
                String errorMsg = "Failed to save activity after multiple attempts";
                Log.e("PhysicalActivityTracker", errorMsg);
                
                // Mensaje más descriptivo para el usuario
                new AlertDialog.Builder(this)
                    .setTitle("Database Error")
                    .setMessage("Failed to save activity. Please check database permissions and storage space.")
                    .setPositiveButton("OK", null)
                    .show();
            }
            
        } catch (NumberFormatException e) {
            String errorMsg = "Please enter valid numbers";
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            Log.e("PhysicalActivityTracker", "Error parsing numbers: " + e.getMessage(), e);
        } catch (Exception e) {
            String errorMsg = "Error: " + e.getMessage();
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            Log.e("PhysicalActivityTracker", "Error saving activity: " + e.getMessage(), e);
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
     * Limpia todos los campos del formulario y resetea el estado
     */
    private void clearInputFields() {
        durationEditText.setText("");
        distanceEditText.setText("");
        activityTypeSpinner.setSelection(0);
        useGpsCheckbox.setChecked(false);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Detener actualizaciones de ubicación si la actividad está en pausa
        if (isTrackingLocation) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reanudar actualizaciones de ubicación si el seguimiento estaba activo
        if (isTrackingLocation && checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        }
        // Recargar actividades cuando se vuelve a la actividad
        loadRecentActivities();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // Si ya estamos en la actividad actual, no hacer nada
        if (itemId == R.id.navigation_activity) {
            return true;
        }

        // Preparar la intent para la nueva actividad
        Intent intent = null;
        
        if (itemId == R.id.navigation_dashboard) {
            intent = new Intent(this, DashboardActivity.class);
        } else if (itemId == R.id.navigation_food) {
            intent = new Intent(this, FoodTrackerActivity.class);
        } else if (itemId == R.id.navigation_reports) {
            intent = new Intent(this, ReportsActivity.class);
        }
        
        // Si tenemos una intent válida, iniciar la actividad
        if (intent != null) {
            // Detener el tracking de GPS si está activo
            if (isTrackingLocation) {
                stopLocationTracking();
            }
            
            // Limpiar recursos
            if (gpsTimeoutHandler != null) {
                gpsTimeoutHandler.removeCallbacksAndMessages(null);
            }
            
            // Añadir flags para evitar comportamientos inesperados
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            
            // No llamar a finish() para mantener la actividad en el back stack
            return true;
        }
        
        return false;
    }

    
    /**
     * Carga las actividades recientes del usuario
     */
    private void loadRecentActivities() {
        try {
            Log.d("PhysicalActivityTracker", "Loading recent activities for user: " + currentUserId);
            
        // Limpiar lista anterior
        activityList.clear();
        
            // Verificar que el RecyclerView y el adapter estén inicializados
            if (recentActivitiesRecyclerView == null || activityAdapter == null) {
                Log.e("PhysicalActivityTracker", "RecyclerView or adapter is null");
                return;
            }
            
            // Obtener actividades del usuario actual
            List<PhysicalActivity> recentActivities = activityDAO.getRecentActivities(currentUserId, 10);
            
            Log.d("PhysicalActivityTracker", "Found " + recentActivities.size() + " recent activities");
            
            if (recentActivities.isEmpty()) {
                if (noRecentActivitiesText != null) {
                    noRecentActivitiesText.setVisibility(View.VISIBLE);
                    Log.d("PhysicalActivityTracker", "No activities found, showing empty message");
                }
                if (recentActivitiesRecyclerView != null) {
                    recentActivitiesRecyclerView.setVisibility(View.GONE);
                }
            } else {
                // Registrar detalles de las actividades
                for (PhysicalActivity activity : recentActivities) {
                    Log.d("PhysicalActivityTracker", "Activity: ID=" + activity.getId() + 
                            ", Type=" + activity.getActivityType() + 
                            ", Date=" + activity.getDate());
                }
                
                activityList.addAll(recentActivities);
                
                if (noRecentActivitiesText != null) {
                    noRecentActivitiesText.setVisibility(View.GONE);
                }
                if (recentActivitiesRecyclerView != null) {
                    recentActivitiesRecyclerView.setVisibility(View.VISIBLE);
                    
                    // Notificar cambios en el adaptador
                    if (activityAdapter != null) {
                        activityAdapter.notifyDataSetChanged();
                        Log.d("PhysicalActivityTracker", "Adapter notified of data changes");
                    } else {
                        Log.e("PhysicalActivityTracker", "Activity adapter is null");
                    }
                }
            }
        } catch (Exception e) {
            Log.e("PhysicalActivityTracker", "Error loading activities: " + e.getMessage(), e);
            
            if (noRecentActivitiesText != null) {
                noRecentActivitiesText.setText("Error: " + e.getMessage());
                noRecentActivitiesText.setVisibility(View.VISIBLE);
            }
            if (recentActivitiesRecyclerView != null) {
                recentActivitiesRecyclerView.setVisibility(View.GONE);
            }
            
            // Mostrar diálogo con detalles del error
            new AlertDialog.Builder(this)
                .setTitle("Database Error")
                .setMessage("Error loading activities: " + e.getMessage() + 
                        "\n\nPlease check the logs for details")
                .setPositiveButton("OK", null)
                .show();
        }
    }

    /**
     * Configura el RecyclerView de manera segura
     */
    private void setupRecyclerView() {
        try {
            if (recentActivitiesRecyclerView == null) {
                Log.e("PhysicalActivityTracker", "recentActivitiesRecyclerView is null");
                return;
            }
            
            recentActivitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            
            activityAdapter = new ActivityAdapter(this, activityList, new ActivityAdapter.OnActivityActionListener() {
                @Override
                public void onActivityClick(PhysicalActivity activity) {
                    // Mostrar detalles si se desea
                    if (activity != null) {
                        Toast.makeText(PhysicalActivityTracker.this, 
                                "Activity: " + activity.getActivityType(), 
                                Toast.LENGTH_SHORT).show();
                    }
                }
    
                @Override
                public void onEditActivity(PhysicalActivity activity) {
                    if (activity != null) {
                        startEditingActivity(activity);
                    }
                }
    
                @Override
                public void onDeleteActivity(PhysicalActivity activity) {
                    if (activity != null) {
                        showDeleteConfirmationDialog(activity);
                    }
                }
            });
            
            recentActivitiesRecyclerView.setAdapter(activityAdapter);
            Log.d("PhysicalActivityTracker", "RecyclerView setup successfully");
        } catch (Exception e) {
            Log.e("PhysicalActivityTracker", "Error setting up RecyclerView: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up activity list", Toast.LENGTH_SHORT).show();
        }
    }

    private void startEditingActivity(PhysicalActivity activity) {
        currentEditingActivity = activity;
        isEditing = true;
        
        // Actualizar UI con los datos de la actividad
        activityTypeSpinner.setSelection(getActivityTypePosition(activity.getActivityType()));
        durationEditText.setText(String.valueOf(activity.getDuration()));
        distanceEditText.setText(activity.getDistance() > 0 ? String.format(Locale.getDefault(), "%.2f", activity.getDistance()) : "");
        
        // Actualizar el botón de guardar
        saveActivityButton.setText(R.string.update_activity);
        
        // Desactivar GPS tracking durante la edición
        useGpsCheckbox.setChecked(false);
        gpsContainer.setVisibility(View.GONE);
        
        // Scroll hacia arriba para mostrar el formulario
        ScrollView scrollView = findViewById(R.id.activity_scroll_view);
        if (scrollView != null) {
            scrollView.smoothScrollTo(0, 0);
        }
    }

    private void updateActivity() {
        if (currentEditingActivity == null) return;
        
        try {
            // Validar campos
            if (TextUtils.isEmpty(durationEditText.getText())) {
                durationEditText.setError(getString(R.string.error_field_required));
                durationEditText.requestFocus();
                return;
            }
            
            // Actualizar datos de la actividad
            currentEditingActivity.setActivityType(activityTypeSpinner.getSelectedItem().toString());
            currentEditingActivity.setDuration(Integer.parseInt(durationEditText.getText().toString()));
            
            if (!TextUtils.isEmpty(distanceEditText.getText())) {
                currentEditingActivity.setDistance(Double.parseDouble(distanceEditText.getText().toString()));
            }
            
            // Recalcular calorías
            int calories = estimateCaloriesBurned(
                currentEditingActivity.getActivityType(),
                currentEditingActivity.getDuration(),
                currentEditingActivity.getDistance()
            );
            currentEditingActivity.setCaloriesBurned(calories);
            
            // Actualizar en la base de datos
            if (activityDAO.updateActivity(currentEditingActivity)) {
                Toast.makeText(this, "Activity updated successfully", Toast.LENGTH_SHORT).show();
                
                // Recargar la lista
                loadRecentActivities();
                
                // Limpiar el formulario y resetear el estado
                clearInputFields();
                resetEditingState();
            } else {
                Toast.makeText(this, "Error updating activity", Toast.LENGTH_SHORT).show();
            }
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog(PhysicalActivity activity) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Activity")
            .setMessage("Are you sure you want to delete this activity?")
            .setPositiveButton("Delete", (dialog, which) -> {
                if (activityDAO.deleteActivity(activity.getId())) {
                    Toast.makeText(this, "Activity deleted", Toast.LENGTH_SHORT).show();
                    loadRecentActivities();
                } else {
                    Toast.makeText(this, "Error deleting activity", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private int getActivityTypePosition(String activityType) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) activityTypeSpinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(activityType)) {
                return i;
            }
        }
        return 0;
    }

    private void resetEditingState() {
        currentEditingActivity = null;
        isEditing = false;
        saveActivityButton.setText(R.string.save_activity);
    }
}