package com.example.smartparking.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Space implements Parcelable {
    private boolean available;
    private int carparkid;
    private boolean disabled;
    private int floornumber;
    private double latitude;
    private double longitude;

    public Space(){}

    public Space(boolean available, int carparkid, boolean disabled, int floornumber, double latitude, double longitude) {
        this.available = available;
        this.carparkid = carparkid;
        this.disabled = disabled;
        this.floornumber = floornumber;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected Space(Parcel in) {
        available = in.readByte() != 0;
        carparkid = in.readInt();
        disabled = in.readByte() != 0;
        floornumber = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<Space> CREATOR = new Creator<Space>() {
        @Override
        public Space createFromParcel(Parcel in) {
            return new Space(in);
        }

        @Override
        public Space[] newArray(int size) {
            return new Space[size];
        }
    };

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getCarparkid() {
        return carparkid;
    }

    public void setCarparkid(int carparkid) {
        this.carparkid = carparkid;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public int getFloornumber() {
        return floornumber;
    }

    public void setFloornumber(int floornumber) {
        this.floornumber = floornumber;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (available ? 1 : 0));
        dest.writeInt(carparkid);
        dest.writeByte((byte) (disabled ? 1 : 0));
        dest.writeInt(floornumber);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
