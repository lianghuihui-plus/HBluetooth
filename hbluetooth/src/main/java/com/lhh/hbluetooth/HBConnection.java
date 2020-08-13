package com.lhh.hbluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * 蓝牙连接对象，使用了观察者模式
 * 与蓝牙设备建立连接后，将会通过连接回调返回一个该对象
 * 您并不需要在建立连接后保存该对象，因为您可以随时根据蓝牙地址从{@link HBUtil}中获取到该连接对象
 * 您应该通过该对象对当前的连接进行读、写等操作
 * 您需要通过注册一个观察者来获取该对象下发的通知信息
 * 一个连接对象可以注册多个观察者
 * 连接对象被告知连接已失效后会通知所有观察者，然后取消所有的注册，最后释放资源
 * ！！！请不要直接通过调用连接对象的die()方法来断开该连接！！！
 * 应该通过{@link HBUtil}disconnectDevice()方法来断开连接
 * 因为直接通过die()方法虽然可以断开一个连接，但该连接的实例仍然作为可用的连接被保存起来
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
