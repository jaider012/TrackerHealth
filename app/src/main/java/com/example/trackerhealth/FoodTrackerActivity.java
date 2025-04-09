package com.example.trackerhealth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FoodTrackerActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private Spinner mealTypeSpinner;
    private Button takePhotoButton;
    private Button saveMealButton;
    private ImageView foodPhotoPreview;
    private EditText foodNameInput;
    private EditText caloriesInput;
    
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
        foodNameInput = findViewById(R.id.food_name_input);
        caloriesInput = findViewById(R.id.calories_input);

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
            saveMealData();
        });
        
        // Cargar comidas guardadas
        loadSavedMeals();
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
                    
                    // Optimizar la imagen capturada
                    String optimizedPath = optimizeAndSaveImage(photoUri);
                    if (optimizedPath != null) {
                        // Actualizar ruta para usar la versión optimizada
                        currentPhotoPath = optimizedPath;
                    }
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
                    
                    // Optimizar la imagen de la galería
                    String optimizedPath = optimizeAndSaveImage(photoUri);
                    if (optimizedPath != null) {
                        // Usar ruta optimizada en lugar de URI
                        currentPhotoPath = optimizedPath;
                        photoUri = null;
                    }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    private void requestStoragePermission() {
        String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 
                ? android.Manifest.permission.READ_MEDIA_IMAGES 
                : android.Manifest.permission.READ_EXTERNAL_STORAGE;
                
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            // Mostrar un diálogo explicando por qué se necesita el permiso
            new AlertDialog.Builder(this)
                .setTitle("Permiso necesario")
                .setMessage("Se necesita acceso a tus imágenes para seleccionar fotos de tu galería")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Solicitar el permiso
                    ActivityCompat.requestPermissions(this,
                            new String[]{permission},
                            REQUEST_STORAGE_PERMISSION);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
        } else {
            // Solicitar el permiso directamente
            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
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

    /**
     * Carga y muestra las comidas guardadas
     */
    private void loadSavedMeals() {
        // Obtener LinearLayout donde se mostrarán las comidas
        LinearLayout mealsContainer = findViewById(R.id.meals_container);
        TextView noMealsText = findViewById(R.id.no_meals_text);
        
        if (mealsContainer == null) return;
        
        // Limpiar contenedor
        mealsContainer.removeAllViews();
        
        // Obtener listado de comidas guardadas
        SharedPreferences prefs = getSharedPreferences("FoodTrackerPrefs", MODE_PRIVATE);
        String mealIdsStr = prefs.getString("meal_ids", "");
        
        if (mealIdsStr.isEmpty()) {
            // Mostrar mensaje de "no hay comidas"
            if (noMealsText != null) {
                noMealsText.setVisibility(View.VISIBLE);
            }
            return;
        }
        
        // Ocultar mensaje de "no hay comidas"
        if (noMealsText != null) {
            noMealsText.setVisibility(View.GONE);
        }
        
        // Convertir string de IDs a array
        String[] mealIds = mealIdsStr.split(",");
        
        // Obtener fecha actual para filtrar por día
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis();
        
        // Variable para contar comidas del día
        int todayMealsCount = 0;
        
        // Iterar sobre IDs en orden inverso (más reciente primero)
        for (int i = mealIds.length - 1; i >= 0; i--) {
            String mealId = mealIds[i];
            
            // Obtener fecha de la comida
            long mealDate = prefs.getLong(mealId + "_date", 0);
            
            // Filtrar solo las comidas de hoy
            if (mealDate >= startOfDay && mealDate < endOfDay) {
                // Obtener datos de la comida
                String name = prefs.getString(mealId + "_name", "");
                String type = prefs.getString(mealId + "_type", "");
                int calories = prefs.getInt(mealId + "_calories", 0);
                String photoPath = prefs.getString(mealId + "_photo", null);
                
                // Inflar layout para item de comida
                View mealItemView = LayoutInflater.from(this).inflate(R.layout.item_meal, null);
                
                // Configurar vistas
                TextView mealNameText = mealItemView.findViewById(R.id.tv_meal_name);
                TextView mealTypeText = mealItemView.findViewById(R.id.tv_meal_type);
                TextView caloriesText = mealItemView.findViewById(R.id.tv_calories);
                ImageView mealPhotoView = mealItemView.findViewById(R.id.iv_meal_icon);
                
                if (mealNameText != null) mealNameText.setText(name);
                if (mealTypeText != null) mealTypeText.setText(type);
                if (caloriesText != null) {
                    if (calories > 0) {
                        caloriesText.setText(calories + " cal");
                    } else {
                        caloriesText.setText("--");
                    }
                }
                
                // Cargar foto si existe
                if (mealPhotoView != null && photoPath != null && !photoPath.isEmpty()) {
                    try {
                        if (photoPath.startsWith("content:")) {
                            // Es URI de galería
                            Uri photoUri = Uri.parse(photoPath);
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                            mealPhotoView.setImageBitmap(bitmap);
                        } else {
                            // Es ruta de archivo
                            File imgFile = new File(photoPath);
                            if (imgFile.exists()) {
                                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                mealPhotoView.setImageBitmap(bitmap);
                            }
                        }
                        mealPhotoView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        mealPhotoView.setVisibility(View.GONE);
                    }
                } else if (mealPhotoView != null) {
                    mealPhotoView.setVisibility(View.GONE);
                }
                
                // Añadir a contenedor
                mealsContainer.addView(mealItemView);
                todayMealsCount++;
            }
        }
        
        // Si no hay comidas hoy, mostrar mensaje
        if (todayMealsCount == 0 && noMealsText != null) {
            noMealsText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Guarda los datos de la comida
     */
    private void saveMealData() {
        String foodName = foodNameInput.getText().toString().trim();
        String caloriesStr = caloriesInput.getText().toString().trim();
        String mealType = mealTypeSpinner.getSelectedItem().toString();
        
        // Validar campos
        if (foodName.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa el nombre de la comida", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Guardar en SharedPreferences para una solución simple
        SharedPreferences prefs = getSharedPreferences("FoodTrackerPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Crear ID único para esta comida
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String mealId = "meal_" + timeStamp;
        
        // Guardar datos de la comida
        editor.putString(mealId + "_name", foodName);
        editor.putString(mealId + "_type", mealType);
        
        if (!caloriesStr.isEmpty()) {
            editor.putInt(mealId + "_calories", Integer.parseInt(caloriesStr));
        }
        
        if (photoUri != null) {
            editor.putString(mealId + "_photo", photoUri.toString());
        } else if (currentPhotoPath != null) {
            editor.putString(mealId + "_photo", currentPhotoPath);
        }
        
        // Guardar fecha
        editor.putLong(mealId + "_date", System.currentTimeMillis());
        
        // Añadir esta comida a la lista de IDs
        String mealIds = prefs.getString("meal_ids", "");
        if (mealIds.isEmpty()) {
            mealIds = mealId;
        } else {
            mealIds = mealIds + "," + mealId;
        }
        editor.putString("meal_ids", mealIds);
        
        // Guardar todo
        if (editor.commit()) {
            Toast.makeText(this, "Comida guardada correctamente", Toast.LENGTH_SHORT).show();
            // Limpiar formulario
            foodNameInput.setText("");
            caloriesInput.setText("");
            resetPhotoPreview();
            // Actualizar la lista de comidas
            loadSavedMeals();
        } else {
            Toast.makeText(this, "Error al guardar la comida", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Optimiza y guarda una imagen, devolviendo la ruta del archivo optimizado
     */
    private String optimizeAndSaveImage(Uri imageUri) {
        try {
            // Cargar la imagen original
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            
            // Optimizar la imagen
            Bitmap optimizedBitmap = compressBitmap(originalBitmap, 80);
            
            // Crear un archivo para guardar la imagen optimizada
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "FOOD_OPT_" + timeStamp + ".jpg";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile = new File(storageDir, imageFileName);
            
            // Guardar la imagen optimizada
            FileOutputStream fos = new FileOutputStream(imageFile);
            optimizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            
            // Liberar memoria
            if (originalBitmap != optimizedBitmap) {
                originalBitmap.recycle();
            }
            optimizedBitmap.recycle();
            
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Comprime un bitmap para reducir su tamaño
     */
    private Bitmap compressBitmap(Bitmap original, int quality) {
        if (original == null) return null;
        
        int maxSize = 1024; // tamaño máximo (ancho o alto)
        int width = original.getWidth();
        int height = original.getHeight();
        
        // Si la imagen ya es pequeña, devolver el original
        if (width <= maxSize && height <= maxSize) {
            return original;
        }
        
        float scale;
        if (width > height) {
            scale = (float) maxSize / width;
        } else {
            scale = (float) maxSize / height;
        }
        
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        
        // Redimensionar bitmap
        Bitmap resized = Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
        
        // Comprimir a JPEG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        
        // Convertir de nuevo a bitmap (solo si es necesario reducir más)
        if (baos.size() > 500 * 1024) { // Si es mayor a 500KB
            byte[] bytes = baos.toByteArray();
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        
        return resized;
    }
}