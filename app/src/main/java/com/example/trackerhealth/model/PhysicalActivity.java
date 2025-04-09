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
    private double latitude; // Coordenada de latitud
    private double longitude; // Coordenada de longitud

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
    public PhysicalActivity(long id, long userId, String activityType, int duration, int caloriesBurned, double distance, String date, String notes, String photoPath, double latitude, double longitude) {
        this.id = id;
        this.userId = userId;
        this.activityType = activityType;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
        this.distance = distance;
        this.date = date;
        this.notes = notes;
        this.photoPath = photoPath;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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
    
    /**
     * Verifica si la actividad tiene datos de ubicación GPS
     * @return true si la actividad tiene coordenadas válidas
     */
    public boolean hasLocationData() {
        return latitude != 0 && longitude != 0;
    }
    
    /**
     * Devuelve una cadena formateada con las coordenadas GPS
     * @return Cadena con formato "latitud,longitud"
     */
    public String getLocationString() {
        if (hasLocationData()) {
            return String.format("%.6f,%.6f", latitude, longitude);
        }
        return "";
    }
    
    /**
     * Extrae información específica de las notas
     * @param key La clave a buscar en las notas (ejemplo: "totalDistance")
     * @return El valor correspondiente a la clave o cadena vacía si no se encuentra
     */
    public String getInfoFromNotes(String key) {
        if (notes == null || notes.isEmpty() || key == null || key.isEmpty()) {
            return "";
        }
        
        String[] pairs = notes.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2 && keyValue[0].trim().equals(key)) {
                return keyValue[1].trim();
            }
        }
        return "";
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