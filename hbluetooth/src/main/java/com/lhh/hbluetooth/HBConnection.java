package com.lhh.hbluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 蓝牙连接对象
 * 与蓝牙设备建立连接后，将会通过连接回调返回一个该对象
 * 您应该通过该对象对当前的连接进行读、写等操作
 */
public class HBConnection {

    private String deviceName;

    private String devcieAddress;

    private BluetoothSocket socket;

    private InputStream inputStream;

    private OutputStream outputStream;

    private boolean isClosed = false;

    public HBConnection(String deviceName, String devcieAddress, BluetoothSocket socket) {
        this.deviceName = deviceName;
        this.devcieAddress = devcieAddress;
        this.socket = socket;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDevcieAddress() {
        return devcieAddress;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void close() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = null;
        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }

        isClosed = true;
    }
}
