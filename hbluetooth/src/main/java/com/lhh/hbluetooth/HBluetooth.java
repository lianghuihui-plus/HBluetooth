package com.lhh.hbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * 蓝牙控制类，单例
 * 封装了基本的连接相关操作
 * 保存蓝牙连接实例
 */
public class HBluetooth {

    private static HBluetooth instance;

    private HBAcceptThread acceptThread;

    private HashMap<String, HBConnection> connectionMap;

    public static HBluetooth getInstance() {
        if (instance == null) {
            synchronized (HBluetooth.class) {
                if (instance == null)
                    instance = new HBluetooth();
            }
        }
        return instance;
    }

    /**
     * 初始化
     */
    public void init() {
        connectionMap = new HashMap<>();
    }

    /**
     * 连接蓝牙设备
     * @param device 目标设备
     * @param uuid UUID
     * @param callback 连接回调
     */
    public void connect(final BluetoothDevice device, final UUID uuid, final ConnectDeviceCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
                    socket.connect();
                    HBConnection connection = new HBConnection(device.getName(), device.getAddress(), socket);
                    saveConnection(connection);
                    callback.onConnected(connection);
                } catch (IOException e) {
                    callback.onError(e);
                }
            }
        }).start();
    }

    /**
     * 开始接受蓝牙设备的连接
     * @param adapter 蓝牙适配器
     * @param name 服务名
     * @param uuid UUID
     * @param listener 回调
     */
    public void startAccept(BluetoothAdapter adapter, String name, UUID uuid, final HBAcceptDeviceListener listener) {
        cancelAccept();
        acceptThread = new HBAcceptThread(adapter, name, uuid, new HBAcceptDeviceListener() {
            @Override
            public void onAccepted(HBConnection connection) {
                saveConnection(connection);
                listener.onAccepted(connection);
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
        acceptThread.start();
    }

    /**
     * 取消接受蓝牙设备连接
     */
    public void cancelAccept() {
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
    }

    /**
     * 获取连接对象
     * @param address 目标设备蓝牙地址
     * @return 蓝牙连接对象
     */
    public HBConnection getConnection(String address) {
        HBConnection connection = null;
        if (connectionMap.containsKey(address)) {
            connection = connectionMap.get(address);
        }
        return connection;
    }

    /**
     * 获取所有连接对象
     * @return 蓝牙连接对象集合
     */
    public HBConnection[] getConnections() {
        return connectionMap.values().toArray(new HBConnection[]{});
    }

    /**
     * 断开所有连接
     */
    public void disconnectAll() {
        for (HBConnection connection : connectionMap.values()) {
            connection.unregisterListener(getClass().getName());
            connection.disconnect();
        }
        connectionMap.clear();
    }

    /**
     * 释放所有资源，此后HBluetooth不可用，需要重新初始化
     */
    public void release() {
        cancelAccept();
        disconnectAll();
        connectionMap = null;
    }

    /**
     * 取消在所有连接对象上的连接回调注册
     * @param key 注册名
     */
    public void unregisterAll(String key) {
        for (HBConnection connection: connectionMap.values()) {
            connection.unregisterListener(key);
        }
    }

    private void saveConnection(HBConnection connection) {
        if (!connectionMap.containsKey(connection.getDeviceAddress())) {
            connection.registerListener(getClass().getName(), connectionListener);
            connectionMap.put(connection.getDeviceAddress(), connection);
        }
    }

    private void removeConnection(String address) {
        connectionMap.remove(address);
    }

    private HBConnectionListener connectionListener = new HBConnectionListener() {
        @Override
        public void onRead(byte[] bytes) {

        }

        @Override
        public void onDisconnected(String address) {
            removeConnection(address);
        }

        @Override
        public void onError(Exception e) {

        }
    };
}
