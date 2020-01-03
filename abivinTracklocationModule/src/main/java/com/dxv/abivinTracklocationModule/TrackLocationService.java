package com.dxv.abivinTracklocationModule;

import android.app.Notification;

import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
//import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dxv.abivinTracklocationModule.model.TrackLocationConfig;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import androidx.core.app.NotificationCompat;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TrackLocationService extends Service {
    private final LocationServiceBinder binder = new LocationServiceBinder();
    private final String TAG = "BackgroundService";
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private OkHttpHandler mOkHttpHandler;
    private String responseTrackLocal;


    private TrackLocationConfig trackLocationConfig;

    private final int LOCATION_INTERVAL = 500;
    private final int LOCATION_DISTANCE = 0;


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    private class OkHttpHandler extends AsyncTask<Location, Location, String>

    {
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(Location... location) {
            Gson gson = new Gson();
            String locationJson = gson.toJson(location);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
//                    .addFormDataPart("organizationId", trackLocationConfig.getOrganizationId())
                    .addFormDataPart("coords", locationJson)
                    .build();

            Request builder = new Request.Builder()
                    .url("https://vapp.cotest.abivin.vn" + trackLocationConfig.getURL())
                    .header("x-access-token", trackLocationConfig.getToken())
                    .post(requestBody)
                    .build();

            Response response = null;
            try {
                response = client.newCall(builder).execute();
                return response.body().toString();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class LocationListener implements android.location.LocationListener {
        private Location lastLocation = null;
        private final String TAG = "LocationListener";
        private Location mLastLocation;

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            mOkHttpHandler = new OkHttpHandler();

            try {
                responseTrackLocal = mOkHttpHandler.execute(location).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + status);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        startForeground(12345678, getNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listners, ignore", ex);
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void startTracking() {
        initializeLocationManager();
        mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER);
        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener);

        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

    }

    public void stopTracking() {
        this.onDestroy();
    }

    private Notification getNotification() {

        return new NotificationCompat.Builder(this)
                .setContentTitle("Abivin tracking")
                .setSmallIcon(R.drawable.ic_launcher)
                .setOngoing(true)
                .build();
    }


    public class LocationServiceBinder extends Binder {
        public TrackLocationService getService() {
            return TrackLocationService.this;
        }
    }

    public TrackLocationConfig getTrackLocationConfig() {
        return trackLocationConfig;
    }

    public void setTrackLocationConfig(TrackLocationConfig trackLocationConfig) {
        this.trackLocationConfig = trackLocationConfig;
    }
}
