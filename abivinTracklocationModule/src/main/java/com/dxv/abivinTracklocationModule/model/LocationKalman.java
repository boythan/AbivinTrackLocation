package com.dxv.abivinTracklocationModule.model;

import android.location.Location;

public class LocationKalman {
    Location location;
    float variance;

    public LocationKalman(Location location) {
        this.location = location;
    }

    public LocationKalman(Location location, float variance) {
        this.location = location;
        this.variance = variance;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public float getVariance() {
        return variance;
    }

    public void setVariance(float variance) {
        this.variance = variance;
    }
}
