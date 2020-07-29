package com.lhh.hbluetooth;

public interface HBConnectionListener {

    void onDisconnected(String address);

    void onRead(byte[] cache);

    void onError(int code);
}
