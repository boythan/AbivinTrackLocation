package com.dxv.abivinTracklocationModule.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dxv.abivinTracklocationModule.uitls.SmartLog;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String KEY_ID = "_id";
    public static final String KEY_SPEED = "speed";
    public static final String KEY_TIME = "datecreated";
    public static final String KEY_LONG = "longitude";
    public static final String KEY_LAT = "latitude";
    public static final String KEY_HISTORY = "history";
    public static final String KEY_ACCURACY = "accracy";
    public static final String KEY_PROVIDER = "provider";

    public static final String DB_TABLE = "location";
    public static final String DB_NAME = "ManageLocation";
    public static final int DB_VERSION = 2;

    static final String DB_DROP = "drop table if exists " + DB_TABLE+" ;";
    static final String DB_CREATE = "create table if not exists " + DB_TABLE + " (" +
            KEY_ID + " integer primary key autoincrement, " +
            KEY_LONG + " text not null, " +
            KEY_LAT + " text not null, " +
            KEY_SPEED + " text not null, " +
            KEY_TIME + " text not null, " +
            KEY_ACCURACY + " text not null, " +
            KEY_PROVIDER + " text not null" + ");";

//    static final String DB_ALTER_TABLE = "ALTER TABLE " + DB_TABLE + " ADD COLUMN " + KEY_FLAG +
//            " INTEGER DEFAULT 0";

    private static final String TAG = " DatabaseHelper: ";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(DB_CREATE);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(DB_DROP);
            db.execSQL(DB_CREATE);
            SmartLog.logE(TAG);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
