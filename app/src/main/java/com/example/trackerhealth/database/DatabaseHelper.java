package com.example.trackerhealth.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "TrackerHealth.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_PHYSICAL_ACTIVITIES = "physical_activities";
    public static final String TABLE_FOOD_ENTRIES = "food_entries";
    public static final String TABLE_WATER_INTAKE = "water_intake";
    public static final String TABLE_SLEEP_RECORDS = "sleep_records";

    // User Table Columns
    public static final String KEY_USER_ID = "id";
    public static final String KEY_USER_NAME = "name";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_PASSWORD = "password";
    public static final String KEY_USER_HEIGHT = "height";
    public static final String KEY_USER_WEIGHT = "weight";
    public static final String KEY_USER_AGE = "age";
    public static final String KEY_USER_GENDER = "gender";
    public static final String KEY_USER_CREATED_AT = "created_at";

    // Physical Activity Table Columns
    public static final String KEY_ACTIVITY_ID = "id";
    public static final String KEY_ACTIVITY_USER_ID_FK = "user_id";
    public static final String KEY_ACTIVITY_TYPE = "activity_type";
    public static final String KEY_ACTIVITY_DURATION = "duration";
    public static final String KEY_ACTIVITY_CALORIES = "calories_burned";
    public static final String KEY_ACTIVITY_DISTANCE = "distance";
    public static final String KEY_ACTIVITY_DATE = "date";
    public static final String KEY_ACTIVITY_NOTES = "notes";

    // Food Entry Table Columns
    public static final String KEY_FOOD_ID = "id";
    public static final String KEY_FOOD_USER_ID_FK = "user_id";
    public static final String KEY_FOOD_NAME = "food_name";
    public static final String KEY_FOOD_MEAL_TYPE = "meal_type";
    public static final String KEY_FOOD_CALORIES = "calories";
    public static final String KEY_FOOD_PROTEIN = "protein";
    public static final String KEY_FOOD_CARBS = "carbs";
    public static final String KEY_FOOD_FATS = "fats";
    public static final String KEY_FOOD_DATE = "date";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS +
                "(" +
                KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_USER_NAME + " TEXT," +
                KEY_USER_EMAIL + " TEXT UNIQUE," +
                KEY_USER_PASSWORD + " TEXT," +
                KEY_USER_HEIGHT + " REAL," +
                KEY_USER_WEIGHT + " REAL," +
                KEY_USER_AGE + " INTEGER," +
                KEY_USER_GENDER + " TEXT," +
                KEY_USER_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";

        String CREATE_PHYSICAL_ACTIVITIES_TABLE = "CREATE TABLE " + TABLE_PHYSICAL_ACTIVITIES +
                "(" +
                KEY_ACTIVITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_ACTIVITY_USER_ID_FK + " INTEGER REFERENCES " + TABLE_USERS + "," +
                KEY_ACTIVITY_TYPE + " TEXT," +
                KEY_ACTIVITY_DURATION + " INTEGER," +
                KEY_ACTIVITY_CALORIES + " INTEGER," +
                KEY_ACTIVITY_DISTANCE + " REAL," +
                KEY_ACTIVITY_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                KEY_ACTIVITY_NOTES + " TEXT" +
                ")";

        String CREATE_FOOD_ENTRIES_TABLE = "CREATE TABLE " + TABLE_FOOD_ENTRIES +
                "(" +
                KEY_FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_FOOD_USER_ID_FK + " INTEGER REFERENCES " + TABLE_USERS + "," +
                KEY_FOOD_NAME + " TEXT," +
                KEY_FOOD_MEAL_TYPE + " TEXT," +
                KEY_FOOD_CALORIES + " INTEGER," +
                KEY_FOOD_PROTEIN + " REAL," +
                KEY_FOOD_CARBS + " REAL," +
                KEY_FOOD_FATS + " REAL," +
                KEY_FOOD_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_PHYSICAL_ACTIVITIES_TABLE);
        db.execSQL(CREATE_FOOD_ENTRIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Drop older tables if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD_ENTRIES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHYSICAL_ACTIVITIES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            
            // Create tables again
            onCreate(db);
        }
    }
} 