package com.example.test.entity;

/**
 * 可变的数据部分, 包括ip， username， time三部分
 * 除了第一次传递的是Info，之后传递的都是VariableInfo
 */
public class VariableInfo {

    private String IP;
    private String Time;
    private String ServerResId;

    public String getIP() {
        return IP;
    }

    public String getServerResId() {
        return ServerResId;
    }

    public void setServerResId(String serverResId) {
        ServerResId = serverResId;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }
}
