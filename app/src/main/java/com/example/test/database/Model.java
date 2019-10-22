package com.example.test.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.test.entity.Info;
import com.example.test.entity.ResponseBody;
import com.example.test.entity.VariableInfo;
import com.example.test.service.DataCommitService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
import static com.example.test.database.DatabaseHelper.IP;
import static com.example.test.database.DatabaseHelper.TABLE_NAME;
import static com.example.test.database.DatabaseHelper.TIME;
import static com.example.test.database.DatabaseHelper.USERNAME;
import static com.example.test.database.DatabaseHelper.VERSION;

public class Model {

    private OkHttpClient mOkHttpClient;
    private SQLiteDatabase mSQLiteDatabase;
    private static final String URL = "http://www.mockhttp.cn/mock/wpstest";

    public Model(Context context) {
        mOkHttpClient = new OkHttpClient();
        mSQLiteDatabase = new DatabaseHelper(context, DB_NAME, null, VERSION).getWritableDatabase();
    }

    public List<VariableInfo> getInfoList() {
        String sql = "select * from " + TABLE_NAME;
        Cursor cursor = mSQLiteDatabase.rawQuery(sql, null);
        List<VariableInfo> list = new ArrayList<>();
        VariableInfo info = null;
        while (cursor.moveToNext()) {
            info = new VariableInfo();
            info.setIP(cursor.getString(cursor.getColumnIndex(IP)));
            info.setUserName(cursor.getString(cursor.getColumnIndex(USERNAME)));
            info.setTime(cursor.getString(cursor.getColumnIndex(TIME)));
            list.add(info);
        }
        return list;
    }

    public void saveInfo(VariableInfo info) {
        String sql = "insert into " + TABLE_NAME + " values('"
                + info.getIP() + "', '"
                + info.getUserName() + "', '"
                + info.getTime() + "')";
        mSQLiteDatabase.execSQL(sql);
    }

    public <T> void delete(T data) {
        if (data instanceof Info) {
            Info info = (Info)data;
            String sql = "delete from " + TABLE_NAME + " where " + TIME + " = '" + info.getTime() + "'";
            mSQLiteDatabase.execSQL(sql);
        } else if (data instanceof List){
            List<VariableInfo> list = (List<VariableInfo>) data;
            mSQLiteDatabase.beginTransaction();
            String sql;
            for (VariableInfo info : list) {
                sql = "delete from " + TABLE_NAME + " where " + TIME + " = '" + info.getTime() + "'";
                mSQLiteDatabase.execSQL(sql);
            }
            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }

    }

    public <T> void commit(final T data, final DataCommitService.OnFirstCommitCallback callback) {
        String jsonData = new Gson().toJson(data);
        System.out.println(jsonData);
        FormBody.Builder formBody = new FormBody.Builder();
        formBody.add("data", jsonData);
        Request request = new Request.Builder()
                .url(URL)
                .post(formBody.build())
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (callback != null) {
                    callback.onFailed();
                    System.out.println("首次onFailure!!!!!!!!!!! " + e.getMessage());
                } else {
                    System.out.println("onFailure!!!!!!!!!!! " + e.getMessage());
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String resp = response.body().string();
                // 如果url访问不到(我测试是url去掉尾部的t，应用会崩溃，报JsonSyntaxException异常)，response返回的结构就不是封装的responseBody了,
                ResponseBody responseBody = null;
                try {
                    responseBody = new Gson().fromJson(resp, ResponseBody.class);
                } catch (JsonSyntaxException e) {
                    if (callback != null) {
                        callback.onFailed();
                        System.out.println("首次提交失败!!!!!!!!!!!" + response.toString());
                    } else {
                        System.out.println("提交失败!!!!!!!!!!!" + response.toString());
                    }
                    return;
                }
                if (responseBody.getCode() == 200 && "success".equals(responseBody.getMsg())) {
                    if (callback != null) {
                        callback.onSuccess();
                        System.out.println("首次提交成功！！！！！！！！");
                    } else {
                        System.out.println("提交成功！！！！！！！！");
                    }
                    delete(data);
                } else {
                    if (callback != null) {
                        callback.onFailed();
                        System.out.println("首次提交失败!!!!!!!!!!!" + response.toString());
                    } else {
                        System.out.println("提交失败!!!!!!!!!!!" + response.toString());
                    }
                }
            }
        });
    }

}
