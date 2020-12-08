package com.lhh.hbluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * 蓝牙读线程
 * 唯一观察者为创建该实例的{@link HBConnection}对象
 * 并且该类的实例应该只由{@link HBConnection}对象进行创建
 */
public class HBReadThread extends Thread {

    private static final String TAG = "HBReadThread";

    private String tag;
    private HBReadListener listener;
    private InputStream inputStream;

    public HBReadThread(String tag, BluetoothSocket socket, HBReadListener listener) {
        this.tag = tag;
        this.listener = listener;
        try {
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            HBLog.e(TAG, "[ReadThread-"+tag+"] Get input stream error: " + e.getMessage());
        }
        HBLog.d(TAG, "[ReadThread-"+tag+"] Created");
    }

    @Override
    public void run() {
        super.run();
        if (inputStream == null) return;
        byte[] readBuffer = new byte[10240];
        byte[] buffer;
        int bufferLen;
        while (!isInterrupted()) {
            try {
                bufferLen = inputStream.read(readBuffer);
            } catch (IOException e) {
                HBLog.e(TAG, "[ReadThread-"+tag+"] Read buffer error: " + e.getMessage());
                break;
            }
            if (bufferLen > 0) {
                buffer = Arrays.copyOf(readBuffer, bufferLen);
                listener.onRead(buffer);
            }
        }
        release();
    }

    private void release() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = null;
            HBLog.d(TAG, "[ReadThread-"+tag +"] InputStream is closed");
        }

        listener = null;
    }
}
