package com.dxv.abivinTracklocationModule.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;


import com.dxv.abivinTracklocationModule.model.LocationAbi;
import com.dxv.abivinTracklocationModule.uitls.SmartLog;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by AnhlaMrDuc on 25-Apr-16.
 */
public class DBInsertListLocation extends
        AsyncTask<ArrayList<LocationAbi>, Objects, Integer> {

    private int index;

    /**
     * insert only from index of list
     *
     * @param numberOfLocationInDatabase
     */
    public DBInsertListLocation(int numberOfLocationInDatabase) {
        this.index = numberOfLocationInDatabase;
    }

    @Override
    protected Integer doInBackground(ArrayList<LocationAbi>... params) {
        String sql = "INSERT INTO " + DatabaseHelper.DB_TABLE + "(" + DatabaseHelper.KEY_LONG + ","
                + DatabaseHelper.KEY_LAT + ","
                + DatabaseHelper.KEY_SPEED + ","
                + DatabaseHelper.KEY_TIME + ","
                + DatabaseHelper.KEY_ACCURACY + ","
                + DatabaseHelper.KEY_PROVIDER + ")" + " VALUES(?,?,?,?,?,?)";

        DatabaseManager databaseManager = DatabaseManager.getInstance();
        SQLiteDatabase db = databaseManager.openDatabase();

        ArrayList<LocationAbi> itemLocations = params[0];
        int count = 0;

        db.beginTransaction();

        SQLiteStatement statementInsert = db.compileStatement(sql);

        try {
            for (int i = index; i < itemLocations.size(); i++) {
                LocationAbi itemLocation = itemLocations.get(i);
                statementInsert.bindDouble(1, itemLocation.getLongitude());
                statementInsert.bindDouble(2, itemLocation.getLatitude());
                statementInsert.bindDouble(3, itemLocation.getSpeed());
                statementInsert.bindLong(4, itemLocation.getTime());
                statementInsert.bindDouble(5, itemLocation.getAccuracy());
                statementInsert.bindString(6, itemLocation.getProvider());

                statementInsert.execute();
                statementInsert.clearBindings();
                count++;
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }

        SmartLog.logE("DBInsertListLocation  save to DB success " + count + "/ " + itemLocations.size());
        databaseManager.closeDatabase();
        return count;
    }
}
