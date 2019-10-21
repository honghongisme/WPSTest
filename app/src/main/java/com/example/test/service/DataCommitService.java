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

import com.example.test.entity.VariableInfo;
import com.example.test.util.FileUtil;
import com.example.test.util.InfoCollectHelper;
import com.example.test.R;
import com.example.test.database.Model;
import com.example.test.entity.Info;

import java.util.List;

/**
 * 考虑的保活策略
 * 1、双进程保护 （5.0以上采取回收进程组策略，主进程死了，他启动的子进程也会被杀放弃。）
 * 2、静态广播监听系统广播唤醒服务，比如网络状态改变 （高版本上谷歌已经不太支持静态广播了，很多都不能用了，idea也会提示放弃使用）
 * 3、后台无声音乐（不友好）
 * 4、前台服务（可行，正常的保活方法）
 * 5、JobScheduler（android9.0上最短定时间隔时间是15分钟，我试过。。好像只能执行一次，不太清楚原因）
 *
 * 项目采用的保活策略
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

    // 是否提交完整数据包
    private boolean mIsCommitEntireData;
    private Boolean mIsCommitEntireDataSuccess = null;


    @Override
    public void onCreate() {
        super.onCreate();

        mModel = new Model(this);
        mInfoCollectHelper = InfoCollectHelper.getInstance(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForeGroundService();
        }
        if (FileUtil.getBaseInfo(this).getIMEI() != null) {
            mIsCommitEntireData = false;
        } else {
            // 保存不变数据
            FileUtil.saveBaseInfo(this, mInfoCollectHelper.getIMEI(), mInfoCollectHelper.getOSV(), mInfoCollectHelper.getPackageName());
            mIsCommitEntireData = true;
        }
        startTask();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 使用while死循环 + thread.sleep进行定时操作
     * 步骤：
     * 1、查询数据库里是否有未提交的数据
     * 2、获取本次数据并保存在数据库里
     * 3、数据整合成list一并提交
     * 4、只有后台返回success时才算提交成功，删除数据库里本次提交的内容
     *
     * 为了避免数据冗余，只提交一次完整数据（当本地没有存储不变信息时） IMEI + OSV + PackageName + IP + username + time
     * 接下来的请求都只提交数据中可变的部分 IP + username + time
     * 需要后台支持
     */
    private void startTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // 查询数据库里未提交的数据
                    final List<VariableInfo> list = mModel.getInfoList();
                    // 获取本次数据
                    VariableInfo variableInfo = mInfoCollectHelper.getInfo();
                    if (variableInfo.getUserName() != null) {
                        // 保存本次数据
                        mModel.saveInfo(variableInfo);
                        list.add(variableInfo);
                    }
                    if (list.size() != 0) {
                        // 如果还没提交过，就提交一次完整数据，取第一条数据
                        if (mIsCommitEntireData) {
                            Info info = FileUtil.getBaseInfo(DataCommitService.this);
                            info.setIP(list.get(0).getIP());
                            info.setTime(list.get(0).getTime());
                            info.setUserName(list.get(0).getUserName());
                            mModel.commit(info, new OnFirstCommitCallback() {
                                @Override
                                public void onSuccess() {
                                    mIsCommitEntireData = false;
                                    mIsCommitEntireDataSuccess = true;
                                    list.remove(0);
                                }

                                @Override
                                public void onFailed() {
                                    mIsCommitEntireDataSuccess = false;
                                }
                            });
                        }
                        // 如果发送完整数据失败，要重试即继续while循环，直到发送成功才能继续发送接下来的数据
                        // 什么时候成功？
                        // mIsCommitEntireDataSuccess == null表示以前发过完整数据，服务不需要发了，只需要发接下来的可变数据。
                        // mIsCommitEntireDataSuccess表示发了且成功了，继续发送可变数据
                        if (list.size() != 0 && (mIsCommitEntireDataSuccess == null || mIsCommitEntireDataSuccess)) {
                            // 发送可变数据
                            mModel.commit(list, null);
                        }
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

    public interface OnFirstCommitCallback {
        void onSuccess();
        void onFailed();
    }
}
