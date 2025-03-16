package com.example.trackerhealth.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.trackerhealth.database.DatabaseHelper;
import com.example.trackerhealth.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private static final String TAG = UserDAO.class.getSimpleName();
    private DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Inserta un nuevo usuario en la base de datos
     *
     * @param user El usuario a insertar
     * @return El ID del usuario insertado, o -1 si ocurre un error
     */
    public long insertUser(User user) {
        long userId = -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_USER_NAME, user.getName());
            values.put(DatabaseHelper.KEY_USER_EMAIL, user.getEmail());
            values.put(DatabaseHelper.KEY_USER_PASSWORD, user.getPassword());
            values.put(DatabaseHelper.KEY_USER_HEIGHT, user.getHeight());
            values.put(DatabaseHelper.KEY_USER_WEIGHT, user.getWeight());
            values.put(DatabaseHelper.KEY_USER_AGE, user.getAge());
            values.put(DatabaseHelper.KEY_USER_GENDER, user.getGender());

            // Insertar la fila
            userId = db.insertOrThrow(DatabaseHelper.TABLE_USERS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error al insertar usuario: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
        
        return userId;
    }

    /**
     * Actualiza un usuario existente en la base de datos
     *
     * @param user El usuario con los datos actualizados
     * @return true si la actualización fue exitosa, false en caso contrario
     */
    public boolean updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = 0;
        
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_USER_NAME, user.getName());
            values.put(DatabaseHelper.KEY_USER_EMAIL, user.getEmail());
            values.put(DatabaseHelper.KEY_USER_HEIGHT, user.getHeight());
            values.put(DatabaseHelper.KEY_USER_WEIGHT, user.getWeight());
            values.put(DatabaseHelper.KEY_USER_AGE, user.getAge());
            values.put(DatabaseHelper.KEY_USER_GENDER, user.getGender());

            // Si hay una nueva contraseña, actualizarla
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                values.put(DatabaseHelper.KEY_USER_PASSWORD, user.getPassword());
            }

            // Actualizar la fila
            String selection = DatabaseHelper.KEY_USER_ID + " = ?";
            String[] selectionArgs = { String.valueOf(user.getId()) };
            
            rows = db.update(DatabaseHelper.TABLE_USERS, values, selection, selectionArgs);
            
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar usuario: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
        
        return rows > 0;
    }

    /**
     * Elimina un usuario de la base de datos
     *
     * @param userId El ID del usuario a eliminar
     * @return true si la eliminación fue exitosa, false en caso contrario
     */
    public boolean deleteUser(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = 0;
        
        db.beginTransaction();
        try {
            // Eliminar usuario por ID
            String selection = DatabaseHelper.KEY_USER_ID + " = ?";
            String[] selectionArgs = { String.valueOf(userId) };
            
            rows = db.delete(DatabaseHelper.TABLE_USERS, selection, selectionArgs);
            
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error al eliminar usuario: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
        
        return rows > 0;
    }

    /**
     * Busca un usuario por su ID
     *
     * @param userId El ID del usuario a buscar
     * @return El usuario si se encuentra, null en caso contrario
     */
    public User getUserById(long userId) {
        String SELECT_QUERY = "SELECT * FROM " + DatabaseHelper.TABLE_USERS +
                " WHERE " + DatabaseHelper.KEY_USER_ID + " = ?";
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(SELECT_QUERY, new String[]{String.valueOf(userId)});
            if (cursor != null && cursor.moveToFirst()) {
                user = getUserFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener usuario por ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return user;
    }

    /**
     * Busca un usuario por su correo electrónico
     *
     * @param email El correo electrónico del usuario a buscar
     * @return El usuario si se encuentra, null en caso contrario
     */
    public User getUserByEmail(String email) {
        String SELECT_QUERY = "SELECT * FROM " + DatabaseHelper.TABLE_USERS +
                " WHERE " + DatabaseHelper.KEY_USER_EMAIL + " = ?";
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(SELECT_QUERY, new String[]{email});
            if (cursor != null && cursor.moveToFirst()) {
                user = getUserFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener usuario por email: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return user;
    }

    /**
     * Verifica si las credenciales del usuario son válidas
     *
     * @param email El correo electrónico del usuario
     * @param password La contraseña del usuario
     * @return El usuario si las credenciales son válidas, null en caso contrario
     */
    public User authenticateUser(String email, String password) {
        User user = getUserByEmail(email);
        
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        
        return null;
    }

    /**
     * Obtiene todos los usuarios de la base de datos
     *
     * @return Lista de todos los usuarios
     */
    public List<User> getAllUsers() {
        String SELECT_QUERY = "SELECT * FROM " + DatabaseHelper.TABLE_USERS;
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<User> users = new ArrayList<>();
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(SELECT_QUERY, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    User user = getUserFromCursor(cursor);
                    users.add(user);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener todos los usuarios: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return users;
    }

    /**
     * Método auxiliar para obtener un usuario a partir de un cursor
     */
    private User getUserFromCursor(Cursor cursor) {
        User user = new User();
        
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_ID)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_PASSWORD)));
        
        // Campos opcionales
        int heightIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_HEIGHT);
        int weightIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_WEIGHT);
        int ageIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_AGE);
        int genderIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_GENDER);
        int createdAtIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_CREATED_AT);
        
        if (!cursor.isNull(heightIndex)) {
            user.setHeight(cursor.getDouble(heightIndex));
        }
        
        if (!cursor.isNull(weightIndex)) {
            user.setWeight(cursor.getDouble(weightIndex));
        }
        
        if (!cursor.isNull(ageIndex)) {
            user.setAge(cursor.getInt(ageIndex));
        }
        
        if (!cursor.isNull(genderIndex)) {
            user.setGender(cursor.getString(genderIndex));
        }
        
        if (!cursor.isNull(createdAtIndex)) {
            user.setCreatedAt(cursor.getString(createdAtIndex));
        }
        
        return user;
    }
} 