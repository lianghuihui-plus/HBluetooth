package com.lhh.hbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.List;
import java.util.UUID;

/**
 * 蓝牙工具类
 */
public class HBUtil {

    public interface HBInitCallback {
        void onSuccess();

        void onError(int code);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (HBService.HBBinder)service;
            mCallback.onSuccess();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private volatile static HBUtil sInstance;

    private BluetoothAdapter mAdapter;

    private HBService.HBBinder mBinder;

    private HBInitCallback mCallback;

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

    public static void initialize(Context context) {
        HBApplication.setContext(context);
    }

    /**
     * 初始化工作，获取设备的蓝牙适配器
     */
    public void init(HBInitCallback callback) {
        mCallback = callback;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            callback.onError(HBConstant.ERROR_CODE_BLUETOOTH_ADAPTER_UNAVAILABLE);
            return;
        }
        mAdapter = adapter;
        startService();
    }

    /**
     * 开启并绑定蓝牙服务
     */
    private void startService() {
        Intent intent = new Intent(HBApplication.getContext(), HBService.class);
        HBApplication.getContext().startService(intent);
        HBApplication.getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 解绑并停止蓝牙服务
     */
    private void stopService() {
        Intent intent = new Intent(HBApplication.getContext(), HBService.class);
        HBApplication.getContext().unbindService(mConnection);
        HBApplication.getContext().stopService(intent);
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
    public void requestEnableAdapter(Activity activity) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, HBConstant.REQ_CODE_ACTION_REQUEST_ENABLE);
    }

    /**
     * 申请让蓝牙可被发现
     * @param activity 活动
     * @param duration 可见时间
     */
    public void requestDiscoverable(Activity activity, int duration) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
        activity.startActivityForResult(intent, HBConstant.REQ_CODE_ACTION_REQUEST_DISCOVERABLE);
    }

    /**
     * 开始搜索蓝牙设备
     */
    public boolean startDiscovery() {
        if (mAdapter == null) {
            return false;
        }
        if (!mAdapter.isDiscovering()) {
            return mAdapter.startDiscovery();
        }
        return true;
    }

    /**
     * 停止搜索蓝牙设备
     */
    public boolean cancelDiscovery() {
        if (mAdapter == null) {
            return false;
        }
        if (mAdapter.isDiscovering()) {
            return mAdapter.cancelDiscovery();
        }
        return true;
    }

    /**
     * 开始接受蓝牙设备的连接，若重复调用，旧的线程会被覆盖
     * @param name 服务名
     * @param uuid UUID
     * @param callback 连接的回调
     */
    public void startAccept(String name, UUID uuid, HBAcceptThread.AcceptCallback callback) {
        mBinder.startAccpet(mAdapter, name, uuid, callback);
    }

    /**
     * 停止接受蓝牙设备的连接
     */
    public boolean cancelAccept() {
        return mBinder.cancelAccept();
    }

    /**
     * 作为客户端连接目标蓝牙设备
     * @param address 目标设备地址
     * @param uuid UUID
     * @param callback 连接的回调
     */
    public void connectDevice(String address, UUID uuid, HBConnectThread.HBConnectCallback callback) {
        if (mBinder == null) {
            callback.onError(HBConstant.ERROR_CODE_SERVICE_NOT_START);
            return;
        }
        if (mBinder.isConnectionExist(address)) {
            callback.onError(HBConstant.ERROR_CODE_DEVICE_ALREADY_CONNECTED);
            return;
        }

        //连接蓝牙前需要停止搜索,很重要
        cancelDiscovery();

        BluetoothDevice device;
        try {
            device = mAdapter.getRemoteDevice(address);
        } catch (IllegalArgumentException e) {
            callback.onError(HBConstant.ERROR_CODE_INVALID_DEVICE_ADDRESS);
            return;
        }

        mBinder.connectDevice(device, uuid, callback);
    }

    /**
     * 根据目标设备地址断开与其的连接
     * @param address 目标设备蓝牙地址
     * @return true：成功与目标断开连接；false：未与目标设备连接
     */
    public boolean disconnectDevice(String address) {
        if (mBinder == null) return false;
        return mBinder.disconnectDevice(address);
    }

    /**
     * 根据蓝牙地址获取连接对象
     * @param address 蓝牙地址
     * @return 蓝牙连接对象
     */
    public HBConnection getConnection(String address) {
        if (mBinder == null) return null;
        return mBinder.getConnection(address);
    }

    /**
     * 获取所有的连接对象
     * @return 连接对象列表
     */
    public List<HBConnection> getAllConnection() {
        if (mBinder == null) return null;
        return mBinder.getAllConnection();
    }

    /**
     * 取消在所有连接上注册的观察者
     * @param key 注册名
     */
    public void unregisterAll(String key) {
        mBinder.unregisterAll(key);
    }

    /**
     * 释放所有资源
     */
    public void release() {
        cancelDiscovery();

        stopService();
    }
}
