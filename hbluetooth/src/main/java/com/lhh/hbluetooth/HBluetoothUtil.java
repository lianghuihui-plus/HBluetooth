package com.lhh.hbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.provider.SyncStateContract;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class HBluetoothUtil {

    private static final String TAG = "HBluetoothUtil";

    private static HBluetoothUtil sInstance;

    private BluetoothAdapter mAdapter;

    private HashMap<String, HBluetoothConnection> mConnectionHashMap = new HashMap<>();

    private HBluetoothAcceptTask mAcceptTask;

    public static HBluetoothUtil getInstance() {
        if (sInstance == null) {
            sInstance = new HBluetoothUtil();
        }
        return sInstance;
    }

    public HBluetoothUtil() {

    }

    /**
     * 初始化工作，获取设备的蓝牙适配器
     * @throws HAdapterUnavaliableException 蓝牙适配器不可用
     */
    public void init() throws HAdapterUnavaliableException {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new HAdapterUnavaliableException();
        }
        mAdapter = adapter;
    }

    /**
     * 获取蓝牙适配器对象
     * @return 蓝牙适配器
     */
    public BluetoothAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * 蓝牙适配器是否已启用
     * @return 是否启用
     */
    public boolean isAdapterEnalbed() {
        if (mAdapter == null) {
            return false;
        }
        return mAdapter.isEnabled();
    }

    /**
     * 申请打开蓝牙适配器
     * @param activity 活动
     */
    public void enableAdapter(Activity activity) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, HBluetoothConstant.REQ_CODE_ACTION_REQUEST_ENABLE);
    }

    /**
     * 申请让蓝牙可被发现
     * @param activity 活动
     * @param duration 可见时间
     */
    public void setDiscoverable(Activity activity, int duration) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
        activity.startActivityForResult(intent, HBluetoothConstant.REQ_CODE_ACTION_REQUEST_DISCOVERABLE);
    }

    /**
     * 开始搜索蓝牙设备
     */
    public void startDiscovery() {
        if (mAdapter == null) {
            Log.w(TAG, "startDiscovery: BluetoothAdapter is Undefined!");
        }
        if (!mAdapter.isDiscovering()) {
            mAdapter.startDiscovery();
        }
    }

    /**
     * 停止搜索蓝牙设备
     */
    public void cancelDiscovery() {
        if (mAdapter == null) {
            Log.w(TAG, "startDiscovery: BluetoothAdapter is Undefined!");
        }
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }
    }

    /**
     * 开始接受蓝牙设备的连接，会结束之前已存在的任务
     * @param name 服务名
     * @param uuid UUID
     * @param listener 连接的回调
     */
    public void startAccept(String name, UUID uuid, HServerAcceptListener listener) {
        cancelAccept();

        mAcceptTask = new HBluetoothAcceptTask(mAdapter, name, uuid, new HServerAcceptListener() {
            @Override
            public void onClientConnect(HBluetoothConnection connection) {
                addConnection(connection);
                listener.onClientConnect(connection);
            }

            @Override
            public void onFailed(Exception e) {
                listener.onFailed(e);
            }
        });
        mAcceptTask.execute();
    }

    /**
     * 停止接受蓝牙设备的连接
     */
    public boolean cancelAccept() {
        boolean isSuccess = false;
        if (mAcceptTask != null) {
            isSuccess = mAcceptTask.cancel(true);
            mAcceptTask = null;
        }
        return isSuccess;
    }

    public void connectDevice(String address, UUID uuid, HDeviceConnectListener listener) {
        if (isDeviceAlreadyConnected(address)) {
            listener.onAlreadyConnected();
            return;
        }

        //连接蓝牙前需要停止搜索
        cancelDiscovery();

        BluetoothDevice device;
        try {
            device = mAdapter.getRemoteDevice(address);
        } catch (IllegalArgumentException e) {
            listener.onFailed(HBluetoothConstant.ERROR_CODE_INVALID_DEVICE_ADDRESS);
            return;
        }

        new HDeviceConnectThread(device, uuid, new HDeviceConnectListener() {
            @Override
            public void onSuccess(HBluetoothConnection connection) {
                addConnection(connection);
                listener.onSuccess(connection);
            }

            @Override
            public void onAlreadyConnected() {
                listener.onAlreadyConnected();
            }

            @Override
            public void onFailed(int code) {
                listener.onFailed(code);
            }
        }).start();
    }

    public boolean disconnectDevice(String address) {
        if (mConnectionHashMap.containsKey(address)) {
            HBluetoothConnection connection = mConnectionHashMap.get(address);
            connection.close();
            mConnectionHashMap.remove(connection);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 将蓝牙连接对象保存到Hash中，若相同设备的连接已存在，则销毁当前连接
     * @param connection 蓝牙连接对象
     */
    private void addConnection(HBluetoothConnection connection) {
        if (!mConnectionHashMap.containsKey(connection.getDevcieAddress())) {
            mConnectionHashMap.put(connection.getDevcieAddress(), connection);
        } else {
            connection.close();
        }
    }

    /**
     * 通过BluetoothSocket生成一个蓝牙连接对象并将其保存在Hash中
     * @param socket BluetoothSocket对象
     */
    private void addConnection(BluetoothSocket socket) {
        BluetoothDevice device = socket.getRemoteDevice();
        HBluetoothConnection connection = new HBluetoothConnection(device.getName(),
                device.getAddress(), socket);
        addConnection(connection);
    }

    public boolean isDeviceAlreadyConnected(String address) {
        return mConnectionHashMap.containsKey(address);
    }

    /**
     * 释放所有资源
     */
    public void release() {
        cancelDiscovery();

        cancelAccept();

        for (HBluetoothConnection connection: mConnectionHashMap.values()) {
            connection.close();
        }
    }
}
