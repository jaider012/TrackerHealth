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
        this.dbHelper = DatabaseHelper.getInstance(context);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    }
    
    /**
     * Adds a new meal to the database
     * @param meal Meal to add
     * @return ID of the new meal, or -1 if insertion failed
     */
    public long insert(Meal meal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.KEY_MEAL_USER_ID_FK, meal.getUserId());
        values.put(DatabaseHelper.KEY_MEAL_NAME, meal.getName());
        values.put(DatabaseHelper.KEY_MEAL_TYPE, meal.getMealType());
        values.put(DatabaseHelper.KEY_MEAL_CALORIES, meal.getCalories());
        values.put(DatabaseHelper.KEY_MEAL_PROTEINS, meal.getProteins());
        values.put(DatabaseHelper.KEY_MEAL_CARBS, meal.getCarbs());
        values.put(DatabaseHelper.KEY_MEAL_FATS, meal.getFats());
        values.put(DatabaseHelper.KEY_MEAL_DATE, meal.getDate());
        values.put(DatabaseHelper.KEY_MEAL_TIME, meal.getTime());
        values.put(DatabaseHelper.KEY_MEAL_NOTES, meal.getNotes());
        values.put(DatabaseHelper.KEY_MEAL_PHOTO_PATH, meal.getPhotoPath());

        return db.insert(DatabaseHelper.TABLE_MEALS, null, values);
    }
    
    /**
     * Adds a new meal to the database (alias for insert)
     * @param meal Meal to add
     * @return ID of the new meal, or -1 if insertion failed
     */
    public long addMeal(Meal meal) {
        return insert(meal);
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
    public List<Meal> getMealsForDate(long userId, String date) {
        List<Meal> meals = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
            DatabaseHelper.KEY_MEAL_ID,
            DatabaseHelper.KEY_MEAL_USER_ID_FK,
            DatabaseHelper.KEY_MEAL_NAME,
            DatabaseHelper.KEY_MEAL_TYPE,
            DatabaseHelper.KEY_MEAL_CALORIES,
            DatabaseHelper.KEY_MEAL_PROTEINS,
            DatabaseHelper.KEY_MEAL_CARBS,
            DatabaseHelper.KEY_MEAL_FATS,
            DatabaseHelper.KEY_MEAL_DATE,
            DatabaseHelper.KEY_MEAL_TIME,
            DatabaseHelper.KEY_MEAL_NOTES,
            DatabaseHelper.KEY_MEAL_PHOTO_PATH
        };

        String selection = DatabaseHelper.KEY_MEAL_USER_ID_FK + " = ? AND " + 
                         DatabaseHelper.KEY_MEAL_DATE + " = ?";
        String[] selectionArgs = {String.valueOf(userId), date};
        String sortOrder = DatabaseHelper.KEY_MEAL_TIME + " DESC";

        Cursor cursor = db.query(
            DatabaseHelper.TABLE_MEALS,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        );

        while (cursor.moveToNext()) {
            Meal meal = new Meal();
            meal.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_ID)));
            meal.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_USER_ID_FK)));
            meal.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_NAME)));
            meal.setMealType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_TYPE)));
            meal.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_CALORIES)));
            meal.setProteins(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_PROTEINS)));
            meal.setCarbs(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_CARBS)));
            meal.setFats(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_FATS)));
            meal.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_DATE)));
            meal.setTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_TIME)));
            meal.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_NOTES)));
            meal.setPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_PHOTO_PATH)));
            
            meals.add(meal);
        }

        cursor.close();
        return meals;
    }
    
    /**
     * Get all meals for today
     * @param userId ID of the user
     * @return List of meals
     */
    public List<Meal> getTodaysMeals(long userId) {
        String today = dateFormat.format(new Date());
        return getMealsForDate(userId, today);
    }
    
    /**
     * Get a meal by its ID
     * @param id ID of the meal
     * @return Meal object or null if not found
     */
    public Meal getMealById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String[] projection = {
            DatabaseHelper.KEY_MEAL_ID,
            DatabaseHelper.KEY_MEAL_USER_ID_FK,
            DatabaseHelper.KEY_MEAL_NAME,
            DatabaseHelper.KEY_MEAL_TYPE,
            DatabaseHelper.KEY_MEAL_CALORIES,
            DatabaseHelper.KEY_MEAL_PROTEINS,
            DatabaseHelper.KEY_MEAL_CARBS,
            DatabaseHelper.KEY_MEAL_FATS,
            DatabaseHelper.KEY_MEAL_DATE,
            DatabaseHelper.KEY_MEAL_TIME,
            DatabaseHelper.KEY_MEAL_NOTES,
            DatabaseHelper.KEY_MEAL_PHOTO_PATH
        };

        String selection = DatabaseHelper.KEY_MEAL_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = db.query(
            DatabaseHelper.TABLE_MEALS,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        );

        Meal meal = null;
        if (cursor.moveToFirst()) {
            meal = new Meal();
            meal.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_ID)));
            meal.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_USER_ID_FK)));
            meal.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_NAME)));
            meal.setMealType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_TYPE)));
            meal.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_CALORIES)));
            meal.setProteins(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_PROTEINS)));
            meal.setCarbs(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_CARBS)));
            meal.setFats(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_FATS)));
            meal.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_DATE)));
            meal.setTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_TIME)));
            meal.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_NOTES)));
            meal.setPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_PHOTO_PATH)));
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
    public int update(Meal meal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.KEY_MEAL_NAME, meal.getName());
        values.put(DatabaseHelper.KEY_MEAL_TYPE, meal.getMealType());
        values.put(DatabaseHelper.KEY_MEAL_CALORIES, meal.getCalories());
        values.put(DatabaseHelper.KEY_MEAL_PROTEINS, meal.getProteins());
        values.put(DatabaseHelper.KEY_MEAL_CARBS, meal.getCarbs());
        values.put(DatabaseHelper.KEY_MEAL_FATS, meal.getFats());
        values.put(DatabaseHelper.KEY_MEAL_DATE, meal.getDate());
        values.put(DatabaseHelper.KEY_MEAL_TIME, meal.getTime());
        values.put(DatabaseHelper.KEY_MEAL_NOTES, meal.getNotes());
        values.put(DatabaseHelper.KEY_MEAL_PHOTO_PATH, meal.getPhotoPath());

        String selection = DatabaseHelper.KEY_MEAL_ID + " = ?";
        String[] selectionArgs = {String.valueOf(meal.getId())};

        return db.update(DatabaseHelper.TABLE_MEALS, values, selection, selectionArgs);
    }
    
    /**
     * Delete a meal
     * @param id ID of the meal to delete
     * @return Number of rows affected
     */
    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = DatabaseHelper.KEY_MEAL_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        return db.delete(DatabaseHelper.TABLE_MEALS, selection, selectionArgs);
    }
    
    /**
     * Convert cursor to meal object
     */
    private Meal cursorToMeal(Cursor cursor) {
        Meal meal = new Meal();
        
        meal.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_ID)));
        meal.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_USER_ID_FK)));
        meal.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_NAME)));
        meal.setMealType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_TYPE)));
        meal.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_CALORIES)));
        meal.setProteins(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_PROTEINS)));
        meal.setCarbs(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_CARBS)));
        meal.setFats(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_FATS)));
        meal.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_DATE)));
        meal.setTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_TIME)));
        meal.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_NOTES)));
        meal.setPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MEAL_PHOTO_PATH)));
        
        return meal;
    }

    /**
     * Get all meals for a user, ordered by date and time
     * @param userId ID of the user
     * @return List of all meals
     */
    public List<Meal> getAllMealsByUser(long userId) {
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
} 