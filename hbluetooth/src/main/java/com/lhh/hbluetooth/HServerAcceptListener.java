package com.lhh.hbluetooth;

public interface HServerAcceptListener {

    void onClientConnect(HBluetoothConnection connection);

    void onFailed(Exception e);
}
