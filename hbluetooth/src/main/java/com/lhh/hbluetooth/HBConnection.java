package com.lhh.hbluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 蓝牙连接对象，使用了观察者模式
 */
public class HBConnection {

    private static final String TAG = "HBConnection";

    public static final int CONNECTION_STATE_DEAD = 0;

    public static final int CONNECTION_STATE_ALIVE = 1;

    private String deviceName;

    private String devcieAddress;

    private int state;

    private BluetoothSocket socket;

    private java.io.OutputStream outputStream;

    private HBReadThread readThread;

    private ConcurrentHashMap<String, HBConnectionListener> connectionListenerHashMap
            = new ConcurrentHashMap<>();

    private byte[] readCache;

    public HBConnection(String deviceName, String devcieAddress, BluetoothSocket socket) {
        HBLog.i(TAG, "[Connection-" + deviceName + "] Connection create: " + devcieAddress);
        this.deviceName = deviceName;
        this.devcieAddress = devcieAddress;
        this.socket = socket;

        state = CONNECTION_STATE_ALIVE;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDevcieAddress() {
        return devcieAddress;
    }

    public int getState() {
        return state;
    }

    public boolean startRead() {
        if (state != CONNECTION_STATE_ALIVE) {
            HBLog.w(TAG, "[Connection-" + deviceName + "] Start read failed: connection is not alive");
            return false;
        }
        if (readThread != null) {
            HBLog.w(TAG, "[Connection-" + deviceName + "] Already reading");
            return true;
        }
        HBLog.i(TAG, "[Connection-" + deviceName + "] Start read");
        readThread = new HBReadThread(deviceName, socket, new HBReadListener() {
            @Override
            public void onRead(byte[] cache) {
                if (readCache == null) {
                    readCache = cache;
                } else {
                    byte[] tmp = Arrays.copyOf(readCache, readCache.length + cache.length);
                    System.arraycopy(cache, 0, tmp, readCache.length, cache.length);
                    readCache = tmp;
                }

                if (connectionListenerHashMap.size() > 0) {
                    for (HBConnectionListener listener: connectionListenerHashMap.values()) {
                        listener.onRead(readCache);
                    }
                    readCache = null;
                }
            }

            @Override
            public void onError(int code) {
                for (HBConnectionListener listener: connectionListenerHashMap.values()) {
                    listener.onError(code);
                }
                HBUtil.getInstance().disconnectDevice(devcieAddress);
            }
        });
        readThread.start();
        return true;
    }

    public void stopRead() {
        HBLog.i(TAG, "[Connection-" + deviceName + "] Stop read");
        if (readThread != null) {
            readThread.cancel();
        }
    }

    public void registerListener(String key, HBConnectionListener listener) {
        if (state == CONNECTION_STATE_ALIVE) {
            HBLog.i(TAG, "[Connection-" + deviceName + "] Register listener: " + key);
            connectionListenerHashMap.put(key, listener);
        } else {
            HBLog.w(TAG, "[Connection-" + deviceName + "] Register listener failed: connection is not alive");
        }
    }

    public void unregisterListener(String key) {
        HBLog.i(TAG, "[Connection-" + deviceName + "]  unregister listener: " + key);
        connectionListenerHashMap.remove(key);
    }

    public boolean write(byte[] bytes) {
        if (state != CONNECTION_STATE_ALIVE) {
            HBLog.w(TAG, "[Connection-" + deviceName + "] Write failed: connection is not alive");
            return false;
        }
        try {
            HBLog.d(TAG, "[Connection-" + deviceName + "] Write buffer len: " + bytes.length);
            if (outputStream == null) {
                outputStream = socket.getOutputStream();
            }
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            HBLog.e(TAG, "[Connection-" + deviceName + "] Write failed: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void die() {
        HBLog.i(TAG, "[Connection-" + deviceName + "] Die");
        state = CONNECTION_STATE_DEAD;
        for (HBConnectionListener listener: connectionListenerHashMap.values()) {
            listener.onDisconnected(devcieAddress);
        }
        release();
    }

    private void release() {
        connectionListenerHashMap.clear();

        stopRead();

        if (outputStream != null) {
            HBLog.i(TAG, "[Connection-" + deviceName + "] Close output stream");
            try {
                outputStream.close();
            } catch (IOException e) {
                HBLog.e(TAG, "[Connection-" + deviceName + "] Close output stream failed: "
                        + e.getMessage());
            }
            outputStream = null;
        }

        if (socket != null) {
            HBLog.i(TAG, "[Connection-" + deviceName + "] Close socket");
            try {
                socket.close();
            } catch (IOException e) {
                HBLog.e(TAG, "[Connection-" + deviceName + "] Close socket failed: "
                        + e.getMessage());
            }
            socket = null;
        }
    }
}
