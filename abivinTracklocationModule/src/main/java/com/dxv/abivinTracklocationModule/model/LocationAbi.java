package com.dxv.abivinTracklocationModule.model;

import android.location.Location;

import androidx.annotation.Keep;
//import android.support.annotation.Keep;

@Keep
public class LocationAbi {
    private long id;
    private Float accuracy;
    private Double latitude;
    private Double longitude;
    private String provider;
    private Float speed;
    private long time;

    public LocationAbi(long id, Double longitude, Double latitude, Float speed, long time, Float accuracy, String provider) {
        this.id = id;
        this.accuracy = accuracy;
        this.latitude = latitude;
        this.longitude = longitude;
        this.provider = provider;
        this.speed = speed;
        this.time = time;
    }

    public LocationAbi(Location location) {
        this.accuracy = location.getAccuracy();
        this.latitude = location.getLatitude();

        this.longitude = location.getLongitude();
        this.provider = location.getProvider();
        this.speed = location.getSpeed() * 3.6f;
        this.time = location.getTime();
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
