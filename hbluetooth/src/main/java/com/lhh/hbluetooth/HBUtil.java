package com.lhh.hbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;
import java.util.UUID;

/**
 * 蓝牙工具类
 */
public class HBUtil {

    private static final String TAG = "HBluetoothUtil";

    private volatile static HBUtil sInstance;

    private BluetoothAdapter mAdapter;

    private HashMap<String, HBConnection> mConnectionHashMap = new HashMap<>();

    private HBAcceptThread mAcceptThread;

    public static HBUtil getInstance() {
        if (sInstance == null) {
            synchronized (HBUtil.class) {
                if (sInstance == null) {
                    sInstance = new HBUtil();
                }
            }
        }
        return sInstance;
    }

    public HBUtil() {

    }

    /**
     * 初始化工作，获取设备的蓝牙适配器
     * @throws HBAdapterUnavailableException 蓝牙适配器不可用
     */
    public void init() throws HBAdapterUnavailableException {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new HBAdapterUnavailableException();
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
    public boolean isAdapterEnabled() {
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
        activity.startActivityForResult(intent, HBConstant.REQ_CODE_ACTION_REQUEST_ENABLE);
    }

    /**
     * 申请让蓝牙可被发现
     * @param activity 活动
     * @param duration 可见时间
     */
    public void setDiscoverable(Activity activity, int duration) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
        activity.startActivityForResult(intent, HBConstant.REQ_CODE_ACTION_REQUEST_DISCOVERABLE);
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
     * 开始接受蓝牙设备的连接，若重复调用，旧的线程会被覆盖
     * @param name 服务名
     * @param uuid UUID
     * @param callback 连接的回调
     */
    public void startAccept(String name, UUID uuid, HBAcceptThread.AcceptCallback callback) {
        cancelAccept();

        mAcceptThread = new HBAcceptThread(mAdapter, name, uuid, new HBAcceptThread.AcceptCallback() {
            @Override
            public void onClientConnected(HBConnection connection) {
                addConnection(connection);
                callback.onClientConnected(connection);
            }

            @Override
            public void onFailed(int code) {
                callback.onFailed(code);
            }
        });
        mAcceptThread.start();
    }

    /**
     * 停止接受蓝牙设备的连接
     */
    public boolean cancelAccept() {
        boolean isSuccess = false;
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
            isSuccess = true;
        }
        return isSuccess;
    }

    /**
     * 作为客户端连接目标蓝牙设备
     * @param address 目标设备地址
     * @param uuid UUID
     * @param callback 连接的回调
     */
    public void connectDevice(String address, UUID uuid, HBConnectThread.HBConnectCallback callback) {
        if (isDeviceAlreadyConnected(address)) {
            callback.onFailed(HBConstant.ERROR_CODE_DEVICE_ALREADY_CONNECTED);
            return;
        }

        //连接蓝牙前需要停止搜索,很重要
        cancelDiscovery();

        BluetoothDevice device;
        try {
            device = mAdapter.getRemoteDevice(address);
        } catch (IllegalArgumentException e) {
            callback.onFailed(HBConstant.ERROR_CODE_INVALID_DEVICE_ADDRESS);
            return;
        }

        new HBConnectThread(device, uuid, new HBConnectThread.HBConnectCallback() {
            @Override
            public void onSuccess(HBConnection connection) {
                addConnection(connection);
                callback.onSuccess(connection);
            }

            @Override
            public void onFailed(int code) {
                callback.onFailed(code);
            }
        }).start();
    }

    /**
     * 根据目标设备地址断开与其的连接
     * @param address 目标设备蓝牙地址
     * @return true：成功与目标断开连接；false：未与目标设备连接
     */
    public boolean disconnectDevice(String address) {
        if (mConnectionHashMap.containsKey(address)) {
            HBConnection connection = mConnectionHashMap.get(address);
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
    private void addConnection(HBConnection connection) {
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
        HBConnection connection = new HBConnection(device.getName(),
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

        for (HBConnection connection: mConnectionHashMap.values()) {
            connection.close();
        }
    }
}
