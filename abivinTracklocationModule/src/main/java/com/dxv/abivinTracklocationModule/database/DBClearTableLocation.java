package com.dxv.abivinTracklocationModule.database;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;


public class DBClearTableLocation extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... params) {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        SQLiteDatabase db = databaseManager.openDatabase();
        db.delete(DatabaseHelper.DB_TABLE, null, null);
        databaseManager.closeDatabase();
        return null;
    }
}
