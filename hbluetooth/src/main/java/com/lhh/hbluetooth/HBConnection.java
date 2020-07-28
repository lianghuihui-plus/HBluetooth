package com.lhh.hbluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;

/**
 * 蓝牙连接对象
 * 与蓝牙设备建立连接后，将会通过连接回调返回一个该对象
 * 您应该通过该对象对当前的连接进行读、写等操作
 * 在开始读操作前，您应该先向该对象注册一个{@link HBReadListener}对象来接收蓝牙数据
 * 您可以注册多个监听对象，但这可能是没有必要的
 * 您应该在需要的时候注册监听对象，在不需要的时候卸载监听对象
 */
public class HBConnection {

    private String deviceName;

    private String devcieAddress;

    private BluetoothSocket socket;

    private OutputStream outputStream;

    private HBReadThread readThread;

    private HashMap<String, HBReadListener> readListenerHashMap = new HashMap<>();

    private byte[] readCache;

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

    public void startRead() {
        if (readThread == null) {
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
                    if (readListenerHashMap.size() > 0) {
                        for (HBReadListener listener: readListenerHashMap.values()) {
                            listener.onRead(readCache);
                        }
                        readCache = null;
                    }
                }

                @Override
                public void onFailed() {
                    for (HBReadListener listener: readListenerHashMap.values()) {
                        listener.onFailed();
                    }
                }
            });
            readThread.start();
        }
    }

    public void stopRead() {
        if (readThread != null) {
            readThread.cancel();
        }
    }

    public void registerReadListener(String key, HBReadListener listener) {
        if (!readListenerHashMap.containsKey(key)) {
            readListenerHashMap.put(key, listener);
        }
    }

    public void unregisterReadListener(String key) {
        readListenerHashMap.remove(key);
    }

    public void write(byte[] bytes) {
        try {
            if (outputStream == null) {
                outputStream = socket.getOutputStream();
            }
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
     }

    public void close() {
        stopRead();

        readListenerHashMap.clear();

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
