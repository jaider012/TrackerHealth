package com.example.trackerhealth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trackerhealth.dao.UserDAO;
import com.example.trackerhealth.model.User;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private UserDAO userDAO;
    
    // Constantes para SharedPreferences
    private static final String PREF_NAME = "TrackerHealthPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar la base de datos
        userDAO = new UserDAO(this);
        
        // Crear usuario de prueba si es la primera vez
        ensureTestUserExists();
        
        // Inicializar vistas
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerLink = findViewById(R.id.register_link);
        
        // Verificar si el usuario ya está logueado
        if (isUserLoggedIn()) {
            navigateToDashboard();
            return;
        }

        // Configurar el botón de login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        // Configurar el enlace de registro
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Rellenar automáticamente las credenciales de prueba
                emailInput.setText("test@example.com");
                passwordInput.setText("password");
                
                Toast.makeText(LoginActivity.this, 
                        "Credenciales de prueba aplicadas. Presiona LOGIN para ingresar.",
                        Toast.LENGTH_LONG).show();
                
                // También podemos crear el usuario si no existe
                User existingUser = userDAO.getUserByEmail("test@example.com");
                if (existingUser == null) {
                    createTestUser();
                }
            }
        });
    }
    
    /**
     * Verifica si hay un usuario ya logueado
     */
    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        
        if (isLoggedIn) {
            // Verificamos también que exista un ID de usuario
            long userId = prefs.getLong(KEY_USER_ID, -1);
            String userEmail = prefs.getString(KEY_USER_EMAIL, "");
            
            // Si no hay ID o email, consideramos que no hay sesión
            if (userId == -1 || userEmail.isEmpty()) {
                // Limpiar preferencias corruptas
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                return false;
            }
            
            // Verificar que el usuario existe en la BD
            User user = userDAO.getUserById(userId);
            if (user == null) {
                // El usuario fue eliminado de la BD, limpiar preferencias
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                return false;
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Navega al dashboard
     */
    private void navigateToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish(); // Cierra esta actividad para que no se pueda regresar con el botón atrás
    }
    
    /**
     * Guarda los datos del usuario en SharedPreferences
     */
    private void saveUserSession(User user) {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putLong(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    /**
     * Intenta hacer login con las credenciales ingresadas
     */
    private void attemptLogin() {
        // Resetear errores
        emailInput.setError(null);
        passwordInput.setError(null);
        
        // Obtener valores de los campos
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        
        // Log para depuración
        Log.d("LoginActivity", "Intentando login con: " + email + " / " + password);
        
        // Validar campos
        boolean cancel = false;
        View focusView = null;
        
        // Validar contraseña
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError(getString(R.string.error_field_required));
            focusView = passwordInput;
            cancel = true;
        }
        
        // Validar email
        if (TextUtils.isEmpty(email)) {
            emailInput.setError(getString(R.string.error_field_required));
            focusView = emailInput;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailInput.setError(getString(R.string.error_invalid_email));
            focusView = emailInput;
            cancel = true;
        }
        
        if (cancel) {
            // Hay un error en los campos, no intentar login
            focusView.requestFocus();
        } else {
            // Verificar si el usuario existe primero
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                Toast.makeText(this, "No existe usuario con ese email", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Intentar autenticar con la base de datos
            User authenticatedUser = userDAO.authenticateUser(email, password);
            
            if (authenticatedUser != null) {
                // Login exitoso
                Toast.makeText(this, "Login exitoso, bienvenido " + authenticatedUser.getName(), Toast.LENGTH_SHORT).show();
                saveUserSession(authenticatedUser);
                navigateToDashboard();
            } else {
                // Login fallido
                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                passwordInput.setError("Contraseña incorrecta");
                passwordInput.requestFocus();
            }
        }
    }
    
    /**
     * Valida el formato del email
     */
    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }
    
    /**
     * Crea un usuario de prueba para facilitar el desarrollo
     */
    private void createTestUser() {
        // Primero verificar si el usuario ya existe
        User existingUser = userDAO.getUserByEmail("test@example.com");
        
        if (existingUser != null) {
            // Si ya existe, mostrar mensaje y prellenar las credenciales
            Toast.makeText(this, "Usuario de prueba ya existe. Usa: test@example.com / password", Toast.LENGTH_LONG).show();
            emailInput.setText("test@example.com");
            passwordInput.setText("password");
            return;
        }
        
        // Si no existe, crearlo
        User testUser = new User("Usuario Prueba", "test@example.com", "password");
        long userId = userDAO.insertUser(testUser);
        
        if (userId > 0) {
            Toast.makeText(this, "Usuario de prueba creado con éxito. Email: test@example.com, Password: password", Toast.LENGTH_LONG).show();
            // Prellenar los campos de login
            emailInput.setText("test@example.com");
            passwordInput.setText("password");
        } else {
            Toast.makeText(this, "No se pudo crear el usuario de prueba. Intenta nuevamente.", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Asegura que el usuario de prueba existe en la base de datos
     */
    private void ensureTestUserExists() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean testUserChecked = prefs.getBoolean("test_user_checked", false);
        
        // Si ya se verificó anteriormente, no hacer nada
        if (testUserChecked) {
            return;
        }
        
        // Verificar si el usuario de prueba ya existe
        User testUser = userDAO.getUserByEmail("test@example.com");
        
        if (testUser == null) {
            // No existe, crearlo silenciosamente
            User newTestUser = new User("Usuario Prueba", "test@example.com", "password");
            long userId = userDAO.insertUser(newTestUser);
            
            if (userId > 0) {
                Log.d("LoginActivity", "Usuario de prueba creado automáticamente");
            } else {
                Log.e("LoginActivity", "No se pudo crear el usuario de prueba automáticamente");
            }
        } else {
            Log.d("LoginActivity", "Usuario de prueba ya existe en la BD");
        }
        
        // Marcar que ya se verificó
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("test_user_checked", true);
        editor.apply();
    }
}
