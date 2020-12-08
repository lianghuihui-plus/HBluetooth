package com.lhh.hbluetooth;

public interface HBAcceptDeviceListener {

    void onAccepted(HBConnection connection);
    void onError(Exception e);
}
