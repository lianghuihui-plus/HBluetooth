package com.lhh.hbluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 蓝牙连接对象
 */
public class HBluetoothConnection {

    private String deviceName;

    private String devcieAddress;

    private BluetoothSocket socket;

    private InputStream inputStream;

    private OutputStream outputStream;

    private boolean isClosed = false;

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
