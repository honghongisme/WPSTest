package com.example.test.entity;

public class ConstantInfo {

    private String IMEI;
    private String OSV;
    private String PackageName;
    private String UserName;

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getOSV() {
        return OSV;
    }

    public void setOSV(String OSV) {
        this.OSV = OSV;
    }

    public String getPackageName() {
        return PackageName;
    }

    public void setPackageName(String packageName) {
        PackageName = packageName;
    }
}
