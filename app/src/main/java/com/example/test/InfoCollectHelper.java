package com.example.test;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.example.test.entity.Info;
import com.example.test.util.FileUtil;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

public class InfoCollectHelper {

    private static volatile InfoCollectHelper mInstance;
    private Context mContext;

    private TelephonyManager mTelephonyManager;
    private ConnectivityManager mConnectivityManager;
    private WifiManager mWifiManager;

    private InfoCollectHelper(Context context) {
        mContext = context.getApplicationContext();

        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    public static InfoCollectHelper getInstance(Context context) {
        if (mInstance == null) {
            synchronized (InfoCollectHelper.class) {
                if (mInstance == null) {
                    mInstance = new InfoCollectHelper(context);
                }
            }
        }
        return mInstance;
    }

    public Info getInfo() {
        Info info = new Info();
        info.setIMEI(getIMEI());
        info.setOSV(getOSV());
        info.setIP(getIP());
        info.setPackageName(getPackageName());
        info.setUserName(getUsername());
        info.setTime(getTime());
        return info;
    }

    private String getIMEI() {
        return mTelephonyManager.getSimSerialNumber();
    }

    private String getOSV() {
        return Build.VERSION.RELEASE;
    }

    private String getIP() {
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) { // 使用2G/3G/4G网络
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) { // 使用无线网络
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                String ipAddress = IPParseString(wifiInfo.getIpAddress());
                return ipAddress;
            }
        }
        return null;
    }

    private String getPackageName() {
        return mContext.getPackageName();
    }

    private String getUsername() {
        return FileUtil.getUser(mContext);
    }

    private String getTime() {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(new Date());
    }

    private String IPParseString(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }
}
