package com.example.trackerhealth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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

import com.example.trackerhealth.dao.PhysicalActivityDAO;
import com.example.trackerhealth.model.PhysicalActivity;

import java.io.IOException;

public class EditPhysicalActivityActivity extends AppCompatActivity {

    private Spinner activityTypeSpinner;
    private EditText durationEditText;
    private EditText distanceEditText;
    private ImageView activityPhotoPreview;
    private Button takePhotoButton;
    private Button saveChangesButton;
    private Button deleteActivityButton;

    private PhysicalActivityDAO activityDAO;
    private PhysicalActivity currentActivity;
    private Uri photoUri;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_physical_activity);

        // Initialize DAO
        activityDAO = new PhysicalActivityDAO(this);

        // Initialize views
        initializeViews();

        // Get activity ID from intent
        long activityId = getIntent().getLongExtra("activity_id", -1);
        if (activityId == -1) {
            Toast.makeText(this, "Error: Activity not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load activity data
        loadActivityData(activityId);

        // Setup listeners
        setupListeners();

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_activity);
        }
    }

    private void initializeViews() {
        activityTypeSpinner = findViewById(R.id.activity_type_spinner);
        durationEditText = findViewById(R.id.duration_input);
        distanceEditText = findViewById(R.id.distance_input);
        activityPhotoPreview = findViewById(R.id.activity_photo_preview);
        takePhotoButton = findViewById(R.id.take_photo_button);
        saveChangesButton = findViewById(R.id.save_changes_button);
        deleteActivityButton = findViewById(R.id.delete_activity_button);

        // Setup activity type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.activity_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activityTypeSpinner.setAdapter(adapter);
    }

    private void loadActivityData(long activityId) {
        currentActivity = activityDAO.getActivityById(activityId);
        if (currentActivity == null) {
            Toast.makeText(this, "Error: Activity not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set activity type
        int position = getActivityTypePosition(currentActivity.getActivityType());
        activityTypeSpinner.setSelection(position);

        // Set duration and distance
        durationEditText.setText(String.valueOf(currentActivity.getDuration()));
        if (currentActivity.getDistance() > 0) {
            distanceEditText.setText(String.format("%.2f", currentActivity.getDistance()));
        }

        // Load photo if exists
        String photoPath = currentActivity.getPhotoPath();
        if (photoPath != null && !photoPath.isEmpty()) {
            try {
                photoUri = Uri.parse(photoPath);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                activityPhotoPreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupListeners() {
        takePhotoButton.setOnClickListener(v -> showImageSourceDialog());

        saveChangesButton.setOnClickListener(v -> saveChanges());

        deleteActivityButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showImageSourceDialog() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } else if (options[item].equals("Choose from Gallery")) {
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhotoIntent, REQUEST_PICK_IMAGE);
            }
        });
        builder.show();
    }

    private void saveChanges() {
        if (!validateInputs()) {
            return;
        }

        // Update activity data
        currentActivity.setActivityType(activityTypeSpinner.getSelectedItem().toString());
        currentActivity.setDuration(Integer.parseInt(durationEditText.getText().toString()));
        
        if (!TextUtils.isEmpty(distanceEditText.getText())) {
            currentActivity.setDistance(Double.parseDouble(distanceEditText.getText().toString()));
        }

        if (photoUri != null) {
            currentActivity.setPhotoPath(photoUri.toString());
        }

        // Save to database
        if (activityDAO.updateActivity(currentActivity)) {
            Toast.makeText(this, "Activity updated successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error updating activity", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(durationEditText.getText())) {
            durationEditText.setError("Duration is required");
            return false;
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Activity")
                .setMessage("Are you sure you want to delete this activity?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (activityDAO.deleteActivity(currentActivity.getId())) {
                        Toast.makeText(this, "Activity deleted", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Error deleting activity", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int getActivityTypePosition(String activityType) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) activityTypeSpinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(activityType)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    activityPhotoPreview.setImageBitmap(imageBitmap);
                    // Save bitmap to file and get URI
                    // TODO: Implement proper image saving
                }
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                photoUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                    activityPhotoPreview.setImageBitmap(bitmap);
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