package com.lhh.hbluetooth;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: " + action);
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_ON) {
                        showToast("Bluetooth Adapter Is On.");
                    } else if (state == BluetoothAdapter.STATE_OFF) {
                        showToast("Bluetooth Adapter Is Off.");
                    } else if (state == BluetoothAdapter.ERROR) {
                        showToast("Bluetooth Adapter Error!");
                    }
                    break;
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                    int previousMode = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE,
                            BluetoothAdapter.ERROR);
                    int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,
                            BluetoothAdapter.ERROR);
                    Log.d(TAG, "onReceive: previous mode: " + previousMode);
                    Log.d(TAG, "onReceive: mode: " + mode);
                    break;
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    int previousState = intent.getIntExtra(
                            BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, BluetoothAdapter.ERROR);
                    int connectState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE,
                            BluetoothAdapter.ERROR);
                    Log.d(TAG, "onReceive: previous state: " + previousState);
                    Log.d(TAG, "onReceive: state: " + connectState);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    showToast("Start Discovery Device.");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    showToast("Canceled Discovery Device.");
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getName() == null || device.getName().isEmpty()) return;
                    Log.d(TAG, "onReceive: ACTION_FOUND: " + device.getName());
                    BlueDevice blueDevice = new BlueDevice(device.getName(), device.getAddress());
                    if (!listContains(blueDevice)) {
                        mDeviceList.add(blueDevice);
                        mDeviceAdapter.notifyItemInserted(mDeviceList.size()-1);
                    }
                    break;
            }
        }
    };

    private static final String TAG = "MainActivity";

    @OnClick(R.id.open_bluetooth_button) void openBluetooth() {
        if (HBluetoothUtil.getInstance().isAdapterEnalbed()) {
            showToast("Bluetooth Adapter Is Alreay Enabled.");
        } else {
            HBluetoothUtil.getInstance().enableAdapter(this);
        }
    }

    @OnClick(R.id.close_bluetooth_button) void closeBluetooth() {
        if (HBluetoothUtil.getInstance().isAdapterEnalbed()) {
            HBluetoothUtil.getInstance().getAdapter().disable();
        } else {
            showToast("Bluetooth Adapter Is Already Disabled.");
        }
    }

    @OnClick(R.id.discoverable_button) void discoverable() {
        if (HBluetoothUtil.getInstance().isAdapterEnalbed()) {
            HBluetoothUtil.getInstance().setDiscoverable(this, 150);
        } else {
            showToast("Bluetooth Adapter Is Disabled!");
        }
    }

    @OnClick(R.id.start_discovery_button) void startDiscovery() {
        if (HBluetoothUtil.getInstance().isAdapterEnalbed()) {
            HBluetoothUtil.getInstance().startDiscovery();
        } else {
            showToast("Bluetooth Adapter Is Disabled!");
        }
    }

    @OnClick(R.id.cancel_discovery_button) void cancelDiscovery() {
        if (HBluetoothUtil.getInstance().isAdapterEnalbed()) {
            HBluetoothUtil.getInstance().cancelDiscovery();
        } else {
            showToast("Bluetooth Adapter Is Disabled!");
        }
    }

    @BindView(R.id.device_list_view)
    protected RecyclerView mDeviceListView;

    private BluetoothDeviceAdapter mDeviceAdapter;

    private List<BlueDevice> mDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initView();

        registerReceiver();

        try {
            HBluetoothUtil.getInstance().init();
        } catch (HAdapterUnavaliableException e) {
            showToast("Bluetooth is Unavailable!");
            delayFinish(3000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode);
        Log.d(TAG, "onActivityResult: resultCode: " + resultCode);
        switch (requestCode) {
            case HBluetoothConstant.REQ_CODE_ACTION_REQUEST_ENABLE:
                if (resultCode != RESULT_OK) {
                    showToast("You Refuse Permission!");
                }
                break;
            case HBluetoothConstant.REQ_CODE_ACTION_REQUEST_DISCOVERABLE:
                if (resultCode == RESULT_CANCELED) {
                    showToast("You Refuse Permission!");
                } else {
                    showToast("You Device Will Be Discoverable In " + resultCode + " Seconds.");
                }
                break;
        }
    }

    private void initView() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mDeviceListView.setLayoutManager(manager);

        mDeviceAdapter = new BluetoothDeviceAdapter(mDeviceList, device -> {
            Log.d(TAG, "onClick: device name: " + device.getName());
            HBluetoothUtil.getInstance().connectDevice(device.getAddress(), new HDeviceConnectListener() {
                @Override
                public void onSuccess(HBluetoothConnection connection) {
                    Log.d(TAG, "onSuccess: connect device: " + connection.getDeviceName());
                    showToast("Device " + connection.getDeviceName() + " Is Connected Success.");
                }

                @Override
                public void onAlreadyConnected() {
                    showToast("Device Is Already Connected!");
                }

                @Override
                public void onFailed(int code) {
                    showToast("Connect Device Failed With Code " + code);
                }
            });
        });

        mDeviceListView.setAdapter(mDeviceAdapter);
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private boolean listContains (BlueDevice blueDevice) {
        for (BlueDevice device: mDeviceList) {
            if (device.getAddress().equals(blueDevice.getAddress())) {
                return true;
            }
        }
        return false;
    }
}