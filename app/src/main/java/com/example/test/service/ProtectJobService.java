package com.example.test.service;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.example.test.service.DataCommitService;

public class ProtectJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        System.out.println("ProtectJobService  执行jobService任务拉！！！！！！！");
        if (!isServiceRunning(this, "com.example.test.service.DataCommitService")) {
            System.out.println("ProtectJobService  服务wei启动  重启！！！！！！！");
            startService(new Intent(this, DataCommitService.class));
        } else {
            System.out.println("ProtectJobService  服务已启动！！！！！！！");
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (TextUtils.isEmpty(ServiceName)) {
            return false;
        }
        ActivityManager myManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : myManager.getRunningServices(30)) {
            if (service.service.getClassName().equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }
}
