package com.lhh.hbluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class HBReadThread extends Thread {

    private InputStream inputStream;

    private HBReadListener listener;

    private volatile boolean exit = false;

    public HBReadThread(BluetoothSocket socket, HBReadListener listener) {
        try {
            this.inputStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.listener = listener;
    }

    @Override
    public void run() {
        super.run();
        byte[] readBuffer;
        byte[] buffer;
        int bufferLen;
        while (!exit) {
            readBuffer = new byte[1024];
            try {
                bufferLen = inputStream.read(readBuffer);
            } catch (IOException e) {
                e.printStackTrace();
                listener.onError(HBConstant.ERROR_CODE_READ_FAILED);
                break;
            }
            if (bufferLen > 0) {
                buffer = Arrays.copyOf(readBuffer, bufferLen);
                listener.onRead(buffer);
            }
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        exit = true;
    }
}
