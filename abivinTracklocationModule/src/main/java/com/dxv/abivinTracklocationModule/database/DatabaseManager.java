package com.dxv.abivinTracklocationModule.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.dxv.abivinTracklocationModule.uitls.SmartLog;

import java.util.concurrent.atomic.AtomicInteger;


public class DatabaseManager {
    private AtomicInteger mOpenCounter = new AtomicInteger();

    SQLiteDatabase db;

    private static DatabaseManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;

    public static synchronized void initializeInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager();
            mDatabaseHelper = new DatabaseHelper(context);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }

        return instance;
    }

    /**
     * Open Database
     */
    public synchronized SQLiteDatabase openDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            db = mDatabaseHelper.getWritableDatabase();
        }
        SmartLog.logE("DatabaseManager + mOpenCounter = " + mOpenCounter);

        return db;
    }

    /*
    Close Database
     */
    public synchronized void closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            db.close();
        }
    }
}
