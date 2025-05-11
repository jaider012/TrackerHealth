package com.example.trackerhealth.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "TrackerHealth.db";
    private static final int DATABASE_VERSION = 4;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_PHYSICAL_ACTIVITIES = "physical_activities";
    public static final String TABLE_FOOD_ENTRIES = "food_entries";
    public static final String TABLE_WATER_INTAKE = "water_intake";
    public static final String TABLE_SLEEP_RECORDS = "sleep_records";
    public static final String TABLE_MEALS = "meals";

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
    public static final String KEY_ACTIVITY_LATITUDE = "latitude";
    public static final String KEY_ACTIVITY_LONGITUDE = "longitude";

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

    // Water Intake Table Columns
    public static final String KEY_WATER_ID = "id";
    public static final String KEY_WATER_USER_ID_FK = "user_id";
    public static final String KEY_WATER_AMOUNT = "amount";
    public static final String KEY_WATER_DATE_TIME = "date_time";

    // Sleep Records Table Columns
    public static final String KEY_SLEEP_ID = "id";
    public static final String KEY_SLEEP_USER_ID_FK = "user_id";
    public static final String KEY_SLEEP_START_TIME = "start_time";
    public static final String KEY_SLEEP_END_TIME = "end_time";
    public static final String KEY_SLEEP_DURATION = "duration";
    public static final String KEY_SLEEP_QUALITY = "quality";
    public static final String KEY_SLEEP_NOTES = "notes";
    public static final String KEY_SLEEP_DATE_RECORDED = "date_recorded";

    // Meals Table Columns
    public static final String KEY_MEAL_ID = "id";
    public static final String KEY_MEAL_USER_ID_FK = "user_id";
    public static final String KEY_MEAL_NAME = "name";
    public static final String KEY_MEAL_TYPE = "meal_type";
    public static final String KEY_MEAL_CALORIES = "calories";
    public static final String KEY_MEAL_PROTEINS = "proteins";
    public static final String KEY_MEAL_CARBS = "carbs";
    public static final String KEY_MEAL_FATS = "fats";
    public static final String KEY_MEAL_DATE = "date";
    public static final String KEY_MEAL_TIME = "time";
    public static final String KEY_MEAL_NOTES = "notes";
    public static final String KEY_MEAL_PHOTO_PATH = "photo_path";

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
                KEY_ACTIVITY_USER_ID_FK + " INTEGER," +
                KEY_ACTIVITY_TYPE + " TEXT," +
                KEY_ACTIVITY_DURATION + " INTEGER," +
                KEY_ACTIVITY_CALORIES + " INTEGER," +
                KEY_ACTIVITY_DISTANCE + " REAL," +
                KEY_ACTIVITY_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                KEY_ACTIVITY_NOTES + " TEXT," +
                KEY_ACTIVITY_LATITUDE + " REAL," +
                KEY_ACTIVITY_LONGITUDE + " REAL," +
                "FOREIGN KEY(" + KEY_ACTIVITY_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")" +
                ")";

        String CREATE_FOOD_ENTRIES_TABLE = "CREATE TABLE " + TABLE_FOOD_ENTRIES +
                "(" +
                KEY_FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_FOOD_USER_ID_FK + " INTEGER," +
                KEY_FOOD_NAME + " TEXT," +
                KEY_FOOD_MEAL_TYPE + " TEXT," +
                KEY_FOOD_CALORIES + " INTEGER," +
                KEY_FOOD_PROTEIN + " REAL," +
                KEY_FOOD_CARBS + " REAL," +
                KEY_FOOD_FATS + " REAL," +
                KEY_FOOD_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(" + KEY_FOOD_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")" +
                ")";

        String CREATE_MEALS_TABLE = "CREATE TABLE " + TABLE_MEALS +
                "(" +
                KEY_MEAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_MEAL_USER_ID_FK + " INTEGER," +
                KEY_MEAL_NAME + " TEXT," +
                KEY_MEAL_TYPE + " TEXT," +
                KEY_MEAL_CALORIES + " INTEGER," +
                KEY_MEAL_PROTEINS + " REAL," +
                KEY_MEAL_CARBS + " REAL," +
                KEY_MEAL_FATS + " REAL," +
                KEY_MEAL_DATE + " TEXT," +
                KEY_MEAL_TIME + " TEXT," +
                KEY_MEAL_NOTES + " TEXT," +
                KEY_MEAL_PHOTO_PATH + " TEXT," +
                "FOREIGN KEY(" + KEY_MEAL_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")" +
                ")";

        String CREATE_WATER_INTAKE_TABLE = "CREATE TABLE " + TABLE_WATER_INTAKE +
                "(" +
                KEY_WATER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_WATER_USER_ID_FK + " INTEGER," +
                KEY_WATER_AMOUNT + " REAL," +
                KEY_WATER_DATE_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(" + KEY_WATER_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")" +
                ")";

        String CREATE_SLEEP_RECORDS_TABLE = "CREATE TABLE " + TABLE_SLEEP_RECORDS +
                "(" +
                KEY_SLEEP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_SLEEP_USER_ID_FK + " INTEGER," +
                KEY_SLEEP_START_TIME + " DATETIME," +
                KEY_SLEEP_END_TIME + " DATETIME," +
                KEY_SLEEP_DURATION + " INTEGER," +
                KEY_SLEEP_QUALITY + " TEXT," +
                KEY_SLEEP_NOTES + " TEXT," +
                KEY_SLEEP_DATE_RECORDED + " DATE DEFAULT (date('now'))," +
                "FOREIGN KEY(" + KEY_SLEEP_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")" +
                ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_PHYSICAL_ACTIVITIES_TABLE);
        db.execSQL(CREATE_FOOD_ENTRIES_TABLE);
        db.execSQL(CREATE_MEALS_TABLE);
        db.execSQL(CREATE_WATER_INTAKE_TABLE);
        db.execSQL(CREATE_SLEEP_RECORDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add location columns to physical activities table
            db.execSQL("ALTER TABLE " + TABLE_PHYSICAL_ACTIVITIES +
                       " ADD COLUMN " + KEY_ACTIVITY_LATITUDE + " REAL;");
            db.execSQL("ALTER TABLE " + TABLE_PHYSICAL_ACTIVITIES +
                       " ADD COLUMN " + KEY_ACTIVITY_LONGITUDE + " REAL;");
        }

        if (oldVersion < 3) {
            // Create the meals table if upgrading from a version before 3
            String CREATE_MEALS_TABLE_V3 = "CREATE TABLE IF NOT EXISTS " + TABLE_MEALS +
                    "(" +
                    KEY_MEAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_MEAL_USER_ID_FK + " INTEGER," +
                    KEY_MEAL_NAME + " TEXT," +
                    KEY_MEAL_TYPE + " TEXT," +
                    KEY_MEAL_CALORIES + " INTEGER," +
                    KEY_MEAL_PROTEINS + " REAL," +
                    KEY_MEAL_CARBS + " REAL," +
                    KEY_MEAL_FATS + " REAL," +
                    KEY_MEAL_DATE + " TEXT," +
                    KEY_MEAL_TIME + " TEXT," +
                    KEY_MEAL_NOTES + " TEXT," +
                    KEY_MEAL_PHOTO_PATH + " TEXT," +
                    "FOREIGN KEY(" + KEY_MEAL_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")" +
                    ")";
            db.execSQL(CREATE_MEALS_TABLE_V3);
        }

        if (oldVersion < 4) {
            // Create Water Intake Table
            String CREATE_WATER_INTAKE_TABLE_V4 = "CREATE TABLE IF NOT EXISTS " + TABLE_WATER_INTAKE +
                    "(" +
                    KEY_WATER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_WATER_USER_ID_FK + " INTEGER," +
                    KEY_WATER_AMOUNT + " REAL," +
                    KEY_WATER_DATE_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(" + KEY_WATER_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")" +
                    ")";
            db.execSQL(CREATE_WATER_INTAKE_TABLE_V4);

            // Create Sleep Records Table
            String CREATE_SLEEP_RECORDS_TABLE_V4 = "CREATE TABLE IF NOT EXISTS " + TABLE_SLEEP_RECORDS +
                    "(" +
                    KEY_SLEEP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_SLEEP_USER_ID_FK + " INTEGER," +
                    KEY_SLEEP_START_TIME + " DATETIME," +
                    KEY_SLEEP_END_TIME + " DATETIME," +
                    KEY_SLEEP_DURATION + " INTEGER," +
                    KEY_SLEEP_QUALITY + " TEXT," +
                    KEY_SLEEP_NOTES + " TEXT," +
                    KEY_SLEEP_DATE_RECORDED + " DATE DEFAULT (date('now'))," +
                    "FOREIGN KEY(" + KEY_SLEEP_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")" +
                    ")";
            db.execSQL(CREATE_SLEEP_RECORDS_TABLE_V4);
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
} 