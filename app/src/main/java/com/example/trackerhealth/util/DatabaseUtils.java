package com.example.trackerhealth.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.trackerhealth.database.DatabaseHelper;

/**
 * Utilidades para diagnosticar y reparar problemas de base de datos
 */
public class DatabaseUtils {
    private static final String TAG = "DatabaseUtils";
    
    /**
     * Verifica todas las tablas en la base de datos
     * @param context Contexto de la aplicación
     * @return true si todas las tablas están bien, false si hay problemas
     */
    public static boolean verifyAllTables(Context context) {
        Log.d(TAG, "Starting database verification");
        
        boolean allTablesOk = true;
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try {
            // Verificar si la base de datos existe y está abierta
            if (db == null || !db.isOpen()) {
                Log.e(TAG, "Database is not open");
                return false;
            }
            
            // Verificar tabla de usuarios
            if (!tableExists(db, DatabaseHelper.TABLE_USERS)) {
                Log.e(TAG, "Users table does not exist");
                allTablesOk = false;
            }
            
            // Verificar tabla de actividades físicas
            if (!tableExists(db, DatabaseHelper.TABLE_PHYSICAL_ACTIVITIES)) {
                Log.e(TAG, "Physical activities table does not exist");
                allTablesOk = false;
            }
            
            // Verificar esquema de la tabla de actividades físicas
            if (!verifyTableSchema(db, DatabaseHelper.TABLE_PHYSICAL_ACTIVITIES)) {
                Log.e(TAG, "Physical activities table has schema issues");
                allTablesOk = false;
            }
            
            Log.d(TAG, "Database verification complete. Status: " + (allTablesOk ? "OK" : "Issues found"));
            return allTablesOk;
            
        } catch (Exception e) {
            Log.e(TAG, "Error verifying database: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Verifica si una tabla existe en la base de datos
     * @param db Base de datos
     * @param tableName Nombre de la tabla
     * @return true si la tabla existe, false en caso contrario
     */
    private static boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        boolean tableExists = false;
        
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", 
                    new String[] {tableName});
            tableExists = cursor != null && cursor.getCount() > 0;
            Log.d(TAG, "Table " + tableName + " exists: " + tableExists);
        } catch (Exception e) {
            Log.e(TAG, "Error checking if table exists: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return tableExists;
    }
    
    /**
     * Verifica el esquema de una tabla
     * @param db Base de datos
     * @param tableName Nombre de la tabla
     * @return true si el esquema está correcto, false en caso contrario
     */
    private static boolean verifyTableSchema(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        boolean schemaOk = true;
        
        try {
            // Obtener información sobre las columnas de la tabla
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            
            if (cursor != null && cursor.moveToFirst()) {
                // Log de las columnas encontradas
                StringBuilder columns = new StringBuilder("Columns in " + tableName + ": ");
                do {
                    String columnName = cursor.getString(cursor.getColumnIndex("name"));
                    String columnType = cursor.getString(cursor.getColumnIndex("type"));
                    columns.append(columnName).append(" (").append(columnType).append("), ");
                } while (cursor.moveToNext());
                
                Log.d(TAG, columns.toString());
            } else {
                Log.e(TAG, "Could not get schema for table " + tableName);
                schemaOk = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error verifying table schema: " + e.getMessage(), e);
            schemaOk = false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return schemaOk;
    }
    
    /**
     * Intenta crear un registro de prueba en la tabla de actividades físicas
     * @param context Contexto de la aplicación
     * @return El ID del registro insertado, o -1 si falló
     */
    public static long createTestActivity(Context context) {
        Log.d(TAG, "Attempting to create test activity");
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long activityId = -1;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_ACTIVITY_USER_ID_FK, 1); // Usuario predeterminado
            values.put(DatabaseHelper.KEY_ACTIVITY_TYPE, "Test Activity");
            values.put(DatabaseHelper.KEY_ACTIVITY_DURATION, 30);
            values.put(DatabaseHelper.KEY_ACTIVITY_CALORIES, 150);
            values.put(DatabaseHelper.KEY_ACTIVITY_DISTANCE, 2.5);
            values.put(DatabaseHelper.KEY_ACTIVITY_DATE, "2023-08-01 12:30:00");
            values.put(DatabaseHelper.KEY_ACTIVITY_NOTES, "Test note");
            
            // Intentar insertar directamente en la tabla
            db.beginTransaction();
            activityId = db.insert(DatabaseHelper.TABLE_PHYSICAL_ACTIVITIES, null, values);
            
            if (activityId > 0) {
                db.setTransactionSuccessful();
                Log.d(TAG, "Test activity created successfully with ID: " + activityId);
            } else {
                Log.e(TAG, "Failed to create test activity");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating test activity: " + e.getMessage(), e);
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }
        
        return activityId;
    }
    
    /**
     * Verificar la existencia de un usuario y crearlo si no existe
     * @param context Contexto de la aplicación
     * @param userId ID del usuario a verificar
     * @return true si el usuario existe o fue creado, false en caso contrario
     */
    public static boolean ensureUserExists(Context context, long userId) {
        Log.d(TAG, "Ensuring user exists with ID: " + userId);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        boolean success = false;
        
        try {
            // Verificar si el usuario existe
            String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS + 
                    " WHERE " + DatabaseHelper.KEY_USER_ID + " = ?";
            
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
            
            boolean userExists = false;
            if (cursor != null && cursor.moveToFirst()) {
                userExists = cursor.getInt(0) > 0;
                cursor.close();
            }
            
            if (userExists) {
                Log.d(TAG, "User exists with ID: " + userId);
                success = true;
            } else {
                // Crear usuario si no existe
                db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.KEY_USER_ID, userId);
                values.put(DatabaseHelper.KEY_USER_NAME, "User " + userId);
                values.put(DatabaseHelper.KEY_USER_EMAIL, "user" + userId + "@example.com");
                values.put(DatabaseHelper.KEY_USER_PASSWORD, "password");
                values.put(DatabaseHelper.KEY_USER_HEIGHT, 170);
                values.put(DatabaseHelper.KEY_USER_WEIGHT, 70);
                
                long newUserId = db.insert(DatabaseHelper.TABLE_USERS, null, values);
                
                if (newUserId > 0) {
                    Log.d(TAG, "Created new user with ID: " + newUserId);
                    success = true;
                } else {
                    Log.e(TAG, "Failed to create new user");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error ensuring user exists: " + e.getMessage(), e);
        }
        
        return success;
    }
    
    /**
     * Recrea la tabla de actividades físicas
     * @param context Contexto de la aplicación
     * @return true si se recreó con éxito, false en caso contrario
     */
    public static boolean recreatePhysicalActivitiesTable(Context context) {
        Log.d(TAG, "Recreating physical activities table");
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            // Eliminar la tabla si existe
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseHelper.TABLE_PHYSICAL_ACTIVITIES);
            
            // Crear la tabla de nuevo
            String CREATE_PHYSICAL_ACTIVITIES_TABLE = "CREATE TABLE " + DatabaseHelper.TABLE_PHYSICAL_ACTIVITIES +
                    "(" +
                    DatabaseHelper.KEY_ACTIVITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseHelper.KEY_ACTIVITY_USER_ID_FK + " INTEGER," +
                    DatabaseHelper.KEY_ACTIVITY_TYPE + " TEXT NOT NULL," +
                    DatabaseHelper.KEY_ACTIVITY_DURATION + " INTEGER NOT NULL," +
                    DatabaseHelper.KEY_ACTIVITY_CALORIES + " INTEGER," +
                    DatabaseHelper.KEY_ACTIVITY_DISTANCE + " REAL," +
                    DatabaseHelper.KEY_ACTIVITY_DATE + " DATETIME," +
                    DatabaseHelper.KEY_ACTIVITY_NOTES + " TEXT," +
                    DatabaseHelper.KEY_ACTIVITY_PHOTO_PATH + " TEXT," +
                    DatabaseHelper.KEY_ACTIVITY_LATITUDE + " REAL," +
                    DatabaseHelper.KEY_ACTIVITY_LONGITUDE + " REAL," +
                    "FOREIGN KEY(" + DatabaseHelper.KEY_ACTIVITY_USER_ID_FK + ") REFERENCES " + 
                    DatabaseHelper.TABLE_USERS + "(" + DatabaseHelper.KEY_USER_ID + ") ON DELETE CASCADE" +
                    ");";
            
            db.execSQL(CREATE_PHYSICAL_ACTIVITIES_TABLE);
            Log.d(TAG, "Physical activities table recreated successfully");
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error recreating physical activities table: " + e.getMessage(), e);
            return false;
        }
    }
} 