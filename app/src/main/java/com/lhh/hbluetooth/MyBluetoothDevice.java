package com.lhh.hbluetooth;

import android.bluetooth.BluetoothDevice;

public class MyBluetoothDevice {

    private BluetoothDevice device;
    private BluetoothDeviceStatus status;

    public MyBluetoothDevice(BluetoothDevice device, BluetoothDeviceStatus status) {
        this.device = device;
        this.status = status;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public BluetoothDeviceStatus getStatus() {
        return status;
    }

    public void setStatus(BluetoothDeviceStatus status) {
        this.status = status;
    }

    public enum BluetoothDeviceStatus {
        Disconnect(0),
        Connecting(1),
        Connected(2);

        public int value;

        BluetoothDeviceStatus(int value) {
            this.value = value;
        }
    }
}
