package com.example.trackerhealth.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.trackerhealth.database.DatabaseHelper;
import com.example.trackerhealth.model.PhysicalActivity;

import java.util.ArrayList;
import java.util.List;

public class PhysicalActivityDAO {
    
    private static final String TAG = PhysicalActivityDAO.class.getSimpleName();
    private final DatabaseHelper dbHelper;

    // Table and column names
    private static final String TABLE_ACTIVITIES = "physical_activities";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_ACTIVITY_TYPE = "activity_type";
    private static final String COLUMN_DURATION = "duration";
    private static final String COLUMN_CALORIES_BURNED = "calories_burned";
    private static final String COLUMN_DISTANCE = "distance";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_NOTES = "notes";
    private static final String COLUMN_PHOTO_PATH = "photo_path";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";

    public PhysicalActivityDAO(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }
    
    /**
     * Inserta una nueva actividad física en la base de datos
     *
     * @param activity La actividad física a insertar
     * @return El ID de la actividad insertada, o -1 si ocurre un error
     */
    public long insertActivity(PhysicalActivity activity) {
        long activityId = -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_ID, activity.getUserId());
            values.put(COLUMN_ACTIVITY_TYPE, activity.getActivityType());
            values.put(COLUMN_DURATION, activity.getDuration());
            values.put(COLUMN_CALORIES_BURNED, activity.getCaloriesBurned());
            values.put(COLUMN_DISTANCE, activity.getDistance());
            values.put(COLUMN_DATE, activity.getDate());
            values.put(COLUMN_NOTES, activity.getNotes());
            values.put(COLUMN_PHOTO_PATH, activity.getPhotoPath());
            values.put(COLUMN_LATITUDE, activity.getLatitude());
            values.put(COLUMN_LONGITUDE, activity.getLongitude());
            
            // Insertar la fila
            activityId = db.insertOrThrow(TABLE_ACTIVITIES, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error al insertar actividad física: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
        
        return activityId;
    }
    
    /**
     * Actualiza una actividad física existente en la base de datos
     *
     * @param activity La actividad física con los datos actualizados
     * @return true si la actualización fue exitosa, false en caso contrario
     */
    public boolean updateActivity(PhysicalActivity activity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = 0;
        
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ACTIVITY_TYPE, activity.getActivityType());
            values.put(COLUMN_DURATION, activity.getDuration());
            values.put(COLUMN_CALORIES_BURNED, activity.getCaloriesBurned());
            values.put(COLUMN_DISTANCE, activity.getDistance());
            values.put(COLUMN_DATE, activity.getDate());
            values.put(COLUMN_NOTES, activity.getNotes());
            values.put(COLUMN_PHOTO_PATH, activity.getPhotoPath());
            values.put(COLUMN_LATITUDE, activity.getLatitude());
            values.put(COLUMN_LONGITUDE, activity.getLongitude());
            
            // Actualizar la fila
            String selection = COLUMN_ID + " = ?";
            String[] selectionArgs = { String.valueOf(activity.getId()) };
            
            rows = db.update(TABLE_ACTIVITIES, values, selection, selectionArgs);
            
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar actividad física: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
        
        return rows > 0;
    }
    
    /**
     * Elimina una actividad física de la base de datos
     *
     * @param activityId El ID de la actividad física a eliminar
     * @return true si la eliminación fue exitosa, false en caso contrario
     */
    public boolean deleteActivity(long activityId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = 0;
        
        db.beginTransaction();
        try {
            // Eliminar actividad por ID
            String selection = COLUMN_ID + " = ?";
            String[] selectionArgs = { String.valueOf(activityId) };
            
            rows = db.delete(TABLE_ACTIVITIES, selection, selectionArgs);
            
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error al eliminar actividad física: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
        
        return rows > 0;
    }
    
    /**
     * Busca una actividad física por su ID
     *
     * @param activityId El ID de la actividad física a buscar
     * @return La actividad física si se encuentra, null en caso contrario
     */
    public PhysicalActivity getActivityById(long activityId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String[] projection = {
            COLUMN_ID,
            COLUMN_USER_ID,
            COLUMN_ACTIVITY_TYPE,
            COLUMN_DURATION,
            COLUMN_CALORIES_BURNED,
            COLUMN_DISTANCE,
            COLUMN_DATE,
            COLUMN_NOTES,
            COLUMN_PHOTO_PATH,
            COLUMN_LATITUDE,
            COLUMN_LONGITUDE
        };

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(activityId)};

        Cursor cursor = db.query(
            TABLE_ACTIVITIES,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        );

        PhysicalActivity activity = null;
        if (cursor.moveToFirst()) {
            activity = cursorToActivity(cursor);
        }

        cursor.close();
        return activity;
    }
    
    /**
     * Obtiene todas las actividades físicas de un usuario
     *
     * @param userId El ID del usuario
     * @return Lista de actividades físicas del usuario
     */
    public List<PhysicalActivity> getActivitiesByUserId(long userId) {
        List<PhysicalActivity> activityList = new ArrayList<>();
        
        String query = "SELECT * FROM " + TABLE_ACTIVITIES + 
                      " WHERE " + COLUMN_USER_ID + " = ?" +
                      " ORDER BY " + COLUMN_DATE + " DESC";
                      
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            do {
                PhysicalActivity activity = cursorToActivity(cursor);
                activityList.add(activity);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return activityList;
    }
    
    /**
     * Obtiene actividades físicas de un usuario en un rango de fechas
     *
     * @param userId El ID del usuario
     * @param startDate Fecha de inicio (formato YYYY-MM-DD)
     * @param endDate Fecha de fin (formato YYYY-MM-DD)
     * @return Lista de actividades físicas en el rango de fechas
     */
    public List<PhysicalActivity> getActivitiesByDateRange(long userId, String startDate, String endDate) {
        String SELECT_QUERY = "SELECT * FROM " + TABLE_ACTIVITIES +
                " WHERE " + COLUMN_USER_ID + " = ?" +
                " AND " + COLUMN_DATE + " BETWEEN ? AND ?" +
                " ORDER BY " + COLUMN_DATE + " DESC";
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<PhysicalActivity> activities = new ArrayList<>();
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(SELECT_QUERY, new String[]{
                    String.valueOf(userId), startDate, endDate
            });
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    PhysicalActivity activity = cursorToActivity(cursor);
                    activities.add(activity);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener actividades físicas por rango de fechas: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return activities;
    }
    
    /**
     * Obtiene un resumen de calorías quemadas por un usuario
     *
     * @param userId El ID del usuario
     * @param days Número de días hacia atrás para el resumen
     * @return Total de calorías quemadas en el periodo
     */
    public int getTotalCaloriesBurned(long userId, int days) {
        String SELECT_QUERY = "SELECT SUM(" + COLUMN_CALORIES_BURNED + ") as total" +
                " FROM " + TABLE_ACTIVITIES +
                " WHERE " + COLUMN_USER_ID + " = ?" +
                " AND " + COLUMN_DATE + " >= date('now', '-" + days + " days')";
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int totalCalories = 0;
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(SELECT_QUERY, new String[]{String.valueOf(userId)});
            if (cursor != null && cursor.moveToFirst()) {
                totalCalories = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener calorías quemadas: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return totalCalories;
    }
    
    /**
     * Obtiene actividades físicas con datos de ubicación GPS para el usuario
     *
     * @param userId El ID del usuario
     * @return Lista de actividades físicas con datos de ubicación
     */
    public List<PhysicalActivity> getActivitiesWithLocation(long userId) {
        String SELECT_QUERY = "SELECT * FROM " + TABLE_ACTIVITIES +
                " WHERE " + COLUMN_USER_ID + " = ?" +
                " AND " + COLUMN_LATITUDE + " IS NOT NULL" +
                " AND " + COLUMN_LONGITUDE + " IS NOT NULL" +
                " ORDER BY " + COLUMN_DATE + " DESC";
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<PhysicalActivity> activities = new ArrayList<>();
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(SELECT_QUERY, new String[]{String.valueOf(userId)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    PhysicalActivity activity = cursorToActivity(cursor);
                    activities.add(activity);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener actividades con ubicación: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return activities;
    }
    
    /**
     * Obtiene las estadísticas de ubicación GPS para un usuario
     * @param userId El ID del usuario
     * @return Un array con [distancia total, actividades con GPS]
     */
    public double[] getLocationStats(long userId) {
        String SELECT_QUERY = "SELECT COUNT(*) as count, SUM(" + 
                COLUMN_DISTANCE + ") as total_distance " +
                " FROM " + TABLE_ACTIVITIES +
                " WHERE " + COLUMN_USER_ID + " = ?" +
                " AND " + COLUMN_LATITUDE + " IS NOT NULL" +
                " AND " + COLUMN_LONGITUDE + " IS NOT NULL";
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double[] stats = new double[2]; // [total_distance, count]
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(SELECT_QUERY, new String[]{String.valueOf(userId)});
            if (cursor != null && cursor.moveToFirst()) {
                stats[0] = cursor.isNull(1) ? 0 : cursor.getDouble(1); // total_distance
                stats[1] = cursor.getInt(0); // count
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener estadísticas de ubicación: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return stats;
    }
    
    /**
     * Método auxiliar para obtener una actividad física a partir de un cursor
     */
    private PhysicalActivity cursorToActivity(Cursor cursor) {
        PhysicalActivity activity = new PhysicalActivity();
        
        activity.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        activity.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
        activity.setActivityType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIVITY_TYPE)));
        activity.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)));
        activity.setCaloriesBurned(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES_BURNED)));
        activity.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
        
        // Campos opcionales
        int distanceIndex = cursor.getColumnIndexOrThrow(COLUMN_DISTANCE);
        int notesIndex = cursor.getColumnIndexOrThrow(COLUMN_NOTES);
        
        if (!cursor.isNull(distanceIndex)) {
            activity.setDistance(cursor.getDouble(distanceIndex));
        }
        
        if (!cursor.isNull(notesIndex)) {
            activity.setNotes(cursor.getString(notesIndex));
        }
        
        // Campos de ubicación
        try {
            int latIndex = cursor.getColumnIndexOrThrow(COLUMN_LATITUDE);
            int lngIndex = cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE);
            
            if (!cursor.isNull(latIndex)) {
                activity.setLatitude(cursor.getDouble(latIndex));
            }
            
            if (!cursor.isNull(lngIndex)) {
                activity.setLongitude(cursor.getDouble(lngIndex));
            }
        } catch (IllegalArgumentException e) {
            // Las columnas de ubicación podrían no existir en versiones anteriores de la base de datos
            Log.w(TAG, "Las columnas de ubicación no están disponibles en esta versión de la base de datos");
        }
        
        return activity;
    }
    
    public long addActivity(PhysicalActivity activity) {
        // TODO: Implement actual database insertion
        // For now, return a dummy ID
        return System.currentTimeMillis();
    }
    
    /**
     * Obtiene las actividades más recientes de un usuario, limitadas por cantidad
     * @param userId ID del usuario
     * @param limit Número máximo de actividades a devolver
     * @return Lista de actividades físicas ordenadas por fecha, más recientes primero
     */
    public List<PhysicalActivity> getRecentActivities(long userId, int limit) {
        List<PhysicalActivity> activityList = new ArrayList<>();
        
        String query = "SELECT * FROM " + TABLE_ACTIVITIES + 
                      " WHERE " + COLUMN_USER_ID + " = ?" +
                      " ORDER BY " + COLUMN_DATE + " DESC" +
                      " LIMIT " + limit;
                      
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            do {
                PhysicalActivity activity = cursorToActivity(cursor);
                activityList.add(activity);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return activityList;
    }
    
    /**
     * Obtiene las actividades de un usuario para una fecha específica
     * @param userId ID del usuario
     * @param date Fecha en formato YYYY-MM-DD
     * @return Lista de actividades realizadas en esa fecha
     */
    public List<PhysicalActivity> getActivitiesByDate(long userId, String date) {
        List<PhysicalActivity> activityList = new ArrayList<>();
        
        String query = "SELECT * FROM " + TABLE_ACTIVITIES + 
                      " WHERE " + COLUMN_USER_ID + " = ?" +
                      " AND " + COLUMN_DATE + " = ?" +
                      " ORDER BY " + COLUMN_ID + " DESC";
                      
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), date});
        
        if (cursor.moveToFirst()) {
            do {
                PhysicalActivity activity = cursorToActivity(cursor);
                activityList.add(activity);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return activityList;
    }
} 