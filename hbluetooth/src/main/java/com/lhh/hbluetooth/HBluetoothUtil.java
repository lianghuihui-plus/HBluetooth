package com.lhh.hbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;

public class HBluetoothUtil {

    private static final String TAG = "HBluetoothUtil";

    private static HBluetoothUtil sInstance;

    private BluetoothAdapter mAdapter;

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

    public void enableAdapter(Activity activity) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, HBluetoothConstant.REQ_CODE_ACTION_REQUEST_ENABLE);
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
}
