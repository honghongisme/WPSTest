package com.example.test.entity;

public class Info {
    private String IMEI;
    private String OSV;
    private String IP;
    private String PackageName;
    private String UserName;
    private String Time;

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public void setOSV(String OSV) {
        this.OSV = OSV;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public void setPackageName(String packageName) {
        PackageName = packageName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getIMEI() {
        return IMEI;
    }

    public String getOSV() {
        return OSV;
    }

    public String getIP() {
        return IP;
    }

    public String getPackageName() {
        return PackageName;
    }

    public String getUserName() {
        return UserName;
    }

    public String getTime() {
        return Time;
    }

    @Override
    public String toString() {
        return "Info{" +
                "IMEI='" + IMEI + '\'' +
                ", OSV='" + OSV + '\'' +
                ", IP='" + IP + '\'' +
                ", PackageName='" + PackageName + '\'' +
                ", UserName='" + UserName + '\'' +
                ", Time='" + Time + '\'' +
                '}';
    }
}
