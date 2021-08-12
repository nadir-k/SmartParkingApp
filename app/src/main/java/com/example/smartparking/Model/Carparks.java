package com.example.smartparking.Model;

public class Carparks {
    int id;
    double latitude;
    double longitude;
    String name;

    public Carparks(){}

    public Carparks(double latitude, double longitude, String name) {

        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public Carparks(int id, double latitude, double longitude, String name) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
