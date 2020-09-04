package com.lhh.hbluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * 蓝牙服务类
 */
public class HBService extends Service {

    public class HBBinder extends Binder {
        public void startAccpet(BluetoothAdapter adapter, String name, UUID uuid,
                                HBAcceptThread.AcceptCallback callback) {
            HBService.this.startAccept(adapter, name, uuid, callback);
        }

        public void cancelAccept() {
            HBService.this.cancelAccept();
        }

        public void connectDevice(BluetoothDevice device, UUID uuid,
                                  HBConnectThread.HBConnectCallback callback) {
            HBService.this.connectDevice(device, uuid, callback);
        }

        public void disconnectDevice(String address) {
            HBService.this.disconnectDevice(address);
        }

        public HBConnection getConnection(String address) {
            return HBService.this.getConnection(address);
        }

        public List<HBConnection> getAllConnection() {
            return HBService.this.getAllConnection();
        }

        public void unregisterAll(String key) {
            HBService.this.unregisterAll(key);
        }

        public boolean isConnectionExist(String address) {
            return HBService.this.isConnectionExist(address);
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_OFF) {
                        cancelAccept();
                        disconnectAllDevice();
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    BluetoothDevice disconnectedDevice = intent
                            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    removeConnection(disconnectedDevice.getAddress());
                    break;
            }
        }
    };

    private static final String TAG = "HBService";

    private HBBinder binder = new HBBinder();

    private HBAcceptThread acceptThread;

    private HashMap<String, HBConnection> connectionHashMap = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(broadcastReceiver);

        cancelAccept();

        disconnectAllDevice();
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * 开始接受蓝牙设备的连接，若重复调用，旧的线程会被覆盖
     * @param adapter 蓝牙适配器
     * @param name 服务名
     * @param uuid UUID
     * @param callback 连接的回调
     */
    public void startAccept(BluetoothAdapter adapter, String name, UUID uuid, HBAcceptThread.AcceptCallback callback) {
        cancelAccept();
        HBLog.i(TAG, "Start accept device");
        acceptThread = new HBAcceptThread(adapter, name, uuid, new HBAcceptThread.AcceptCallback() {
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
        acceptThread.start();
    }

    /**
     * 停止接受蓝牙设备的连接
     */
    public void cancelAccept() {
        HBLog.i(TAG, "Cancel accept device");
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
    }

    /**
     * 作为客户端连接目标蓝牙设备
     * @param device 目标蓝牙设备
     * @param uuid UUID
     * @param callback 连接的回调
     */
    public void connectDevice(BluetoothDevice device, UUID uuid, HBConnectThread.HBConnectCallback callback) {

        new HBConnectThread(device, uuid, new HBConnectThread.HBConnectCallback() {
            @Override
            public void onConnected(HBConnection connection) {
                addConnection(connection);
                callback.onConnected(connection);
            }

            @Override
            public void onError(int code) {
                callback.onError(code);
            }
        }).start();
    }

    /**
     * 根据目标设备地址断开与其的连接
     * @param address 目标设备蓝牙地址
     */
    public void disconnectDevice(String address) {
        HBLog.i(TAG, "Disconnect device: " + address);
        if (!isConnectionExist(address)) {
            HBLog.i(TAG, "Device not connected: " + address);
            return;
        }
        removeConnection(address);
    }

    /**
     * 与所有已连接设备断开连接
     */
    public void disconnectAllDevice() {
        HBLog.i(TAG, "Disconnect from all devices");
        for (HBConnection connection: connectionHashMap.values()) {
            connection.die();
        }
        connectionHashMap.clear();
        connectionHashMap = null;
    }

    /**
     * 将蓝牙连接对象保存到Hash中，若相同设备的连接已存在，则覆盖连接
     * @param connection 蓝牙连接对象
     */
    private void addConnection(HBConnection connection) {
        HBLog.i(TAG, "Add connection: " + connection.getDeviceName());
        if (!isConnectionExist(connection.getDevcieAddress())) {
            connectionHashMap.put(connection.getDevcieAddress(), connection);
        } else {
            HBLog.w(TAG, "Connection is already exist");
            disconnectDevice(connection.getDevcieAddress());
            addConnection(connection);
        }
    }

    /**
     * 移除连接对象，此操作会断开连接
     * @param key 蓝牙地址
     */
    private void removeConnection(String key) {
        if (isConnectionExist(key)) {
            connectionHashMap.get(key).die();
            connectionHashMap.remove(key);
        }
    }

    /**
     * 根据蓝牙地址，获取已连接对象
     * @param address 蓝牙地址
     * @return 蓝牙连接对象
     */
    public HBConnection getConnection(String address) {
        if (isConnectionExist(address)) {
            return connectionHashMap.get(address);
        }
        return null;
    }

    public List<HBConnection> getAllConnection() {
        return new ArrayList<>(connectionHashMap.values());
    }

    /**
     * 取消在所有连接上注册的观察者
     * @param key 注册名
     */
    public void unregisterAll(String key) {
        HBLog.i(TAG, "Unregister all listener of " + key);
        for (HBConnection connection: connectionHashMap.values()) {
            connection.unregisterListener(key);
        }
    }

    /**
     * 设备是否已建立连接
     * @param address 目标设备蓝牙地址
     * @return 是否已建立连接
     */
    public boolean isConnectionExist(String address) {
        return connectionHashMap.containsKey(address);
    }
}
