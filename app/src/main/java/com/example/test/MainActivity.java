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
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.test.service.ProtectJobService;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_READ_PHONE_STATE = 1;
    private static final int JOB_ID = 2;

    private IInfoAidlInterface mBinder;

    private EditText mUsernameEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();
        initView();
    }

    private void initView() {
        Button loginBtn = findViewById(R.id.login);
        mUsernameEt = findViewById(R.id.username);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUsernameEt.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "请输入用户名", Toast.LENGTH_SHORT).show();
                } else {
                    startService(mUsernameEt.getText().toString());
                }
            }
        });

        Button button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService("com.example.test.thread1_username");
            }
        });

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService("com.example.test.thread2_username");
            }
        });
    }

    /**
     * 注册服务的最小单位是线程
     * @param username
     */
    public void startService(final String username) {
        // action必须和aidl文件一个包名下
        Intent intent = new Intent("com.example.test.remoteDataCommitService");
        intent.setPackage("com.example.test");
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                System.out.println("onServiceConnected");
                mBinder = IInfoAidlInterface.Stub.asInterface(service);
                try {
                    mBinder.register(getPackageName(), username, new IServiceRegisterCallback.Stub() {
                        @Override
                        public void onSuccess(String msg) throws RemoteException {
                            System.out.println("IServiceRegisterCallback onSuccess msg = " + msg);
                        }

                        @Override
                        public void onFailed(String msg) throws RemoteException {
                            System.out.println("IServiceRegisterCallback onFailed msg = " + msg);
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                System.out.println("onServiceDisconnected" + name.toString());
            }
        };
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
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
