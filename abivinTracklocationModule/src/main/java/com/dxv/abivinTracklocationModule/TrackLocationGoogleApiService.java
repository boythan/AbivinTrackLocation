package com.dxv.abivinTracklocationModule;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.dxv.abivinTracklocationModule.database.DBClearTableLocation;
import com.dxv.abivinTracklocationModule.database.DBInsertListLocation;
import com.dxv.abivinTracklocationModule.database.DatabaseManager;
import com.dxv.abivinTracklocationModule.kalman.KalmanAlgorithm;
import com.dxv.abivinTracklocationModule.model.LocationAbi;
import com.dxv.abivinTracklocationModule.model.LocationBody;
import com.dxv.abivinTracklocationModule.model.TrackLocationConfig;
import com.dxv.abivinTracklocationModule.uitls.SmartLog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


import static android.location.GpsStatus.GPS_EVENT_STARTED;
import static android.location.GpsStatus.GPS_EVENT_STOPPED;

public class TrackLocationGoogleApiService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final String TAG = TrackLocationGoogleApiService.class.getSimpleName();
    private static final int INTERVER_LOCATION = 5;
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private final LocationGoogleApiServiceBinder binder = new LocationGoogleApiServiceBinder();

    GoogleApiClient mLocationClient;
    LocationRequest mLocationRequest = new LocationRequest();
    private OkHttpHandler mOkHttpHandler;


    private TrackLocationConfig mLocationConfig;
    private String responseTrackLocal;
    private volatile boolean isUpdatingLocation = false;
    private ArrayList<Location> tempLocations = new ArrayList<>();
    private ArrayList<Location> locationPushToServer = new ArrayList<>();
    private KalmanAlgorithm kalmanAlgorithm = new KalmanAlgorithm(locationPushToServer);
    private Location mLocation;
    private Location bestLocation;
    private boolean hasSpeedInIntermediate = false;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static final int ACCURACY_DECAYS_TIME = 3; // Metres per second
    private KalmanLatLong kalmanLatLong = new KalmanLatLong(ACCURACY_DECAYS_TIME);

    private volatile int numberOfLocationInDatabase = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    /**
     * *********************************************LIFE CYCLE SERVICE *****************************
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(123456789, getNotification());
    }

    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = getResources().getString(R.string.appBundle);
        String channelName = getResources().getString(R.string.appTracking);
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle(getResources().getString(R.string.appTracking))
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationClient != null && mLocationClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
        }
    }

    /**
     * *********************************************SERVER API  *****************************
     */
    private class OkHttpHandler extends AsyncTask<ArrayList<Location>, Location, String> {
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(ArrayList<Location>... locationList) {
            isUpdatingLocation = true;
            return callToServer(locationList[0]);
        }

        private String callToServer(ArrayList<Location> locationList) {

            //load Data
            ArrayList<LocationAbi> locationArray = new ArrayList();

            for (Location locationParams : locationList) {
                LocationAbi locationAbi = new LocationAbi(locationParams);
                locationArray.add(locationAbi);
            }

            Long timestamp = Calendar.getInstance().getTimeInMillis();

            LocationBody locationBody = new LocationBody(mLocationConfig.getOrganizationId(), locationArray, mLocationConfig.getShipmentId(), false, timestamp, mLocationConfig.getRouteDetailId());
            SmartLog.logE("SHIPMENT IDDD ......." + mLocationConfig.getShipmentId());
            SmartLog.logE("ROUTE IDDD ......." + mLocationConfig.getRouteDetailId());

            // parse Data to Json object by gson library
            Gson gson = new Gson();

            if (locationBody.getLocations() != null && locationBody.getLocations().size() > 0) {
                ArrayList<LocationAbi> locationListParams = locationBody.getLocations();
                boolean isNANLocation = false;
                for (int index = 0; index < locationListParams.size(); index++) {
                    if (locationListParams.get(index) == null
                            || locationListParams.get(index).getLatitude().isNaN()
                            || locationListParams.get(index).getLongitude().isNaN()
                            || locationListParams.get(index).getSpeed().isNaN()
                            || locationListParams.get(index).getAccuracy().isNaN()) {
                        isNANLocation = true;
                    }
                }
                if (isNANLocation) {
                    SmartLog.logE("LOCATION NAN CRASHHHH.......");

                    return null;

                }
            }
            String locationJsonServer = gson.toJson(locationBody);
            RequestBody requestBody = RequestBody.create(JSON, locationJsonServer);

            loadDataToFirebase(locationBody);


            // call to server
            Request builder = new Request.Builder()
                    .url(mLocationConfig.getURL())
                    .header("x-access-token", mLocationConfig.getToken())
                    .post(requestBody)
                    .build();

            Response response = null;
            try {

                response = client.newCall(builder).execute();
                SmartLog.logE("Tracking SUCESSSSSS ......." + locationList.size() + " locations");

                isUpdatingLocation = false;
                locationPushToServer.clear();

                return response.body().toString();

            } catch (IOException e) {
                SmartLog.logE("Tracking FAILLLLLLL ......." + locationList.size() + " locations");

                isUpdatingLocation = false;
                e.printStackTrace();
            }
            return null;
        }


    }

    /**
     * ************************************************LOGIC PROCESS ******************************
     */

    private void makeServerRequest(ArrayList<Location> location) {
        SmartLog.logE("Make request to server .......");

        mOkHttpHandler = new TrackLocationGoogleApiService.OkHttpHandler();

        try {
            responseTrackLocal = mOkHttpHandler.execute(location).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void updateLocation(Location location) {
        this.mLocation = location;

        if (isUpdatingLocation) {
            SmartLog.logE("Location is updating so add to templocations........");

            if (tempLocations == null) {
                tempLocations = new ArrayList<>();
            }
            tempLocations.add(location);
        } else {
            if (tempLocations.size() > 0) {
                if (locationPushToServer.addAll(tempLocations)) {
                    tempLocations.clear();
                }
                SmartLog.logE("Push templocation to locationPushServer .......");

            }
            locationPushToServer.add(location);
            makeServerRequest(locationPushToServer);


        }
    }


    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /**
     * ***********************************************LOAD DATA TO LOCAL/FIREBASE SOTRAGE***********************************
     */
    private void backupLocationToDB(ArrayList<LocationAbi> abilocationList) {
        if (abilocationList == null) {
            return;
        }

        DatabaseManager.initializeInstance(this);
        DBInsertListLocation dbInsertListLocation = new DBInsertListLocation(numberOfLocationInDatabase) {
            @Override
            protected void onPostExecute(Integer number) {
                numberOfLocationInDatabase += number;
            }
        };
        dbInsertListLocation.execute(abilocationList);
    }

    private void clearTableLocationInDatabase() {
        DBClearTableLocation dbClearTableLocation = new DBClearTableLocation();
        dbClearTableLocation.execute();
    }


    private void loadDataToFirebase(LocationBody locationBody) {
        DatabaseReference myRef = database.getReference(Constant.REF_FIREBASE);
        if (mLocationConfig != null && mLocationConfig.getShipmentId() != null && !mLocationConfig.getShipmentId().isEmpty()) {
            // myRef.child("shipment").child(mLocationConfig.getShipmentId()).push().setValue(locationBody);
            myRef.child("shipment").child(mLocationConfig.getShipmentId()).setValue(locationBody);
        }
        if (mLocationConfig != null && mLocationConfig.getRouteDetailId() != null && !mLocationConfig.getRouteDetailId().isEmpty()) {
            // myRef.child("route").child(mLocationConfig.getRouteDetailId()).push().setValue(locationBody);
            myRef.child("route").child(mLocationConfig.getRouteDetailId()).setValue(locationBody);
        }
    }

    /**
     * ********************************************* CONTROL  *****************************
     */
    public void startTracking() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {

            SmartLog.logE("Start tracking");

            mLocationClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            if (mLocationClient == null || !mLocationClient.isConnected() || !mLocationClient.isConnecting()) {
                mLocationClient.connect();
            }
        } else {
            SmartLog.logE("unable to connect to google play services.");
        }
    }

    public void stopTracking() {
        if (mLocationClient != null && mLocationClient.isConnected()) {
            SmartLog.logE("Stop tracking");

            this.mLocation = null;
            tempLocations.clear();
            locationPushToServer.clear();
            isUpdatingLocation = false;
            hasSpeedInIntermediate = false;

            LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
            mLocationClient.disconnect();
            stopSelf(123456789);
        }
    }

    /**
     * *********************************************LOCATION LISTENER *****************************
     */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(5000); // milliseconds
        mLocationRequest.setFastestInterval(4000); // the fastest rate in milliseconds at which your app can handle location updates
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);

        LocationManager lm = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        lm.addGpsStatusListener(new android.location.GpsStatus.Listener() {
            public void onGpsStatusChanged(int event) {
                switch (event) {
                    case GPS_EVENT_STARTED:
                        // do your tasks
                        SmartLog.logE("GPS startedddddd");
                        break;
                    case GPS_EVENT_STOPPED:
                        // do your tasks
                        SmartLog.logE("GPS stopped");
                        Long timestamp = Calendar.getInstance().getTimeInMillis();
                        loadDataToFirebase(new LocationBody(true, timestamp));
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        stopTracking();
    }
    @Override
    public void onLocationChanged(Location location) {
        SmartLog.logE("On location change.....");
        String caseResult = "";
        if (location != null) {
            if (mLocation == null) {
                bestLocation = location;
                updateLocation(bestLocation);
            } else {

                if (isBetterLocation(location, bestLocation)) {
                    bestLocation = location;
                }

                float d = bestLocation.distanceTo(mLocation);
                long timeDelta = (bestLocation.getTime() - mLocation.getTime()) / 1000; //second
                if (!bestLocation.hasSpeed()) {
                    float speed = d * 1000 / timeDelta;
                    bestLocation.setSpeed(speed);
                }
                SmartLog.logE("SPEED LOCATION " + bestLocation.getSpeed());
                SmartLog.logE("TIME CHANGE " + timeDelta);
                SmartLog.logE("DISTANCE DEPEND SPEED " + bestLocation.getSpeed() * timeDelta + " (m)");
                SmartLog.logE("DISTANCE LOCATION " + d + " (m)");
                SmartLog.logE("ACCURACY LOCATION " + bestLocation.getAccuracy() + " (m)");
//                if (d > bestLocation.getSpeed() * timeDelta) {
//                    SmartLog.logE("---DISTANCE TOO FAR FROM--- ");
//
//                    return;
//                }
                if (bestLocation.getSpeed() == 0.0) {
                    SmartLog.logE("---NO SPEED--- ");
                    return;
                }
                if (bestLocation.hasSpeed() && bestLocation.getSpeed() > Constant.MAX_SPEED) {
                    return;
                }

                int distanceChange;
                if (!hasSpeedInIntermediate && bestLocation.getSpeed() <= Constant.MIN_SPEED && mLocation.getSpeed() <= Constant.MIN_SPEED) {
                    if (locationPushToServer != null && locationPushToServer.size() > 0) {
                        updateLocation(bestLocation);
                    }
                    distanceChange = (int) Math.max(Constant.DISTANCE_CHANGE * 8, bestLocation.getAccuracy());
                    caseResult = "case 1--" + distanceChange;
                    SmartLog.logE("---case 1--- " + distanceChange);

                } else if (bestLocation.getAccuracy() > Constant.MAX_ACCURACY && bestLocation.getAccuracy() < Constant.MAX_OFFLINE_ACCURACY) {
                    distanceChange = (int) (bestLocation.getAccuracy() * 3);
                    caseResult = "case 2--" + distanceChange;

                    SmartLog.logE("---case 2--- " + distanceChange);

                } else {
                    distanceChange = (int) (Math.pow(Math.max(Math.round(bestLocation.getSpeed()), 5), 2) + Constant.DISTANCE_CHANGE);
                    caseResult = "case 3--" + distanceChange;
                    SmartLog.logE("---case 3--- " + distanceChange);

                }
                if (d >= distanceChange) {
                    SmartLog.logE("update location");
                    updateLocation(bestLocation);

                    hasSpeedInIntermediate = false;
                } else if (bestLocation.getSpeed() >= Constant.MIN_SPEED * 3) {
                    SmartLog.logE("--------hasSpeedInIntermediate = true-----------");
                    hasSpeedInIntermediate = true;
                }
            }
        }
    }

    /**
     * ********************************************* INITIAL *****************************
     */

    private Notification getNotification() {

        return new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.appTracking))
                .setSmallIcon(R.drawable.ic_launcher)
                .setOngoing(true)
                .build();
    }


    public class LocationGoogleApiServiceBinder extends Binder {
        public TrackLocationGoogleApiService getService() {
            return TrackLocationGoogleApiService.this;
        }
    }

    public TrackLocationConfig getLocationParams() {
        return mLocationConfig;
    }

    public void setLocationParams(TrackLocationConfig mLocationConfig) {
        this.mLocationConfig = mLocationConfig;
    }
}
