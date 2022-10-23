package com.example.test;

import android.app.Application;
import android.util.Log;

import com.lgy.crash.CrashErrorActivity;
import com.lgy.crash.CrashHandler;
import com.lgy.crash.ICrash;

public class LApplication extends Application {
    ICrash crash = new CrashHandler();
    @Override
    public void onCreate() {
        super.onCreate();
        crash.init(getApplicationContext(),null);
        crash.setStorageType(ICrash.STORAGE_LOCAL);
        crash.setOnHandlerListener(error -> {
            Log.e("lgy","LApplication========");
            CrashErrorActivity.start(getApplicationContext(),"error",true,"com.example.test.MainActivity");
        });
    }

}
