package com.example.test.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "test_db";
    public static final String TABLE_NAME = "info";
    public static final int VERSION = 1;
    public static final String IMEI = "IMEI";
    public static final String OSV = "OSV";
    public static final String IP = "IP";
    public static final String PACKAGE_NAME = "package_name";
    public static final String USERNAME = "username";
    public static final String TIME = "time";



    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "create table " + TABLE_NAME + " ("
                + IMEI + " text, "
                + OSV + " text, "
                + IP + " text, "
                + PACKAGE_NAME + " text, "
                + USERNAME + " text, "
                + TIME + " text primary key)";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
