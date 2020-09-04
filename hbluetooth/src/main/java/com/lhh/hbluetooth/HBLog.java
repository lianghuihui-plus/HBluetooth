package com.lhh.hbluetooth;

public class HBLog {

    private static final String TAG = "HBLog";

    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;

    public static int level = VERBOSE;

    public static void v(String tag, String msg) {
        if (level <= VERBOSE) {
            android.util.Log.v(tag, "[HBluetooth] " + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (level <= DEBUG) {
            android.util.Log.d(tag, "[HBluetooth] " + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (level <= INFO) {
            android.util.Log.i(tag, "[HBluetooth] " + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (level <= WARN) {
            android.util.Log.w(tag, "[HBluetooth] " + msg);
        }
    }

    public static void e(String tag, String msg) {
        if (level <= ERROR) {
            android.util.Log.e(tag, "[HBluetooth] " + msg);
        }
    }

    public static void setLogLevel(int logLevel) {
        android.util.Log.i(TAG, "Set log level: " + logLevel);
        level = logLevel;
    }
}
