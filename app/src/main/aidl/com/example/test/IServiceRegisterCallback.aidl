// IServiceRegisterCallback.aidl
package com.example.test;

// Declare any non-default types here with import statements

interface IServiceRegisterCallback {
    void onSuccess(String msg);
    void onFailed(String msg);
}
