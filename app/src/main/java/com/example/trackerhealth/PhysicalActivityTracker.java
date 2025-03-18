package com.example.trackerhealth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
    
    private PhysicalActivityDAO activityDAO;
    
    // Constantes para SharedPreferences
    private static final String PREF_NAME = "TrackerHealthPrefs";
    private static final String KEY_USER_ID = "user_id";
    
    // Constantes para permisos y códigos de solicitud
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_STORAGE_PERMISSION = 102;
    
    // Variables para manejo de imágenes
    private String currentPhotoPath;
    private Uri photoUri;

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

        // Configurar bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_activity);

        // Configurar spinner de actividades
        String[] activityTypes = {"Running", "Walking", "Cycling", "Swimming", "Gym workout", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, activityTypes);
        activityTypeSpinner.setAdapter(adapter);

        // Configurar checkbox de GPS
        if (useGpsCheckbox != null && gpsContainer != null) {
            useGpsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                gpsContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });
        }

        // Configurar botón de tracking
        if (startTrackingButton != null) {
            startTrackingButton.setOnClickListener(v -> {
                if (startTrackingButton.getText().equals(getString(R.string.start_tracking))) {
                    startTrackingButton.setText(R.string.stop_tracking);
                    Toast.makeText(this, "GPS tracking started", Toast.LENGTH_SHORT).show();
                } else {
                    startTrackingButton.setText(R.string.start_tracking);
                    Toast.makeText(this, "GPS tracking stopped", Toast.LENGTH_SHORT).show();
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
            
            // Estimación simple de calorías quemadas (esto debería ser más sofisticado en una app real)
            calories = estimateCaloriesBurned(activityType, duration, distance);
            
            // Añadir path de imagen si existe
            if (photoUri != null) {
                notes = "imagePath:" + (currentPhotoPath != null ? currentPhotoPath : photoUri.toString());
            }
            
            // Crear el objeto de actividad
            PhysicalActivity activity = new PhysicalActivity(userId, activityType, duration, calories, distance, notes);
            
            // Guardar en la base de datos
            long activityId = activityDAO.insertActivity(activity);
            
            if (activityId > 0) {
                if (photoUri != null) {
                    Toast.makeText(this, "Actividad guardada con foto", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Actividad guardada exitosamente", Toast.LENGTH_SHORT).show();
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
        
        if (distanceEditText != null) {
            distanceEditText.setText("");
        }
        
        // Volver al primer elemento del spinner
        activityTypeSpinner.setSelection(0);
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