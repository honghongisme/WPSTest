package com.example.test;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.test.service.DataCommitService;
import com.example.test.service.ProtectJobService;
import com.example.test.util.FileUtil;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_READ_PHONE_STATE = 1;
    private static final int JOB_ID = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        if (FileUtil.getUser(this) != null) {
            startService();
        }
        initView();

    }

    private void initView() {
        Button loginBtn = findViewById(R.id.login);
        final EditText usernameEt = findViewById(R.id.username);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usernameEt.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "请输入用户名", Toast.LENGTH_SHORT).show();
                } else {
                    boolean flag = FileUtil.saveUser(getApplicationContext(), usernameEt.getText().toString());
                    if (flag) {
                        Toast.makeText(getApplicationContext(), "登录成功，开始收集发送数据...", Toast.LENGTH_SHORT).show();
                        startService();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startJobScheduler();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "登录失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void startService() {
        startService(new Intent(MainActivity.this, DataCommitService.class));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startJobScheduler() {
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, new ComponentName(getPackageName(), ProtectJobService.class.getName()));
        // 间隔时间 最短15min
        builder.setPeriodic(15 * 60 * 1000);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        JobScheduler tm = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.cancel(JOB_ID);
        tm.schedule(builder.build());
    }

    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        }
    }


}
