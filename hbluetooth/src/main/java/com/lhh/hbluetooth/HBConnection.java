package com.lhh.hbluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 蓝牙连接对象
 * 实际控制读写操作，并向观察者反馈数据及状态
 * 通过定时心跳包监测Socket断开状态
 */
public class HBConnection {

    private static final String TAG = "HBConnection";

    private String deviceName;
    private String deviceAddress;
    private BluetoothSocket socket;
    private ConcurrentHashMap<String, HBConnectionListener> listenerMap;
    private HBReadThread readThread;
    private OutputStream outputStream;
    private OutputStream heartBeatSteam;
    private volatile boolean isDeaded = false;
    private Timer timer;

    public HBConnection(String deviceName, String deviceAddress, BluetoothSocket socket) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        this.socket = socket;
        listenerMap = new ConcurrentHashMap<>();
        startHeartBeat();
        HBLog.d(TAG, "[HBConnection-"+deviceName+"] Created");
    }

    private void startHeartBeat() {
        timer = new Timer();
        timer.schedule(new HeartBeatTask(socket), 1000, 1000);
    }

    private void cancelHeartBeat() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public boolean isDeaded() {
        return isDeaded;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void registerListener(String key, HBConnectionListener listener) {
        if (listenerMap != null && !listenerMap.containsKey(key)) {
            listenerMap.put(key, listener);
        }
    }

    public void unregisterListener(String key) {
        if (listenerMap != null) {
            listenerMap.remove(key);
        }
    }

    public void write(byte[] bytes) {
        if (isDeaded) return;
        try {
            if (outputStream == null)
                outputStream = socket.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            HBLog.e(TAG, "[HBConnection-"+deviceName+"] Get output stream error: " + e.getMessage());
        }
    }

    public void startRead() {
        if (isDeaded || readThread != null) return;
        HBLog.d(TAG, "[HBConnection-"+deviceName+"] Start read");
        readThread = new HBReadThread(deviceName, socket, new HBReadListener() {
            @Override
            public void onRead(byte[] bytes) {
                for (HBConnectionListener listener : listenerMap.values()) {
                    listener.onRead(bytes);
                }
            }

            @Override
            public void onError(Exception e) {
                for (HBConnectionListener listener: listenerMap.values()) {
                    listener.onError(e);
                }
            }
        });
        readThread.start();
    }

    public void stopRead() {
        if (readThread != null) {
            readThread.interrupt();
        }
    }

    public void disconnect() {
        isDeaded = true;
        stopRead();
        cancelHeartBeat();

        if (heartBeatSteam != null) {
            try {
                heartBeatSteam.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            heartBeatSteam = null;
            HBLog.d(TAG, "[HBConnection-"+deviceName+"] HeartBeatStream is closed");
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
            HBLog.d(TAG, "[HBConnection-"+deviceName+"] OutputStream is closed");
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
            HBLog.d(TAG, "[HBConnection-"+deviceName+"] Socket is closed");
        }

        for (HBConnectionListener listener : listenerMap.values()) {
            listener.onDisconnected(deviceAddress);
        }
        listenerMap = null;
    }

    private class HeartBeatTask extends TimerTask {

        private OutputStream outputStream;

        public HeartBeatTask(BluetoothSocket socket) {
            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                release();
            }
        }

        private void release() {
            HBLog.i(TAG, "[HBConnection-"+deviceName+"] Remote socket is closed");
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                outputStream = null;
            }
            cancelHeartBeat();
            disconnect();
        }

        @Override
        public void run() {
            try {
                outputStream.write(new byte[]{0x00});
            } catch (IOException e) {
                release();
            }
        }
    }
}
