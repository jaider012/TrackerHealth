package com.example.trackerhealth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import com.example.trackerhealth.dao.PhysicalActivityDAO;
import com.example.trackerhealth.model.PhysicalActivity;
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
import java.util.Date;
import java.util.Locale;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_tracker);

        // Inicializar base de datos
        activityDAO = new PhysicalActivityDAO(this);
        
        // Inicializar componentes
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        activityTypeSpinner = findViewById(R.id.activity_type_spinner);
        durationEditText = findViewById(R.id.duration_input);
        distanceEditText = findViewById(R.id.distance_input);
        useGpsCheckbox = findViewById(R.id.use_gps_checkbox);
        gpsContainer = findViewById(R.id.gps_container);
        startTrackingButton = findViewById(R.id.start_tracking_button);
        saveActivityButton = findViewById(R.id.save_activity_button);
        takePhotoButton = findViewById(R.id.take_photo_button);
        activityPhotoPreview = findViewById(R.id.activity_photo_preview);
        
        // Inicializar componentes de ubicación
        locationStatusTextView = findViewById(R.id.location_status_text);
        currentSpeedTextView = findViewById(R.id.current_speed_text);
        totalDistanceTextView = findViewById(R.id.total_distance_text);

        // Configurar bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_activity);

        // Configurar spinner de actividades
        String[] activityTypes = {"Running", "Walking", "Cycling", "Swimming", "Gym workout", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, activityTypes);
        activityTypeSpinner.setAdapter(adapter);

        // Inicializar el cliente de ubicación fusionada
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Configurar solicitud de ubicación
        createLocationRequest();
        
        // Configurar callback de ubicación
        createLocationCallback();
        
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
                    // Verificar permisos antes de iniciar el seguimiento
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
            takePhotoButton.setOnClickListener(v -> {
                showImageSourceDialog();
            });
        }

        // Configurar botón de guardar
        saveActivityButton.setOnClickListener(v -> {
            saveActivityToDatabase();
        });
    }
    
    /**
     * Configura los parámetros de solicitud de ubicación
     */
    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(5000) // Actualizaciones cada 5 segundos
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(2000) // Mínimo 2 segundos entre actualizaciones
            .setMaxUpdateDelayMillis(10000) // Máximo retraso de 10 segundos
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
                    
                    // Actualizar latitud y longitud actuales
                    currentLatitude = currentLocation.getLatitude();
                    currentLongitude = currentLocation.getLongitude();
                    
                    // Actualizar interfaz de usuario con datos de ubicación
                    updateLocationUI(currentLocation);
                    
                    // Calcular distancia si hay una ubicación anterior
                    if (lastLocation != null) {
                        float distance = lastLocation.distanceTo(currentLocation);
                        totalDistance += distance / 1000; // Convertir a kilómetros
                    }
                    
                    // Guardar ubicación actual como última ubicación
                    lastLocation = currentLocation;
                }
            }
        };
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
            
            // Cambiar texto del botón
            startTrackingButton.setText(R.string.stop_tracking);
            Toast.makeText(this, "GPS tracking started", Toast.LENGTH_SHORT).show();
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
     * Guarda la actividad física en la base de datos
     */
    private void saveActivityToDatabase() {
        // Validar que los campos obligatorios estén completos
        if (TextUtils.isEmpty(durationEditText.getText())) {
            durationEditText.setError(getString(R.string.error_field_required));
            durationEditText.requestFocus();
            return;
        }
        
        try {
            // Obtener el ID del usuario de SharedPreferences
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            long userId = prefs.getLong(KEY_USER_ID, -1);
            
            if (userId == -1) {
                Toast.makeText(this, "Error: No hay un usuario logueado", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Obtener los valores de los campos
            String activityType = activityTypeSpinner.getSelectedItem().toString();
            int duration = Integer.parseInt(durationEditText.getText().toString());
            
            // Valores opcionales
            double distance = 0;
            int calories = 0; // Estimación de calorías basada en la duración y tipo de actividad
            String notes = "";
            
            if (distanceEditText != null && !TextUtils.isEmpty(distanceEditText.getText())) {
                distance = Double.parseDouble(distanceEditText.getText().toString());
            }
            
            // Si usamos GPS, añadir la ubicación a las notas
            if (useGpsCheckbox.isChecked() && currentLatitude != 0 && currentLongitude != 0) {
                notes = String.format(Locale.getDefault(), 
                        "lastLocation:%.6f,%.6f;totalDistance:%.2f", 
                        currentLatitude, currentLongitude, totalDistance);
            }
            
            // Estimación simple de calorías quemadas
            calories = estimateCaloriesBurned(activityType, duration, distance);
            
            // Añadir path de imagen si existe
            if (photoUri != null) {
                if (!TextUtils.isEmpty(notes)) {
                    notes += ";";
                }
                notes += "imagePath:" + (currentPhotoPath != null ? currentPhotoPath : photoUri.toString());
            }
            
            // Crear el objeto de actividad
            PhysicalActivity activity = new PhysicalActivity(userId, activityType, duration, calories, distance, notes);
            
            // Guardar en la base de datos
            long activityId = activityDAO.insertActivity(activity);
            
            if (activityId > 0) {
                String message = "Actividad guardada";
                if (photoUri != null) {
                    message += " con foto";
                }
                if (useGpsCheckbox.isChecked()) {
                    message += " y datos de ubicación";
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                
                // Detener tracking si está activo
                if (isTrackingLocation) {
                    stopLocationTracking();
                }
                
                // Limpiar campos después de guardar
                clearFields();
                resetPhotoPreview();
            } else {
                Toast.makeText(this, "Error al guardar la actividad", Toast.LENGTH_SHORT).show();
            }
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa valores numéricos válidos", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
     * Limpia los campos después de guardar
     */
    private void clearFields() {
        durationEditText.setText("");
        distanceEditText.setText("");
        
        // Resetear variables de ubicación
        totalDistance = 0;
        if (totalDistanceTextView != null) {
            totalDistanceTextView.setText("0.00 km");
        }
        if (currentSpeedTextView != null) {
            currentSpeedTextView.setText("0.0 km/h");
        }
        if (locationStatusTextView != null) {
            locationStatusTextView.setText("Waiting for GPS...");
        }
        
        // Volver al primer elemento del spinner
        activityTypeSpinner.setSelection(0);
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
            return true;
        } else if (itemId == R.id.navigation_food) {
            intent = new Intent(this, FoodTrackerActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.navigation_reports) {
            intent = new Intent(this, ReportsActivity.class);
            startActivity(intent);
            return true;
        }
        
        return false;
    }
}