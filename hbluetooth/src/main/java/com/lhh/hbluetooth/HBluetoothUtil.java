package com.lhh.hbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class HBluetoothUtil {

    private static final String TAG = "HBluetoothUtil";

    private static HBluetoothUtil sInstance;

    private BluetoothAdapter mAdapter;

    private HashMap<String, HBluetoothConnection> mConnectionHashMap = new HashMap<>();

    public static HBluetoothUtil getInstance() {
        if (sInstance == null) {
            sInstance = new HBluetoothUtil();
        }
        return sInstance;
    }

    public HBluetoothUtil() {

    }

    public void init() throws HAdapterUnavaliableException {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new HAdapterUnavaliableException();
        }
        mAdapter = adapter;
    }

    public BluetoothAdapter getAdapter() {
        return mAdapter;
    }

    public boolean isAdapterEnalbed() {
        if (mAdapter == null) {
            return false;
        }
        return mAdapter.isEnabled();
    }

    public void enableAdapter(Activity activity) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, HBluetoothConstant.REQ_CODE_ACTION_REQUEST_ENABLE);
    }

    public void setDiscoverable(Activity activity, int duration) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
        activity.startActivityForResult(intent, HBluetoothConstant.REQ_CODE_ACTION_REQUEST_DISCOVERABLE);
    }

    public void startDiscovery() {
        if (mAdapter == null) {
            Log.w(TAG, "startDiscovery: BluetoothAdapter is Undefined!");
        }
        if (!mAdapter.isDiscovering()) {
            mAdapter.startDiscovery();
        }
    }

    public void cancelDiscovery() {
        if (mAdapter == null) {
            Log.w(TAG, "startDiscovery: BluetoothAdapter is Undefined!");
        }
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }
    }

    public void connectDevice(String address, HDeviceConnectListener listener) {

        if (isDeviceAlreadyConnected(address)) {
            listener.onAlreadyConnected();
            return;
        }

        //连接蓝牙前需要停止搜索
        cancelDiscovery();

        BluetoothDevice device;
        BluetoothSocket socket;
        try {
            device = mAdapter.getRemoteDevice(address);
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString(HBluetoothConstant.SPP_UUID));
        } catch (IllegalArgumentException e) {
            listener.onFailed(HBluetoothConstant.ERROR_CODE_INVALID_DEVICE_ADDRESS);
            return;
        } catch (IOException e) {
            listener.onFailed(HBluetoothConstant.ERROR_CODE_BLUETOOTH_ADAPTER_IS_DISABLED);
            return;
        }
        try {
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
            return;
        }
        HBluetoothConnection connection = new HBluetoothConnection(device.getName(), address, socket);
        mConnectionHashMap.put(address, connection);

        listener.onSuccess(connection);
    }

    public boolean isDeviceAlreadyConnected(String address) {
        return mConnectionHashMap.containsKey(address);
    }
}
