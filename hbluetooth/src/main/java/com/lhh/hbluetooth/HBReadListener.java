package com.lhh.hbluetooth;

public interface HBReadListener {
    void onRead(byte[] bytes);
    void onError(Exception e);
}
