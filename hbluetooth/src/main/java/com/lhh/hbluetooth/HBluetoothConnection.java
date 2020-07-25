package com.lhh.hbluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.InputStream;
import java.io.OutputStream;

public class HBluetoothConnection {

    private String deviceName;

    private String devcieAddress;

    private BluetoothSocket socket;

    private InputStream inputStream;

    private OutputStream outputStream;

    public HBluetoothConnection(String deviceName, String devcieAddress, BluetoothSocket socket) {
        this.deviceName = deviceName;
        this.devcieAddress = devcieAddress;
        this.socket = socket;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDevcieAddress() {
        return devcieAddress;
    }

    public void setDevcieAddress(String devcieAddress) {
        this.devcieAddress = devcieAddress;
    }

    public BluetoothSocket getSocket() {
        return socket;
    }

    public void setSocket(BluetoothSocket socket) {
        this.socket = socket;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
}
