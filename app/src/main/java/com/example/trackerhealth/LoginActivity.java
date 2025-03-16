package com.example.trackerhealth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
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
        
        // Verificar si el usuario ya está logueado
        if (isUserLoggedIn()) {
            navigateToDashboard();
            return;
        }
        
        setContentView(R.layout.activity_login);

        // Inicializar la base de datos
        userDAO = new UserDAO(this);
        
        // Inicializar vistas
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerLink = findViewById(R.id.register_link);

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
                // Aquí normalmente abriríamos una actividad de registro
                // Por ahora, mostramos un mensaje
                Toast.makeText(LoginActivity.this, "Función de registro en desarrollo", Toast.LENGTH_SHORT).show();
                
                // Código para crear un usuario de prueba
                createTestUser();
            }
        });
    }
    
    /**
     * Verifica si hay un usuario ya logueado
     */
    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
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
            // Intentar autenticar con la base de datos
            User authenticatedUser = userDAO.authenticateUser(email, password);
            
            if (authenticatedUser != null) {
                // Login exitoso
                saveUserSession(authenticatedUser);
                navigateToDashboard();
            } else {
                // Login fallido
                Toast.makeText(this, R.string.error_login_failed, Toast.LENGTH_SHORT).show();
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
        User testUser = new User("Usuario Prueba", "test@example.com", "password");
        long userId = userDAO.insertUser(testUser);
        
        if (userId > 0) {
            Toast.makeText(this, "Usuario de prueba creado con éxito. Email: test@example.com, Password: password", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No se pudo crear el usuario de prueba (posiblemente ya existe)", Toast.LENGTH_SHORT).show();
        }
    }
}
