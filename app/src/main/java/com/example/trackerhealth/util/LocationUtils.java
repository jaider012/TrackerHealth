package com.example.trackerhealth.util;

import android.location.Location;
import android.util.Log;

/**
 * Clase de utilidades para operaciones relacionadas con la ubicación y el GPS
 */
public class LocationUtils {
    
    private static final double EARTH_RADIUS = 6371000; // Radio de la Tierra en metros
    
    /**
     * Calcula la distancia entre dos puntos usando la fórmula Haversine
     * Más preciso que Location.distanceTo() para largas distancias
     * 
     * @param lat1 Latitud del punto 1
     * @param lon1 Longitud del punto 1
     * @param lat2 Latitud del punto 2
     * @param lon2 Longitud del punto 2
     * @return Distancia en metros entre los dos puntos
     */
    public static float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convertir coordenadas a radianes
        double latRad1 = Math.toRadians(lat1);
        double lonRad1 = Math.toRadians(lon1);
        double latRad2 = Math.toRadians(lat2);
        double lonRad2 = Math.toRadians(lon2);
        
        // Diferencias
        double dLat = latRad2 - latRad1;
        double dLon = lonRad2 - lonRad1;
        
        // Fórmula Haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(latRad1) * Math.cos(latRad2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // Distancia en metros
        float distance = (float) (EARTH_RADIUS * c);
        
        // Log para depuración
        Log.d("LocationUtils", String.format(
                "Distance calculated: %.2f meters between [%.6f,%.6f] and [%.6f,%.6f]",
                distance, lat1, lon1, lat2, lon2));
        
        return distance;
    }
    
    /**
     * Estima la precisión de una ubicación basándose en varios factores
     * 
     * @param location Ubicación a evaluar
     * @return Puntuación de calidad (0-100) donde 100 es la mejor calidad
     */
    public static int estimateLocationQuality(Location location) {
        if (location == null) return 0;
        
        int qualityScore = 100;
        
        // Reducir puntuación basada en la precisión reportada
        if (location.hasAccuracy()) {
            float accuracy = location.getAccuracy();
            if (accuracy > 100) {
                qualityScore -= 50;
            } else if (accuracy > 50) {
                qualityScore -= 30;
            } else if (accuracy > 20) {
                qualityScore -= 15;
            } else if (accuracy > 10) {
                qualityScore -= 5;
            }
        } else {
            // Sin datos de precisión, reducir significativamente
            qualityScore -= 40;
        }
        
        // Reducir puntuación si la ubicación es antigua
        long ageMillis = System.currentTimeMillis() - location.getTime();
        if (ageMillis > 60000) { // Más de 1 minuto
            qualityScore -= 30;
        } else if (ageMillis > 30000) { // Más de 30 segundos
            qualityScore -= 15;
        } else if (ageMillis > 10000) { // Más de 10 segundos
            qualityScore -= 5;
        }
        
        // Verificar límites
        if (qualityScore < 0) qualityScore = 0;
        if (qualityScore > 100) qualityScore = 100;
        
        return qualityScore;
    }
    
    /**
     * Calcula la velocidad entre dos puntos GPS
     * 
     * @param location1 Primera ubicación
     * @param location2 Segunda ubicación
     * @return Velocidad en metros por segundo
     */
    public static float calculateSpeed(Location location1, Location location2) {
        if (location1 == null || location2 == null) return 0;
        
        // Calcular tiempo entre ubicaciones en segundos
        float timeDiffSeconds = (location2.getTime() - location1.getTime()) / 1000f;
        
        // Si el tiempo es cero o negativo, usar la velocidad reportada o retornar cero
        if (timeDiffSeconds <= 0) {
            return location2.hasSpeed() ? location2.getSpeed() : 0;
        }
        
        // Calcular distancia
        float distance = calculateDistance(
                location1.getLatitude(), location1.getLongitude(),
                location2.getLatitude(), location2.getLongitude());
        
        // Velocidad = distancia / tiempo
        return distance / timeDiffSeconds;
    }
} 