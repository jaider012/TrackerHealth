package com.example.trackerhealth.model;

import java.util.Date;

public class PhysicalActivity {
    private long id;
    private long userId;
    private String activityType;
    private int duration; // en minutos
    private int caloriesBurned;
    private double distance; // en kilómetros
    private String date;
    private String notes;
    private String photoPath; // Ruta de la imagen

    // Constructor vacío
    public PhysicalActivity() {
    }

    // Constructor para crear una nueva actividad física (sin ID)
    public PhysicalActivity(long userId, String activityType, int duration, int caloriesBurned, double distance, String notes) {
        this.userId = userId;
        this.activityType = activityType;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
        this.distance = distance;
        this.notes = notes;
    }

    // Constructor completo
    public PhysicalActivity(long id, long userId, String activityType, int duration, int caloriesBurned, double distance, String date, String notes, String photoPath) {
        this.id = id;
        this.userId = userId;
        this.activityType = activityType;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
        this.distance = distance;
        this.date = date;
        this.notes = notes;
        this.photoPath = photoPath;
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

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    // Métodos útiles
    public double getPace() {
        if (distance > 0 && duration > 0) {
            // Ritmo en minutos por kilómetro
            return duration / distance;
        }
        return 0;
    }

    public double getSpeed() {
        if (duration > 0) {
            // Velocidad en km/h
            return (distance / duration) * 60;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "PhysicalActivity{" +
                "id=" + id +
                ", activityType='" + activityType + '\'' +
                ", duration=" + duration +
                ", date='" + date + '\'' +
                '}';
    }
} 