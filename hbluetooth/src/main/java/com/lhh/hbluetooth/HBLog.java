package com.lhh.hbluetooth;

public class HBLog {
    protected static final int VERBOSE = 1;
    protected static final int DEBUG = 2;
    protected static final int INFO = 3;
    protected static final int WARN = 4;
    protected static final int ERROR = 5;
    protected static final int NOTHING = 6;

    protected static int level = NOTHING;

    protected static void v(String tag, String msg) {
        if (level <= VERBOSE) {
            android.util.Log.v(tag, "[HBluetooth] " + msg);
        }
    }

    protected static void d(String tag, String msg) {
        if (level <= DEBUG) {
            android.util.Log.d(tag, "[HBluetooth] " + msg);
        }
    }

    protected static void i(String tag, String msg) {
        if (level <= INFO) {
            android.util.Log.i(tag, "[HBluetooth] " + msg);
        }
    }

    protected static void w(String tag, String msg) {
        if (level <= WARN) {
            android.util.Log.w(tag, "[HBluetooth] " + msg);
        }
    }

    protected static void e(String tag, String msg) {
        if (level <= ERROR) {
            android.util.Log.e(tag, "[HBluetooth] " + msg);
        }
    }

    protected static void setLogLevel(int logLevel) {
        level = logLevel;
    }
}
