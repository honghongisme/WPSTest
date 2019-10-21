package com.example.test.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.test.entity.Info;

import static android.content.Context.MODE_PRIVATE;

public class FileUtil {

    public static final String SHARE_FILE_NAME_USER = "user";
    public static final String SHARE_FILE_NAME_INFO = "info";
    public static final String USERNAME_KEY = "username";
    public static final String IMEI_KEY = "IMEI";
    public static final String OSV_KEY = "OSV";
    public static final String PACKAGENAME_KEY = "PackageName";


    public static boolean saveUser(Context context, String username) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARE_FILE_NAME_USER, MODE_PRIVATE).edit();
        editor.putString(USERNAME_KEY, username);
        return editor.commit();
    }

    public static String getUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARE_FILE_NAME_USER, MODE_PRIVATE);
        return sharedPreferences.getString(USERNAME_KEY, null);
    }

    /**
     * 存储不变的信息
     * @param context
     * @param IMEI
     * @param OSV
     * @param PackageName
     * @return
     */
    public static boolean saveBaseInfo(Context context, String IMEI, String OSV, String PackageName) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARE_FILE_NAME_INFO, MODE_PRIVATE).edit();
        editor.putString(IMEI_KEY, IMEI);
        editor.putString(OSV_KEY, OSV);
        editor.putString(PACKAGENAME_KEY, PackageName);
        return editor.commit();
    }

    public static Info getBaseInfo(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARE_FILE_NAME_INFO, MODE_PRIVATE);
        Info info = new Info();
        info.setIMEI(sharedPreferences.getString(IMEI_KEY, null));
        info.setOSV(sharedPreferences.getString(OSV_KEY, null));
        info.setPackageName(sharedPreferences.getString(PACKAGENAME_KEY, null));
        return info;
    }
}
