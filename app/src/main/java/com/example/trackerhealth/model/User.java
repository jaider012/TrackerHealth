package com.example.trackerhealth.model;

public class User {
    private long id;
    private String name;
    private String email;
    private String password;
    private double height;
    private double weight;
    private int age;
    private String gender;
    private String createdAt;

    // Constructor vacío requerido para algunas operaciones
    public User() {
    }

    // Constructor para crear un nuevo usuario (sin ID, se generará en la base de datos)
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
    
    // Constructor completo para cargar un usuario desde la base de datos
    public User(long id, String name, String email, String password, double height, double weight, int age, String gender, String createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.height = height;
        this.weight = weight;
        this.age = age;
        this.gender = gender;
        this.createdAt = createdAt;
    }

    // Getters y setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    // Método para calcular el IMC (Índice de Masa Corporal)
    public double calculateBMI() {
        if (height <= 0) return 0;
        // El IMC se calcula como peso(kg) / altura(m)²
        double heightInMeters = height / 100; // convertir cm a metros
        return weight / (heightInMeters * heightInMeters);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
} 