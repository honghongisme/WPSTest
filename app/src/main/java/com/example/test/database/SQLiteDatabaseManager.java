package com.example.test.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import static com.example.test.database.DatabaseHelper.DB_NAME;
import static com.example.test.database.DatabaseHelper.VERSION;

public class SQLiteDatabaseManager {

    private static volatile SQLiteDatabaseManager mInstance;
    private SQLiteDatabase mSQLiteDatabase;

    private SQLiteDatabaseManager(Context context){
        mSQLiteDatabase = new DatabaseHelper(context, DB_NAME, null, VERSION).getWritableDatabase();
    }

    public static SQLiteDatabaseManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (SQLiteDatabaseManager.class) {
                if (mInstance == null) {
                    mInstance = new SQLiteDatabaseManager(context);
                }
            }
        }
        return mInstance;
    }

    public SQLiteDatabase getSQLiteDatabase() {
        return mSQLiteDatabase;
    }
}
