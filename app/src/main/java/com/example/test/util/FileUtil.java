package com.example.test.util;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class FileUtil {

    public static final String USERNAME_KEY = "username";

    public static boolean saveUser(Context context, String username) {
        SharedPreferences.Editor editor = context.getSharedPreferences("user", MODE_PRIVATE).edit();
        editor.putString(USERNAME_KEY, username);
        return editor.commit();
    }

    public static String getUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", MODE_PRIVATE);
        return sharedPreferences.getString(USERNAME_KEY, null);
    }
}
