package com.example.smartparking.Model;

public class Preference {
    int id, lowestFloor, disabledSpace;

    public Preference(){}

    public Preference(int lowestFloor, int disabledSpace) {
        this.lowestFloor = lowestFloor;
        this.disabledSpace = disabledSpace;
    }

    public Preference(int id, int lowestFloor, int disabledSpace) {
        this.id = id;
        this.lowestFloor = lowestFloor;
        this.disabledSpace = disabledSpace;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLowestFloor() {
        return lowestFloor;
    }

    public void setLowestFloor(int lowestFloor) {
        this.lowestFloor = lowestFloor;
    }

    public int getDisabledSpace() {
        return disabledSpace;
    }

    public void setDisabledSpace(int disabledSpace) {
        this.disabledSpace = disabledSpace;
    }
}
