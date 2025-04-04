package com.example.trackerhealth.util;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.example.trackerhealth.model.PhysicalActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utilidad para el manejo de rutas GPS, guardado y carga de trayectos
 */
public class LocationTrackingUtil {
    
    private static final String TAG = "LocationTrackingUtil";
    private static final String ROUTES_DIR = "routes";
    
    /**
     * Guarda una lista de ubicaciones como un archivo JSON para una actividad específica
     * 
     * @param context El contexto para acceder al almacenamiento
     * @param activityId El ID de la actividad física asociada
     * @param locations Lista de ubicaciones recogidas durante el tracking
     * @return La ruta del archivo guardado o null si ocurre un error
     */
    public static String saveRouteData(Context context, long activityId, List<Location> locations) {
        if (locations == null || locations.isEmpty()) {
            return null;
        }
        
        String fileName = "route_" + activityId + "_" + UUID.randomUUID().toString().substring(0, 8) + ".json";
        
        try {
            // Crear directorio si no existe
            File routesDir = new File(context.getFilesDir(), ROUTES_DIR);
            if (!routesDir.exists()) {
                routesDir.mkdirs();
            }
            
            // Crear el archivo de ruta
            File routeFile = new File(routesDir, fileName);
            
            // Crear el objeto JSON con los datos de la ruta
            JSONObject routeJson = new JSONObject();
            routeJson.put("activity_id", activityId);
            routeJson.put("timestamp", System.currentTimeMillis());
            
            JSONArray pointsArray = new JSONArray();
            for (Location location : locations) {
                JSONObject pointJson = new JSONObject();
                pointJson.put("lat", location.getLatitude());
                pointJson.put("lng", location.getLongitude());
                pointJson.put("time", location.getTime());
                pointJson.put("speed", location.hasSpeed() ? location.getSpeed() : 0);
                pointJson.put("altitude", location.hasAltitude() ? location.getAltitude() : 0);
                pointsArray.put(pointJson);
            }
            
            routeJson.put("points", pointsArray);
            
            // Escribir al archivo
            FileOutputStream fos = new FileOutputStream(routeFile);
            fos.write(routeJson.toString().getBytes());
            fos.close();
            
            return routeFile.getAbsolutePath();
            
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error al guardar datos de ruta: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Carga los datos de ruta para una actividad específica
     * 
     * @param context El contexto para acceder al almacenamiento
     * @param activityId El ID de la actividad física
     * @return Lista de objetos Location con los puntos de la ruta o null si no existe
     */
    public static List<Location> loadRouteData(Context context, long activityId) {
        File routesDir = new File(context.getFilesDir(), ROUTES_DIR);
        if (!routesDir.exists()) {
            return null;
        }
        
        // Buscar archivos que comiencen con el prefijo de la actividad
        File[] files = routesDir.listFiles((dir, name) -> name.startsWith("route_" + activityId + "_"));
        
        if (files == null || files.length == 0) {
            return null;
        }
        
        // Usar el primer archivo encontrado
        File routeFile = files[0];
        
        try {
            // Leer el contenido del archivo
            FileInputStream fis = new FileInputStream(routeFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            reader.close();
            fis.close();
            
            // Parsear el JSON
            JSONObject routeJson = new JSONObject(sb.toString());
            JSONArray pointsArray = routeJson.getJSONArray("points");
            
            List<Location> locations = new ArrayList<>();
            
            for (int i = 0; i < pointsArray.length(); i++) {
                JSONObject pointJson = pointsArray.getJSONObject(i);
                
                Location location = new Location("file");
                location.setLatitude(pointJson.getDouble("lat"));
                location.setLongitude(pointJson.getDouble("lng"));
                location.setTime(pointJson.getLong("time"));
                
                if (pointJson.has("speed")) {
                    location.setSpeed((float) pointJson.getDouble("speed"));
                }
                
                if (pointJson.has("altitude")) {
                    location.setAltitude(pointJson.getDouble("altitude"));
                }
                
                locations.add(location);
            }
            
            return locations;
            
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error al cargar datos de ruta: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene la lista de actividades que tienen archivos de ruta guardados
     * 
     * @param context El contexto para acceder al almacenamiento
     * @return Una lista de IDs de actividades que tienen datos de ruta
     */
    public static List<Long> getActivitiesWithRoutes(Context context) {
        File routesDir = new File(context.getFilesDir(), ROUTES_DIR);
        if (!routesDir.exists()) {
            return new ArrayList<>();
        }
        
        File[] files = routesDir.listFiles((dir, name) -> name.startsWith("route_"));
        
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }
        
        List<Long> activityIds = new ArrayList<>();
        
        for (File file : files) {
            try {
                // Extraer ID de actividad del nombre de archivo (route_ID_UUID.json)
                String fileName = file.getName();
                String[] parts = fileName.split("_");
                
                if (parts.length >= 2) {
                    long activityId = Long.parseLong(parts[1]);
                    if (!activityIds.contains(activityId)) {
                        activityIds.add(activityId);
                    }
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error al parsear ID de actividad: " + e.getMessage());
            }
        }
        
        return activityIds;
    }
    
    /**
     * Calcula la distancia total recorrida por una lista de ubicaciones
     * 
     * @param locations Lista de ubicaciones
     * @return Distancia total en kilómetros
     */
    public static double calculateTotalDistance(List<Location> locations) {
        if (locations == null || locations.size() < 2) {
            return 0;
        }
        
        float totalDistance = 0;
        Location prevLocation = null;
        
        for (Location location : locations) {
            if (prevLocation != null) {
                totalDistance += prevLocation.distanceTo(location);
            }
            prevLocation = location;
        }
        
        // Convertir a kilómetros
        return totalDistance / 1000.0;
    }
    
    /**
     * Elimina los datos de ruta para una actividad específica
     * 
     * @param context El contexto para acceder al almacenamiento
     * @param activityId El ID de la actividad física
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public static boolean deleteRouteData(Context context, long activityId) {
        File routesDir = new File(context.getFilesDir(), ROUTES_DIR);
        if (!routesDir.exists()) {
            return false;
        }
        
        File[] files = routesDir.listFiles((dir, name) -> name.startsWith("route_" + activityId + "_"));
        
        if (files == null || files.length == 0) {
            return false;
        }
        
        boolean success = true;
        
        for (File file : files) {
            if (!file.delete()) {
                success = false;
            }
        }
        
        return success;
    }
} 