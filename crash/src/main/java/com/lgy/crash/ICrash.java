package com.lgy.crash;

import android.content.Context;

import java.util.LinkedHashMap;

public interface ICrash extends Thread.UncaughtExceptionHandler {
    public String STORAGE_LOCAL = "localCardStorage"; // 本地存储
    public String STORAGE_NET = "netCardStorage"; // 网络存储
    public String STORAGE_NOT = "noStorage"; //不存储
    public String STORAGE_LOCAL_AND_NET = "localAndNetStorage"; //本地和网络存储

    void init(Context context, LinkedHashMap<String,String> map);
    void setStorageType(String type);
    void setStoragePath(String path);
    void setOnHandlerListener(OnHandlerListener listener);

    public interface OnHandlerListener{
        void onCrash(String error);
    }
}
