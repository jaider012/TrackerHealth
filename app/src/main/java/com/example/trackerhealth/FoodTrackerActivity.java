package com.example.trackerhealth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FoodTrackerActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private Spinner mealTypeSpinner;
    private Button takePhotoButton;
    private Button saveMealButton;
    private ImageView foodPhotoPreview;
    
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
        setContentView(R.layout.activity_food_tracker);

        // Inicializar componentes
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        mealTypeSpinner = findViewById(R.id.meal_type_spinner);
        takePhotoButton = findViewById(R.id.take_photo_button);
        saveMealButton = findViewById(R.id.save_meal_button);
        foodPhotoPreview = findViewById(R.id.food_photo_preview);

        // Configurar bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_food);

        // Configurar spinner de tipos de comida
        String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mealTypes);
        mealTypeSpinner.setAdapter(adapter);

        // Configurar botón de foto
        takePhotoButton.setOnClickListener(v -> {
            showImageSourceDialog();
        });

        // Configurar botón de guardar
        saveMealButton.setOnClickListener(v -> {
            // Aquí se guardarían los datos, incluyendo la ruta de la imagen
            if (photoUri != null || currentPhotoPath != null) {
                Toast.makeText(this, "Meal saved with photo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Meal saved without photo", Toast.LENGTH_SHORT).show();
            }
            
            // Reiniciar la vista previa después de guardar
            resetPhotoPreview();
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
        String imageFileName = "FOOD_" + timeStamp + "_";
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
        foodPhotoPreview.setImageResource(android.R.drawable.ic_menu_camera);
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
                    foodPhotoPreview.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                // Imagen seleccionada de la galería
                photoUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    foodPhotoPreview.setImageBitmap(bitmap);
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Mostrar un diálogo explicando por qué se necesita el permiso
            new AlertDialog.Builder(this)
                .setTitle("Permiso necesario")
                .setMessage("Se necesita acceso al almacenamiento para seleccionar fotos de tu galería")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Solicitar el permiso
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
        } else {
            // Solicitar el permiso directamente
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        }
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
            return true;
        } else if (itemId == R.id.navigation_reports) {
            intent = new Intent(this, ReportsActivity.class);
            startActivity(intent);
            return true;
        }
        
        return false;
    }
}