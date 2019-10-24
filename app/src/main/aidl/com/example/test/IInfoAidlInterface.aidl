// IInfoAidlInterface.aidl
package com.example.test;

// Declare any non-default types here with import statements
import com.example.test.IServiceRegisterCallback;

interface IInfoAidlInterface {
    void register(String packageName, String username, IServiceRegisterCallback callback);
    void unRegister(IServiceRegisterCallback callback);
}
