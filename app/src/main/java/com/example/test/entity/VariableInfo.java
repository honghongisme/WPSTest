package com.example.test.entity;

/**
 * 可变的数据部分, 包括ip， username， time三部分
 * 除了第一次传递的是Info，之后传递的都是VariableInfo
 */
public class VariableInfo {

    private String IP;
    private String UserName;
    private String Time;

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }
}
