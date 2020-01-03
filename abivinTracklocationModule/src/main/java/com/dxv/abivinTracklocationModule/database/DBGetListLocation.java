package com.dxv.abivinTracklocationModule.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;


import com.dxv.abivinTracklocationModule.model.LocationAbi;

import java.util.ArrayList;
import java.util.Objects;


public class DBGetListLocation extends AsyncTask<Void, Objects, ArrayList<LocationAbi>> {
    @Override
    protected ArrayList<LocationAbi> doInBackground(Void... params) {
        ArrayList<LocationAbi> itemLocations = new ArrayList<>();
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        SQLiteDatabase db = databaseManager.openDatabase();

        if (getAllLocation(db) != null) {
            itemLocations = getAllLocation(db);
        }
        databaseManager.closeDatabase();
        return itemLocations;
    }

    public ArrayList<LocationAbi> getAllLocation(SQLiteDatabase db) {
        ArrayList<LocationAbi> arr = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.DB_TABLE, new String[]{DatabaseHelper.KEY_ID,
                        DatabaseHelper.KEY_LONG,
                        DatabaseHelper.KEY_LAT,
                        DatabaseHelper.KEY_SPEED,
                        DatabaseHelper.KEY_TIME,
                        DatabaseHelper.KEY_ACCURACY,
                        DatabaseHelper.KEY_PROVIDER,

                },
                null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                Double longitude = cursor.getDouble(1);
                Double latitude = cursor.getDouble(2);
                float speed = cursor.getFloat(3);
                long time = cursor.getLong(4);
                float accuracy = cursor.getFloat(5);
                String provider = cursor.getString(6);


                LocationAbi c = new LocationAbi(id,  longitude, latitude, speed, time, accuracy, provider );
                arr.add(c);

            } while (cursor.moveToNext());
            return arr;
        }
        return null;
    }
}
