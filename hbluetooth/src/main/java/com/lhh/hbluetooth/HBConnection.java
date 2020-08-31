package com.lhh.hbluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import static android.content.ContentValues.TAG;

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

    private java.io.OutputStream outputStream;

    private HBReadThread readThread;

    private ConcurrentHashMap<String, HBConnectionListener> connectionListenerHashMap
            = new ConcurrentHashMap<>();

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
                    HBUtil.getInstance().disconnectDevice(devcieAddress);
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
            android.util.Log.i(TAG, "write: start");
            outputStream.write(bytes);
            android.util.Log.i(TAG, "write: end");
            outputStream.flush();
            android.util.Log.i(TAG, "flush: end");
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
        connectionListenerHashMap.clear();

        stopRead();

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
