package com.example.test.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.test.entity.Info;
import com.example.test.entity.ResponseBody;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.test.database.DatabaseHelper.DB_NAME;
import static com.example.test.database.DatabaseHelper.IMEI;
import static com.example.test.database.DatabaseHelper.IP;
import static com.example.test.database.DatabaseHelper.OSV;
import static com.example.test.database.DatabaseHelper.PACKAGE_NAME;
import static com.example.test.database.DatabaseHelper.TABLE_NAME;
import static com.example.test.database.DatabaseHelper.TIME;
import static com.example.test.database.DatabaseHelper.USERNAME;
import static com.example.test.database.DatabaseHelper.VERSION;

public class Model {

    private OkHttpClient mOkHttpClient;
    private SQLiteDatabase mSQLiteDatabase;

    public Model(Context context) {
        mOkHttpClient = new OkHttpClient();
        mSQLiteDatabase = new DatabaseHelper(context, DB_NAME, null, VERSION).getWritableDatabase();
    }

    public List<Info> getInfoList() {
        String sql = "select * from " + TABLE_NAME;
        Cursor cursor = mSQLiteDatabase.rawQuery(sql, null);
        List<Info> list = new ArrayList<>();
        Info info = null;
        while (cursor.moveToNext()) {
            info = new Info();
            info.setIMEI(cursor.getString(cursor.getColumnIndex(IMEI)));
            info.setOSV(cursor.getString(cursor.getColumnIndex(OSV)));
            info.setIP(cursor.getString(cursor.getColumnIndex(IP)));
            info.setPackageName(cursor.getString(cursor.getColumnIndex(PACKAGE_NAME)));
            info.setUserName(cursor.getString(cursor.getColumnIndex(USERNAME)));
            info.setTime(cursor.getString(cursor.getColumnIndex(TIME)));
            list.add(info);
        }
        return list;
    }

    public void saveInfo(Info info) {
        String sql = "insert into " + TABLE_NAME + " values('"
                + info.getIMEI() + "', '"
                + info.getOSV() + "', '"
                + info.getIP() + "', '"
                + info.getPackageName() + "', '"
                + info.getUserName() + "', '"
                + info.getTime() + "')";
        mSQLiteDatabase.execSQL(sql);
    }

    public void deleteInfo(List<Info> list) {
        mSQLiteDatabase.beginTransaction();
        String sql;
        for (Info info : list) {
            sql = "delete from " + TABLE_NAME + " where " + TIME + " = '" + info.getTime() + "'";
            mSQLiteDatabase.execSQL(sql);
        }
        mSQLiteDatabase.setTransactionSuccessful();
        mSQLiteDatabase.endTransaction();
    }

    public void commit(final List<Info> list) {
        String data = new Gson().toJson(list);
        FormBody.Builder formBody = new FormBody.Builder();
        formBody.add("data", data);
        Request request = new Request.Builder()
                .url("http://www.mockhttp.cn/mock/wpstest")
                .post(formBody.build())
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("onFailure!!!!!!!!!!! " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String resp = response.body().string();
                ResponseBody responseBody = new Gson().fromJson(resp, ResponseBody.class);
                if (responseBody.getCode() == 200 && "success".equals(responseBody.getMsg())) {
                    System.out.println("提交成功！！！！！！！！");
                    deleteInfo(list);
                } else {
                    System.out.println("提交失败!!!!!!!!!!!" + response.toString());
                }
            }
        });
    }
}
