package com.dxv.abivinTracklocationModule.kalman;


import android.location.Location;

import com.dxv.abivinTracklocationModule.model.LocationKalman;
import com.dxv.abivinTracklocationModule.uitls.SmartLog;

import java.util.ArrayList;

public class KalmanAlgorithm {
    private static final int KALMAN_CONST = 500;

    private LocationKalman lastLocation;
    private ArrayList<Location> rawData;

    public KalmanAlgorithm(ArrayList<Location> rawData) {
        this.rawData = rawData;
    }

    public ArrayList<Location> getRawData() {
        return rawData;
    }

    public void setRawData(ArrayList<Location> rawData) {
        this.rawData = rawData;
    }

    public LocationKalman getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(LocationKalman lastLocation) {
        this.lastLocation = lastLocation;
    }

    private static Double _calculateGreatCircleDistance(Location one, Location two) {
        int R = 6371000;
        Double dLat = toRad(two.getLatitude() - one.getLatitude());
        Double dLon = toRad(two.getLongitude() - one.getLongitude());
        Double lat1 = toRad(one.getLatitude());
        Double lat2 = toRad(two.getLatitude());
        Double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Double d = R * c;
        return d;
    }

    private static double toRad(Double d) {
        return d * Math.PI / 180;
    }

    private LocationKalman kalman(LocationKalman location, LocationKalman lastLocation, int constant) {
        float accuracy = Math.max(location.getLocation().getAccuracy(), 1);
        LocationKalman result;
        if (lastLocation == null) {
            result = new LocationKalman(location.getLocation(), accuracy * accuracy);
        } else {
            result = new LocationKalman(lastLocation.getLocation());

        }


        if (lastLocation != null) {

            long timestampInc = location.getLocation().getTime() - lastLocation.getLocation().getTime();

            if (timestampInc > 0) {
                // We can tune the velocity and particularly the coefficient at the end
                double velocity = _calculateGreatCircleDistance(location.getLocation(), lastLocation.getLocation()) /
                        timestampInc * constant;
                result.setVariance((float) (result.getVariance() + timestampInc * velocity * velocity / 1000));
            }

            float k = result.getVariance() / (result.getVariance() + accuracy * accuracy);
            double lat = result.getLocation().getLatitude() + k * (location.getLocation().getLatitude() - lastLocation.getLocation().getLatitude());
            double lng = result.getLocation().getLongitude() + k * (location.getLocation().getLongitude() - lastLocation.getLocation().getLongitude());

            location.getLocation().setLatitude(lat);
            location.getLocation().setLongitude(lng);
            location.setVariance((1 - k) * result.getVariance());
            location.getLocation().setSpeed(getCurrentSpeed(location.getLocation()));
        }

        return location;
    }

    public ArrayList<Location> runKalmanOnLocations() {
        ArrayList<Location> resultData = new ArrayList<>();
        for (Location location : rawData) {
            SmartLog.logE("source location :" + location.getLatitude() + " " + location.getLongitude());

            setLastLocation(kalman(new LocationKalman(location), getLastLocation(), KALMAN_CONST));

            SmartLog.logE("kalman location :" + getLastLocation().getLocation().getLatitude() + " " + getLastLocation().getLocation().getLongitude());
            SmartLog.logE("kalman speed :" + getLastLocation().getLocation().getSpeed());

            resultData.add(getLastLocation().getLocation());
        }
        return resultData;
    }

    public float getCurrentSpeed(Location location) {
        float speed = 0;
        if (this.lastLocation != null)
            speed = (float) (Math.sqrt(
                    Math.pow(location.getLongitude() - this.lastLocation.getLocation().getLongitude(), 2)
                            + Math.pow(location.getLatitude() - this.lastLocation.getLocation().getLatitude(), 2)
            ) / (location.getTime() - this.lastLocation.getLocation().getTime()));
        //if there is speed from location
        if (location.hasSpeed())
            //get location speed
            speed = location.getSpeed();
        return speed;
    }

}
