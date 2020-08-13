package com.lhh.hbluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;

/**
 * 蓝牙连接对象，使用了观察者模式
 */
public class HBConnection {

    public static final int CONNECTION_STATE_DEAD = 0;

    public static final int CONNECTION_STATE_ALIVE = 1;

    private String deviceName;

    private String devcieAddress;

    private int state;

    private BluetoothSocket socket;

    private OutputStream outputStream;

    private HBReadThread readThread;

    private HashMap<String, HBConnectionListener> connectionListenerHashMap = new HashMap<>();

    private byte[] readCache;

    public HBConnection(String deviceName, String devcieAddress, BluetoothSocket socket) {
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
        if (readThread == null && state == CONNECTION_STATE_ALIVE) {
            readThread = new HBReadThread(socket, new HBReadListener() {
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
                }
            });
            readThread.start();
            return true;
        }
        return false;
    }

    public void stopRead() {
        if (readThread != null) {
            readThread.cancel();
        }
    }

    public void registerListener(String key, HBConnectionListener listener) {
        if (state == CONNECTION_STATE_ALIVE) {
            connectionListenerHashMap.put(key, listener);
        }
    }

    public void unregisterListener(String key) {
        connectionListenerHashMap.remove(key);
    }

    public boolean write(byte[] bytes) {
        if (state == CONNECTION_STATE_DEAD) return false;
        try {
            if (outputStream == null) {
                outputStream = socket.getOutputStream();
            }
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void die() {
        state = CONNECTION_STATE_DEAD;
        for (HBConnectionListener listener: connectionListenerHashMap.values()) {
            listener.onDisconnected(devcieAddress);
        }
        release();
    }

    private void release() {
        stopRead();

        connectionListenerHashMap.clear();

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
    }
}
