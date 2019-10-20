package com.example.test.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;

import com.example.test.InfoCollectHelper;
import com.example.test.R;
import com.example.test.database.Model;
import com.example.test.entity.Info;
import com.google.gson.Gson;

import java.util.List;

/**
 * 保活方法
 * 1、设置为前台服务
 * 2、jobschedulerservice定时检查服务是否开启
 */
public class DataCommitService extends Service {

    private static final String ID="channel_1";
    private static final String NAME="前台服务";
    // 发送消息的间隔时间
    private static final int INTERVAL_TIME = 8000;

    private InfoCollectHelper mInfoCollectHelper;
    private Model mModel;


    @Override
    public void onCreate() {
        super.onCreate();

        mModel = new Model(this);
        mInfoCollectHelper = InfoCollectHelper.getInstance(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForeGroundService();
        }
        startTask();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // 查询数据库里未提交的数据
                    List<Info> list = mModel.getInfoList();
                    // 获取本次数据
                    Info info = mInfoCollectHelper.getInfo();
                    // 登录过，不是被广播唤醒的
                    if (info.getUserName() != null) {
                        // 保存本次数据
                        mModel.saveInfo(info);
                        list.add(info);
                    }
                    if (list.size() != 0) {
                        System.out.println(new Gson().toJson(list));
                        // 发送数据
                        mModel.commit(list);
                        try {
                            Thread.sleep(INTERVAL_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForeGroundService() {
        NotificationManager manager = (NotificationManager)getSystemService (NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel (ID, NAME, NotificationManager.IMPORTANCE_HIGH);
        manager.createNotificationChannel (channel);
        Notification notification = new Notification.Builder (this,ID)
                .setContentTitle ("已开启后台数据收集服务")
                .setContentText ("持续发送数据....")
                .setSmallIcon (R.mipmap.ic_launcher)
                .setLargeIcon (BitmapFactory.decodeResource (getResources (), R.mipmap.ic_launcher))
                .build ();
        startForeground (1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 允许重启
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 杀死重启
        Intent intent = new Intent(getApplicationContext(), DataCommitService.class);
        startService(intent);
    }
}
