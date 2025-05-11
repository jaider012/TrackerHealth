package com.example.trackerhealth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trackerhealth.dao.MealDAO;
import com.example.trackerhealth.model.Meal;

import java.io.File;
import java.io.IOException;

public class EditMealActivity extends AppCompatActivity {

    private Spinner mealTypeSpinner;
    private EditText foodNameInput;
    private EditText caloriesInput;
    private EditText proteinsInput;
    private EditText carbsInput;
    private EditText fatsInput;
    private EditText notesInput;
    private ImageView foodPhotoPreview;
    private Button takePhotoButton;
    private Button saveMealButton;
    private Button deleteMealButton;

    private MealDAO mealDao;
    private Meal currentMeal;
    private String currentPhotoPath;
    private Uri photoUri;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_meal);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_meal);
        }

        // Initialize views
        initializeViews();

        // Initialize DAO
        mealDao = new MealDAO(this);

        // Get meal ID from intent
        long mealId = getIntent().getLongExtra("meal_id", -1);
        if (mealId == -1) {
            Toast.makeText(this, "Error: Meal not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load meal data
        currentMeal = mealDao.getMealById(mealId);
        if (currentMeal == null) {
            Toast.makeText(this, "Error: Meal not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup meal type spinner
        setupMealTypeSpinner();

        // Load meal data into views
        loadMealData();

        // Setup button listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        mealTypeSpinner = findViewById(R.id.meal_type_spinner);
        foodNameInput = findViewById(R.id.food_name_input);
        caloriesInput = findViewById(R.id.calories_input);
        proteinsInput = findViewById(R.id.proteins_input);
        carbsInput = findViewById(R.id.carbs_input);
        fatsInput = findViewById(R.id.fats_input);
        notesInput = findViewById(R.id.notes_input);
        foodPhotoPreview = findViewById(R.id.food_photo_preview);
        takePhotoButton = findViewById(R.id.take_photo_button);
        saveMealButton = findViewById(R.id.save_meal_button);
        deleteMealButton = findViewById(R.id.delete_meal_button);
    }

    private void setupMealTypeSpinner() {
        String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_dropdown_item, mealTypes);
        mealTypeSpinner.setAdapter(adapter);
        
        // Set current meal type
        for (int i = 0; i < mealTypes.length; i++) {
            if (mealTypes[i].equals(currentMeal.getMealType())) {
                mealTypeSpinner.setSelection(i);
                break;
            }
        }
    }

    private void loadMealData() {
        foodNameInput.setText(currentMeal.getName());
        caloriesInput.setText(String.valueOf(currentMeal.getCalories()));
        proteinsInput.setText(String.valueOf(currentMeal.getProteins()));
        carbsInput.setText(String.valueOf(currentMeal.getCarbs()));
        fatsInput.setText(String.valueOf(currentMeal.getFats()));
        notesInput.setText(currentMeal.getNotes());

        // Load photo if exists
        String photoPath = currentMeal.getPhotoPath();
        if (photoPath != null && !photoPath.isEmpty()) {
            try {
                if (photoPath.startsWith("content:")) {
                    Uri photoUri = Uri.parse(photoPath);
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    foodPhotoPreview.setImageBitmap(bitmap);
                } else {
                    File imgFile = new File(photoPath);
                    if (imgFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        foodPhotoPreview.setImageBitmap(bitmap);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupButtonListeners() {
        takePhotoButton.setOnClickListener(v -> showImageSourceDialog());
        
        saveMealButton.setOnClickListener(v -> saveMealChanges());
        
        deleteMealButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showImageSourceDialog() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Photo");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                dispatchTakePictureIntent();
            } else if (options[item].equals("Choose from Gallery")) {
                pickImageFromGallery();
            }
            dialog.dismiss();
        });
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    private void saveMealChanges() {
        // Update meal object with new values
        currentMeal.setName(foodNameInput.getText().toString().trim());
        currentMeal.setMealType(mealTypeSpinner.getSelectedItem().toString());
        
        try {
            currentMeal.setCalories(Integer.parseInt(caloriesInput.getText().toString().trim()));
            currentMeal.setProteins(Double.parseDouble(proteinsInput.getText().toString().trim()));
            currentMeal.setCarbs(Double.parseDouble(carbsInput.getText().toString().trim()));
            currentMeal.setFats(Double.parseDouble(fatsInput.getText().toString().trim()));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        currentMeal.setNotes(notesInput.getText().toString().trim());

        // Update photo path if changed
        if (photoUri != null) {
            currentMeal.setPhotoPath(photoUri.toString());
        } else if (currentPhotoPath != null) {
            currentMeal.setPhotoPath(currentPhotoPath);
        }

        // Save to database
        int result = mealDao.update(currentMeal);
        if (result > 0) {
            Toast.makeText(this, "Meal updated successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error updating meal", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Meal")
            .setMessage("Are you sure you want to delete this meal?")
            .setPositiveButton("Delete", (dialog, which) -> deleteMeal())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteMeal() {
        int result = mealDao.delete(currentMeal.getId());
        if (result > 0) {
            Toast.makeText(this, "Meal deleted successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error deleting meal", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    foodPhotoPreview.setImageBitmap(imageBitmap);
                    // TODO: Save bitmap to file and update photoUri/currentPhotoPath
                }
            } else if (requestCode == REQUEST_PICK_IMAGE) {
                photoUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    foodPhotoPreview.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 