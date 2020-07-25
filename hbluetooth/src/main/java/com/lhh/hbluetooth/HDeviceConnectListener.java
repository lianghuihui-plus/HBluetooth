package com.lhh.hbluetooth;

import android.bluetooth.BluetoothDevice;

public interface HDeviceConnectListener {

    void onSuccess(HBluetoothConnection connection);

    void onAlreadyConnected();

    void onFailed(int code);
}
