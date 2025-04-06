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
    private DatabaseHelper dbHelper;
    private Context context;
    
    public PhysicalActivityDAO(Context context) {
        this.context = context;
        dbHelper = DatabaseHelper.getInstance(context);
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
            values.put(DatabaseHelper.KEY_ACTIVITY_USER_ID_FK, activity.getUserId());
            values.put(DatabaseHelper.KEY_ACTIVITY_TYPE, activity.getActivityType());
            values.put(DatabaseHelper.KEY_ACTIVITY_DURATION, activity.getDuration());
            values.put(DatabaseHelper.KEY_ACTIVITY_CALORIES, activity.getCaloriesBurned());
            values.put(DatabaseHelper.KEY_ACTIVITY_DISTANCE, activity.getDistance());
            values.put(DatabaseHelper.KEY_ACTIVITY_NOTES, activity.getNotes());
            values.put(DatabaseHelper.KEY_ACTIVITY_LATITUDE, activity.getLatitude());
            values.put(DatabaseHelper.KEY_ACTIVITY_LONGITUDE, activity.getLongitude());
            
            // Insertar la fila
            activityId = db.insertOrThrow(DatabaseHelper.TABLE_PHYSICAL_ACTIVITIES, null, values);
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
            values.put(DatabaseHelper.KEY_ACTIVITY_TYPE, activity.getActivityType());
            values.put(DatabaseHelper.KEY_ACTIVITY_DURATION, activity.getDuration());
            values.put(DatabaseHelper.KEY_ACTIVITY_CALORIES, activity.getCaloriesBurned());
            values.put(DatabaseHelper.KEY_ACTIVITY_DISTANCE, activity.getDistance());
            values.put(DatabaseHelper.KEY_ACTIVITY_NOTES, activity.getNotes());
            values.put(DatabaseHelper.KEY_ACTIVITY_LATITUDE, activity.getLatitude());
            values.put(DatabaseHelper.KEY_ACTIVITY_LONGITUDE, activity.getLongitude());
            
            // Actualizar la fila
            String selection = DatabaseHelper.KEY_ACTIVITY_ID + " = ?";
            String[] selectionArgs = { String.valueOf(activity.getId()) };
            
            rows = db.update(DatabaseHelper.TABLE_PHYSICAL_ACTIVITIES, values, selection, selectionArgs);
            
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
            String selection = DatabaseHelper.KEY_ACTIVITY_ID + " = ?";
            String[] selectionArgs = { String.valueOf(activityId) };
            
            rows = db.delete(DatabaseHelper.TABLE_PHYSICAL_ACTIVITIES, selection, selectionArgs);
            
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
        // TODO: Implement actual database query
        // For now, return dummy data
        PhysicalActivity activity = new PhysicalActivity();
        activity.setId(activityId);
        activity.setDistance(14.5f);
        activity.setCaloriesBurned(110);
        activity.setNotes("heartRate:95");
        return activity;
    }
    
    /**
     * Obtiene todas las actividades físicas de un usuario
     *
     * @param userId El ID del usuario
     * @return Lista de actividades físicas del usuario
     */
    public List<PhysicalActivity> getActivitiesByUserId(long userId) {
        String SELECT_QUERY = "SELECT * FROM " + DatabaseHelper.TABLE_PHYSICAL_ACTIVITIES +
                " WHERE " + DatabaseHelper.KEY_ACTIVITY_USER_ID_FK + " = ?" +
                " ORDER BY " + DatabaseHelper.KEY_ACTIVITY_DATE + " DESC";
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<PhysicalActivity> activities = new ArrayList<>();
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(SELECT_QUERY, new String[]{String.valueOf(userId)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    PhysicalActivity activity = getActivityFromCursor(cursor);
                    activities.add(activity);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener actividades físicas por usuario: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return activities;
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
        String SELECT_QUERY = "SELECT * FROM " + DatabaseHelper.TABLE_PHYSICAL_ACTIVITIES +
                " WHERE " + DatabaseHelper.KEY_ACTIVITY_USER_ID_FK + " = ?" +
                " AND " + DatabaseHelper.KEY_ACTIVITY_DATE + " BETWEEN ? AND ?" +
                " ORDER BY " + DatabaseHelper.KEY_ACTIVITY_DATE + " DESC";
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<PhysicalActivity> activities = new ArrayList<>();
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(SELECT_QUERY, new String[]{
                    String.valueOf(userId), startDate, endDate
            });
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    PhysicalActivity activity = getActivityFromCursor(cursor);
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
        String SELECT_QUERY = "SELECT SUM(" + DatabaseHelper.KEY_ACTIVITY_CALORIES + ") as total" +
                " FROM " + DatabaseHelper.TABLE_PHYSICAL_ACTIVITIES +
                " WHERE " + DatabaseHelper.KEY_ACTIVITY_USER_ID_FK + " = ?" +
                " AND " + DatabaseHelper.KEY_ACTIVITY_DATE + " >= date('now', '-" + days + " days')";
        
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
        String SELECT_QUERY = "SELECT * FROM " + DatabaseHelper.TABLE_PHYSICAL_ACTIVITIES +
                " WHERE " + DatabaseHelper.KEY_ACTIVITY_USER_ID_FK + " = ?" +
                " AND " + DatabaseHelper.KEY_ACTIVITY_LATITUDE + " IS NOT NULL" +
                " AND " + DatabaseHelper.KEY_ACTIVITY_LONGITUDE + " IS NOT NULL" +
                " ORDER BY " + DatabaseHelper.KEY_ACTIVITY_DATE + " DESC";
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<PhysicalActivity> activities = new ArrayList<>();
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(SELECT_QUERY, new String[]{String.valueOf(userId)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    PhysicalActivity activity = getActivityFromCursor(cursor);
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
                DatabaseHelper.KEY_ACTIVITY_DISTANCE + ") as total_distance " +
                " FROM " + DatabaseHelper.TABLE_PHYSICAL_ACTIVITIES +
                " WHERE " + DatabaseHelper.KEY_ACTIVITY_USER_ID_FK + " = ?" +
                " AND " + DatabaseHelper.KEY_ACTIVITY_LATITUDE + " IS NOT NULL" +
                " AND " + DatabaseHelper.KEY_ACTIVITY_LONGITUDE + " IS NOT NULL";
        
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
    private PhysicalActivity getActivityFromCursor(Cursor cursor) {
        PhysicalActivity activity = new PhysicalActivity();
        
        activity.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ACTIVITY_ID)));
        activity.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ACTIVITY_USER_ID_FK)));
        activity.setActivityType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ACTIVITY_TYPE)));
        activity.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ACTIVITY_DURATION)));
        activity.setCaloriesBurned(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ACTIVITY_CALORIES)));
        activity.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ACTIVITY_DATE)));
        
        // Campos opcionales
        int distanceIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ACTIVITY_DISTANCE);
        int notesIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ACTIVITY_NOTES);
        
        if (!cursor.isNull(distanceIndex)) {
            activity.setDistance(cursor.getDouble(distanceIndex));
        }
        
        if (!cursor.isNull(notesIndex)) {
            activity.setNotes(cursor.getString(notesIndex));
        }
        
        // Campos de ubicación
        try {
            int latIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ACTIVITY_LATITUDE);
            int lngIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ACTIVITY_LONGITUDE);
            
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
} 