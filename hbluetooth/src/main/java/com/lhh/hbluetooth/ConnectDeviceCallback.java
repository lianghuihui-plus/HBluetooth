package com.lhh.hbluetooth;

public abstract class ConnectDeviceCallback {
    public abstract void onConnected(HBConnection connection);
    public abstract void onError(Exception e);
}
