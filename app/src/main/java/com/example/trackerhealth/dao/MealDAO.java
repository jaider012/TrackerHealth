package com.example.trackerhealth.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.trackerhealth.database.DatabaseHelper;
import com.example.trackerhealth.model.Meal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealDAO {
    
    private final DatabaseHelper dbHelper;
    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat timeFormat;
    
    // Table and column names
    private static final String TABLE_MEALS = DatabaseHelper.TABLE_MEALS;
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_MEAL_TYPE = "meal_type";
    private static final String COLUMN_CALORIES = "calories";
    private static final String COLUMN_PROTEINS = "proteins";
    private static final String COLUMN_CARBS = "carbs";
    private static final String COLUMN_FATS = "fats";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_NOTES = "notes";
    private static final String COLUMN_PHOTO_PATH = "photo_path";
    
    public MealDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    /**
     * Adds a new meal to the database
     * @param meal Meal to add
     * @return ID of the new meal, or -1 if insertion failed
     */
    public long addMeal(Meal meal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, meal.getUserId());
        values.put(COLUMN_NAME, meal.getName());
        values.put(COLUMN_MEAL_TYPE, meal.getMealType());
        values.put(COLUMN_CALORIES, meal.getCalories());
        values.put(COLUMN_PROTEINS, meal.getProteins());
        values.put(COLUMN_CARBS, meal.getCarbs());
        values.put(COLUMN_FATS, meal.getFats());
        
        // If date is not provided, use current date
        if (meal.getDate() == null || meal.getDate().isEmpty()) {
            values.put(COLUMN_DATE, dateFormat.format(new Date()));
        } else {
            values.put(COLUMN_DATE, meal.getDate());
        }
        
        // If time is not provided, use current time
        if (meal.getTime() == null || meal.getTime().isEmpty()) {
            values.put(COLUMN_TIME, timeFormat.format(new Date()));
        } else {
            values.put(COLUMN_TIME, meal.getTime());
        }
        
        values.put(COLUMN_NOTES, meal.getNotes());
        values.put(COLUMN_PHOTO_PATH, meal.getPhotoPath());
        
        long id = db.insert(TABLE_MEALS, null, values);
        db.close();
        
        return id;
    }
    
    /**
     * Get all meals for a specific user
     * @param userId ID of the user
     * @return List of meals
     */
    public List<Meal> getMealsByUserId(long userId) {
        List<Meal> mealList = new ArrayList<>();
        
        String selectQuery = "SELECT * FROM " + TABLE_MEALS + 
                             " WHERE " + COLUMN_USER_ID + " = ?" +
                             " ORDER BY " + COLUMN_DATE + " DESC, " +
                             COLUMN_TIME + " DESC";
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(userId) });
        
        if (cursor.moveToFirst()) {
            do {
                Meal meal = cursorToMeal(cursor);
                mealList.add(meal);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return mealList;
    }
    
    /**
     * Get all meals for a specific date
     * @param userId ID of the user
     * @param date Date in format yyyy-MM-dd
     * @return List of meals
     */
    public List<Meal> getMealsByDate(long userId, String date) {
        List<Meal> mealList = new ArrayList<>();
        
        String selectQuery = "SELECT * FROM " + TABLE_MEALS + 
                             " WHERE " + COLUMN_USER_ID + " = ?" +
                             " AND " + COLUMN_DATE + " = ?" +
                             " ORDER BY " + COLUMN_TIME + " ASC";
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(userId), date });
        
        if (cursor.moveToFirst()) {
            do {
                Meal meal = cursorToMeal(cursor);
                mealList.add(meal);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return mealList;
    }
    
    /**
     * Get all meals for today
     * @param userId ID of the user
     * @return List of meals
     */
    public List<Meal> getTodaysMeals(long userId) {
        String today = dateFormat.format(new Date());
        return getMealsByDate(userId, today);
    }
    
    /**
     * Get a meal by its ID
     * @param id ID of the meal
     * @return Meal object or null if not found
     */
    public Meal getMealById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_MEALS, null, COLUMN_ID + " = ?",
                new String[] { String.valueOf(id) }, null, null, null);
        
        Meal meal = null;
        if (cursor.moveToFirst()) {
            meal = cursorToMeal(cursor);
        }
        
        cursor.close();
        db.close();
        
        return meal;
    }
    
    /**
     * Update an existing meal
     * @param meal Meal to update
     * @return Number of rows affected
     */
    public int updateMeal(Meal meal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, meal.getName());
        values.put(COLUMN_MEAL_TYPE, meal.getMealType());
        values.put(COLUMN_CALORIES, meal.getCalories());
        values.put(COLUMN_PROTEINS, meal.getProteins());
        values.put(COLUMN_CARBS, meal.getCarbs());
        values.put(COLUMN_FATS, meal.getFats());
        values.put(COLUMN_DATE, meal.getDate());
        values.put(COLUMN_TIME, meal.getTime());
        values.put(COLUMN_NOTES, meal.getNotes());
        values.put(COLUMN_PHOTO_PATH, meal.getPhotoPath());
        
        int rowsAffected = db.update(TABLE_MEALS, values, COLUMN_ID + " = ?",
                new String[] { String.valueOf(meal.getId()) });
        
        db.close();
        
        return rowsAffected;
    }
    
    /**
     * Delete a meal
     * @param id ID of the meal to delete
     * @return Number of rows affected
     */
    public int deleteMeal(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        int rowsAffected = db.delete(TABLE_MEALS, COLUMN_ID + " = ?",
                new String[] { String.valueOf(id) });
        
        db.close();
        
        return rowsAffected;
    }
    
    /**
     * Convert cursor to meal object
     */
    private Meal cursorToMeal(Cursor cursor) {
        Meal meal = new Meal();
        
        meal.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        meal.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
        meal.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
        meal.setMealType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_TYPE)));
        meal.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)));
        meal.setProteins(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROTEINS)));
        meal.setCarbs(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CARBS)));
        meal.setFats(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FATS)));
        meal.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
        meal.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)));
        meal.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)));
        meal.setPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_PATH)));
        
        return meal;
    }
} 