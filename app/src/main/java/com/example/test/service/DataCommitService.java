package com.example.test.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.RequiresApi;

import com.example.test.IInfoAidlInterface;
import com.example.test.IServiceRegisterCallback;
import com.example.test.R;
import com.example.test.entity.VariableInfo;
import com.example.test.util.InfoCollectHelper;
import com.example.test.database.Model;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 考虑的保活策略
 * 1、双进程保护 （5.0以上采取回收进程组策略，主进程死了，他启动的子进程也会被杀。）
 * 2、静态广播监听系统广播唤醒服务，比如网络状态改变 （高版本上谷歌已经不太支持静态广播了，很多都不能用了，idea也会提示deprecated）
 * 3、后台无声音乐（应该是可行的，比如网易云音乐如果正在播放，后台清理不会清理掉服务，事实上app也不会清理掉。但如果是非音乐类app就不适合使用，不友好）
 * 4、前台服务（可行，正常的保活方法）
 * 5、JobScheduler（官方推荐）
 * 6、关联app拉活（条件不足）
 * 7、厂商白名单（不存在的）
 * 8、
 *
 * 项目采用的保活策略
 * 1、设置为前台服务
 * 2、jobschedulerservice定时检查服务是否开启，15分钟间隔周期性检查（android9.0上最短定时间隔时间是15分钟，我试过。。好像只能执行一次，不太清楚原因）
 *
 *
 * 测试结果（Android9.0）：
 * 按下返回、切到后台，息屏状态下服务一直存在
 * 但后台清理和手动杀死app，服务会关闭，唤醒不了
 */
public class DataCommitService extends Service {

    private static final String ID="channel_1";
    private static final String NAME="前台服务";
    // 发送消息的间隔时间 5分钟
    private static final int INTERVAL_TIME = 5 *1000;

    private InfoCollectHelper mInfoCollectHelper;
    private Model mModel;
    private Thread mTask;

    private ConcurrentHashMap<String, String> mCurrentRegisterUser; // <username, serverResId>

    private IInfoAidlInterface.Stub stub = new IInfoAidlInterface.Stub() {
        @Override
        public void register(final String packageName, final String username, final IServiceRegisterCallback callback) throws RemoteException {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentRegisterUser.get(username) != null) { // 服务正在采集该用户的数据
                        try {
                            callback.onFailed(packageName + " ：" + username + "正在进行服务");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String id = mModel.getUserId(username);
                        if (id != null) { // 之前已经注册，表里已经有该用户的数据
                            mCurrentRegisterUser.put(username, id);
                            try {
                                callback.onSuccess(packageName + " ：" + username + "登录成功, 开始采集数据");
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        } else { // 从未注册过服务
                            // 发送不变数据
                            mModel.commit(mInfoCollectHelper.getConstantInfo(username, packageName), new OnFirstCommitCallback() {
                                @Override
                                public void onSuccess(String resId) {
                                    mModel.createUser(username, resId);
                                    mCurrentRegisterUser.put(username, resId);
                                    try {
                                        callback.onSuccess(packageName + " ：" + username + "注册成功，开始采集数据");
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailed() {

                                }
                            });
                        }
                    }
                }
            }).start();
        }

        @Override
        public void unRegister(IServiceRegisterCallback callback) throws RemoteException {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("onCreate!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        mCurrentRegisterUser = new ConcurrentHashMap<>();
        mModel = new Model(this);
        mInfoCollectHelper = InfoCollectHelper.getInstance(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForeGroundService();
        }
        startTask();
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("onBind!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return stub;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("onUnbind!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return super.onUnbind(intent);
    }

    /**
     * 使用while死循环 + thread.sleep进行定时操作
     * 步骤：
     * 1、查询数据库里是否有未提交的数据
     * 2、获取本次数据并保存在数据库里
     * 3、数据整合成list一并提交
     * 4、只有后台返回success时才算提交成功，删除数据库里本次提交的内容
     *
     * 为了避免数据冗余，只提交一次不变数据（第一次register时） IMEI + OSV + PackageName + username。获取到后台关联的serverResId
     * 接下来的请求都只提交数据中可变的部分 IP +  time + serverResId
     * 需要后台支持
     */
    private void startTask() {
        mTask = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // 查询数据库里未提交的数据
                    final List<VariableInfo> list = mModel.getDatabaseInfoList();
                    for (String resId : mCurrentRegisterUser.values()) {
                        VariableInfo data = mInfoCollectHelper.getVariableInfo(resId);
                        // 保存本次数据
                        mModel.saveInfo(data);
                        list.add(data);
                    }
                    mModel.commit(list, null);
                    try {
                        Thread.sleep(INTERVAL_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        mTask.start();
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
        void onSuccess(String resId);
        void onFailed();
    }
}
