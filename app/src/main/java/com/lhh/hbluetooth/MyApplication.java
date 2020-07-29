package com.lhh.hbluetooth;

import android.app.Application;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HBUtil.initialize(this);
    }
}
