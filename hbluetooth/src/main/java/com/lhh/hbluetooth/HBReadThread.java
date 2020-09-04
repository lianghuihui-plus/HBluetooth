package com.lhh.hbluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.Arrays;

public class HBReadThread extends Thread {

    private static final String TAG = "HBReadThread";

    private String deviceName;

    private java.io.InputStream inputStream;

    private HBReadListener listener;

    private volatile boolean exit = false;

    public HBReadThread(String deviceName, BluetoothSocket socket, HBReadListener listener) {
        HBLog.i(TAG, "[ReadThread-" + deviceName + "] Read thread create");
        this.deviceName = deviceName;
        this.listener = listener;

        try {
            this.inputStream = socket.getInputStream();
        } catch (IOException e) {
            HBLog.e(TAG, "[ReadThread-" + deviceName + "] Get input stream failed: "
                    + e.getMessage());
        }
    }

    @Override
    public void run() {
        super.run();
        byte[] readBuffer = new byte[5120];
        byte[] buffer;
        int bufferLen;
        while (!exit) {
            try {
                bufferLen = inputStream.read(readBuffer);
                HBLog.d(TAG, "[ReadThread-" + deviceName + "] Read buffer len: " + bufferLen);
            } catch (IOException e) {
                if (!exit) {
                    HBLog.e(TAG, "[ReadThread-" + deviceName + "] Read buffer error: "
                            + e.getMessage());
                    listener.onError(HBConstant.ERROR_CODE_READ_FAILED);
                }
                break;
            }
            if (bufferLen > 0) {
                buffer = Arrays.copyOf(readBuffer, bufferLen);
                listener.onRead(buffer);
            }
        }
        release();
    }

    public void cancel() {
        HBLog.i(TAG, "[ReadThread-" + deviceName + "] Cancel");
        exit = true;
    }

    private void release() {
        try {
            HBLog.i(TAG, "[ReadThread-" + deviceName + "] Close input stream");
            inputStream.close();
        } catch (IOException e) {
            HBLog.e(TAG, "[ReadThread-" + deviceName + "] Close input stream failed: "
                    + e.getMessage());
        }
    }
}
