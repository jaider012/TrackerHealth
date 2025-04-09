package com.example.trackerhealth.model;

import java.util.Date;

public class Meal {
    private long id;
    private long userId;
    private String name;
    private String mealType; // Breakfast, Lunch, Dinner, Snack
    private int calories;
    private double proteins; // in grams
    private double carbs; // in grams
    private double fats; // in grams
    private String date;
    private String time;
    private String notes;
    private String photoPath;

    // Constructor vacío
    public Meal() {
    }

    // Constructor completo
    public Meal(long id, long userId, String name, String mealType, int calories, 
                double proteins, double carbs, double fats, 
                String date, String time, String notes, String photoPath) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.mealType = mealType;
        this.calories = calories;
        this.proteins = proteins;
        this.carbs = carbs;
        this.fats = fats;
        this.date = date;
        this.time = time;
        this.notes = notes;
        this.photoPath = photoPath;
    }

    // Constructor para crear una nueva comida (sin ID)
    public Meal(long userId, String name, String mealType, int calories, 
              double proteins, double carbs, double fats, 
              String date, String time, String notes) {
        this.userId = userId;
        this.name = name;
        this.mealType = mealType;
        this.calories = calories;
        this.proteins = proteins;
        this.carbs = carbs;
        this.fats = fats;
        this.date = date;
        this.time = time;
        this.notes = notes;
    }

    // Getters y setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public double getProteins() {
        return proteins;
    }

    public void setProteins(double proteins) {
        this.proteins = proteins;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public double getFats() {
        return fats;
    }

    public void setFats(double fats) {
        this.fats = fats;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    @Override
    public String toString() {
        return "Meal{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mealType='" + mealType + '\'' +
                ", calories=" + calories +
                ", date='" + date + '\'' +
                '}';
    }
} 