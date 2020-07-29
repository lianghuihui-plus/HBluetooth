package com.lhh.hbluetooth;

import android.app.Application;
import android.content.Context;

public class HBApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        HBApplication.context = context;
    }
}
