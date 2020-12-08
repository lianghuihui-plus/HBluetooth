package com.lhh.hbluetooth;

public interface HBConnectionListener {

    void onRead(byte[] bytes);
    void onDisconnected(String address);
    void onError(Exception e);
}
