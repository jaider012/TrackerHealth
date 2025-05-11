package com.example.trackerhealth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackerhealth.adapters.ActivityAdapter;
import com.example.trackerhealth.adapters.MealAdapter;
import com.example.trackerhealth.dao.MealDAO;
import com.example.trackerhealth.dao.PhysicalActivityDAO;
import com.example.trackerhealth.model.Meal;
import com.example.trackerhealth.model.PhysicalActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private Button logoutButton;
    private Button runningDashboardButton;
    private TextView welcomeText;
    private TextView noActivitiesText;
    private TextView noMealsText;
    private RecyclerView activitiesRecyclerView;
    private RecyclerView mealsRecyclerView;
    private TextView stepsValue;
    private TextView caloriesValue;
    private TextView exercisesValue;
    
    // Adaptadores
    private ActivityAdapter activityAdapter;
    private MealAdapter mealAdapter;
    
    // Listas de datos
    private List<PhysicalActivity> activityList;
    private List<Meal> mealList;
    
    // DAOs
    private PhysicalActivityDAO activityDAO;
    private MealDAO mealDAO;
    
    // Constantes para SharedPreferences
    private static final String PREF_NAME = "TrackerHealthPrefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    // ID de usuario actual (en un app real se tomaría del login)
    private long currentUserId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Inicializar vistas
        initializeViews();
        
        // Inicializar DAO
        activityDAO = new PhysicalActivityDAO(this);
        mealDAO = new MealDAO(this);
        
        // Inicializar listas
        activityList = new ArrayList<>();
        mealList = new ArrayList<>();
        
        // Configurar RecyclerViews
        setupRecyclerViews();
        
        // Configurar el mensaje de bienvenida personalizado
        setupWelcomeMessage();
        
        // Configurar botones
        setupButtons();
        
        // Configurar bottom navigation
        setupBottomNavigation();
        
        // Cargar datos
        loadDashboardData();
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        logoutButton = findViewById(R.id.logout_button);
        runningDashboardButton = findViewById(R.id.running_dashboard_button);
        welcomeText = findViewById(R.id.welcome_text);
        noActivitiesText = findViewById(R.id.no_activities_text);
        noMealsText = findViewById(R.id.no_meals_text);
        activitiesRecyclerView = findViewById(R.id.activities_recycler_view);
        mealsRecyclerView = findViewById(R.id.meals_recycler_view);
        stepsValue = findViewById(R.id.steps_value);
        caloriesValue = findViewById(R.id.calories_value);
        exercisesValue = findViewById(R.id.exercises_value);
    }

    private void setupRecyclerViews() {
        // Configurar RecyclerView de actividades
        activitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        activityAdapter = new ActivityAdapter(this, activityList, activity -> {
            WorkoutDetailActivity.start(this, activity.getId(), activity.getActivityType());
        });
        activitiesRecyclerView.setAdapter(activityAdapter);
        
        // Configurar RecyclerView de comidas
        LinearLayoutManager mealsLayoutManager = new LinearLayoutManager(this);
        mealsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mealsRecyclerView.setLayoutManager(mealsLayoutManager);
        mealAdapter = new MealAdapter(this, mealList, meal -> {
            // Abrir detalles de la comida al hacer clic
            Toast.makeText(this, "Detalles de: " + meal.getName(), Toast.LENGTH_SHORT).show();
        });
        mealsRecyclerView.setAdapter(mealAdapter);
        
        // Agregar decoración para separar items
        int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        mealsRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view, 
                                     @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = spacing;
            }
        });
    }

    private void setupWelcomeMessage() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String userName = prefs.getString(KEY_USER_NAME, "");
        if (!userName.isEmpty()) {
            welcomeText.setText(getString(R.string.welcome_user, userName));
        }
    }

    private void setupButtons() {
        logoutButton.setOnClickListener(v -> logout());
        
        if (runningDashboardButton != null) {
            runningDashboardButton.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, RunningDashboardActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
    }
    
    private void loadDashboardData() {
        try {
            loadRecentActivities();
            loadAllMeals();
            updateDailySummary();
        } catch (Exception e) {
            Log.e("DashboardActivity", "Error al cargar datos: " + e.getMessage(), e);
            Toast.makeText(this, "Error al cargar algunos datos. Intente nuevamente.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadAllMeals() {
        try {
            // Limpiar lista anterior
            mealList.clear();
            
            // Obtener todas las comidas del usuario
            List<Meal> meals = mealDAO.getAllMealsByUser(currentUserId);
            
            if (meals.isEmpty()) {
                // Solo crear datos de ejemplo si no hay comidas
                createSampleMeals();
            } else {
                mealList.addAll(meals);
            }
            
            // Actualizar UI
            if (mealList.isEmpty()) {
                noMealsText.setVisibility(View.VISIBLE);
                mealsRecyclerView.setVisibility(View.GONE);
            } else {
                noMealsText.setVisibility(View.GONE);
                mealsRecyclerView.setVisibility(View.VISIBLE);
                mealAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e("DashboardActivity", "Error al cargar comidas: " + e.getMessage(), e);
            noMealsText.setText(R.string.error_loading_meals);
            noMealsText.setVisibility(View.VISIBLE);
            mealsRecyclerView.setVisibility(View.GONE);
        }
    }
    
    private void loadRecentActivities() {
        try {
            // Limpiar lista anterior
            activityList.clear();
            
            // Obtener actividades del usuario actual
            List<PhysicalActivity> recentActivities = activityDAO.getRecentActivities(currentUserId, 5);
            
            if (recentActivities.isEmpty()) {
                noActivitiesText.setVisibility(View.VISIBLE);
                activitiesRecyclerView.setVisibility(View.GONE);
            } else {
                activityList.addAll(recentActivities);
                noActivitiesText.setVisibility(View.GONE);
                activitiesRecyclerView.setVisibility(View.VISIBLE);
                activityAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e("DashboardActivity", "Error al cargar actividades: " + e.getMessage(), e);
            noActivitiesText.setText("Error al cargar actividades");
            noActivitiesText.setVisibility(View.VISIBLE);
            activitiesRecyclerView.setVisibility(View.GONE);
        }
    }
    
    private void updateDailySummary() {
        try {
            // Sumar calorías consumidas en comidas
            int totalCalories = 0;
            for (Meal meal : mealList) {
                totalCalories += meal.getCalories();
            }
            
            // Calcular pasos (simulados para la demo)
            int steps = 6532; // Valor de ejemplo
            
            // Calcular número de ejercicios realizados hoy
            int exercisesCount = activityDAO.getActivitiesByDate(currentUserId, 
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())).size();
            
            // Actualizar UI
            stepsValue.setText(String.valueOf(steps));
            caloriesValue.setText(String.valueOf(totalCalories));
            exercisesValue.setText(String.valueOf(exercisesCount));
        } catch (Exception e) {
            Log.e("DashboardActivity", "Error al actualizar resumen diario: " + e.getMessage(), e);
            // En caso de error, mostrar valores predeterminados
            stepsValue.setText("0");
            caloriesValue.setText("0");
            exercisesValue.setText("0");
        }
    }
    
    /**
     * Cierra la sesión del usuario y regresa a la pantalla de login
     */
    private void logout() {
        // Limpiar las preferencias de usuario
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
        
        // Redirigir a la pantalla de login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        
        int itemId = item.getItemId();
        
        if (itemId == R.id.navigation_dashboard) {
            return true;
        } else if (itemId == R.id.navigation_activity) {
            intent = new Intent(this, PhysicalActivityTracker.class);
            startActivity(intent);
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
    
    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos cuando se vuelve a la actividad
        loadDashboardData();
    }

    // Crear datos de ejemplo para la demo
    private void createSampleMeals() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String today = dateFormat.format(new Date());
            
            // Crear comidas de ejemplo
            Meal breakfast = new Meal();
            breakfast.setUserId(currentUserId);
            breakfast.setName("Avena con frutas y nueces");
            breakfast.setMealType("Desayuno");
            breakfast.setCalories(420);
            breakfast.setProteins(14);
            breakfast.setCarbs(65);
            breakfast.setFats(12);
            breakfast.setDate(today);
            breakfast.setTime("07:30");
            breakfast.setNotes("Con plátano, fresas y almendras");
            
            Meal snack1 = new Meal();
            snack1.setUserId(currentUserId);
            snack1.setName("Batido de proteínas");
            snack1.setMealType("Snack");
            snack1.setCalories(180);
            snack1.setProteins(25);
            snack1.setCarbs(15);
            snack1.setFats(3);
            snack1.setDate(today);
            snack1.setTime("10:30");
            
            Meal lunch = new Meal();
            lunch.setUserId(currentUserId);
            lunch.setName("Pechuga de pollo a la plancha");
            lunch.setMealType("Almuerzo");
            lunch.setCalories(550);
            lunch.setProteins(45);
            lunch.setCarbs(35);
            lunch.setFats(25);
            lunch.setDate(today);
            lunch.setTime("13:00");
            lunch.setNotes("Con ensalada y arroz integral");
            
            Meal snack2 = new Meal();
            snack2.setUserId(currentUserId);
            snack2.setName("Yogur con granola");
            snack2.setMealType("Snack");
            snack2.setCalories(220);
            snack2.setProteins(10);
            snack2.setCarbs(28);
            snack2.setFats(8);
            snack2.setDate(today);
            snack2.setTime("16:30");
            
            Meal dinner = new Meal();
            dinner.setUserId(currentUserId);
            dinner.setName("Salmón al horno");
            dinner.setMealType("Cena");
            dinner.setCalories(480);
            dinner.setProteins(35);
            dinner.setCarbs(25);
            dinner.setFats(28);
            dinner.setDate(today);
            dinner.setTime("19:30");
            dinner.setNotes("Con vegetales asados");
            
            // Guardar en la base de datos y añadir a la lista
            long breakfastId = mealDAO.addMeal(breakfast);
            long snack1Id = mealDAO.addMeal(snack1);
            long lunchId = mealDAO.addMeal(lunch);
            long snack2Id = mealDAO.addMeal(snack2);
            long dinnerId = mealDAO.addMeal(dinner);
            
            if (breakfastId > 0) {
                breakfast.setId(breakfastId);
                mealList.add(breakfast);
            }
            
            if (snack1Id > 0) {
                snack1.setId(snack1Id);
                mealList.add(snack1);
            }
            
            if (lunchId > 0) {
                lunch.setId(lunchId);
                mealList.add(lunch);
            }
            
            if (snack2Id > 0) {
                snack2.setId(snack2Id);
                mealList.add(snack2);
            }
            
            if (dinnerId > 0) {
                dinner.setId(dinnerId);
                mealList.add(dinner);
            }
        } catch (Exception e) {
            Log.e("DashboardActivity", "Error al crear comidas de ejemplo: " + e.getMessage(), e);
        }
    }
}