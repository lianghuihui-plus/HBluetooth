package com.lhh.hbluetooth;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends BaseActivity {

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            Log.d(TAG, "onReceive: " + action);
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_ON) {
                        showToast("Bluetooth Adapter Is On.");
                    } else if (state == BluetoothAdapter.STATE_OFF) {
                        showToast("Bluetooth Adapter Is Off.");
                        mDeviceList.clear();
                        runOnUiThread(() -> mDeviceAdapter.notifyDataSetChanged());
                    } else if (state == BluetoothAdapter.ERROR) {
                        showToast("Bluetooth Adapter Error!");
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    showToast("Start Discovery Device.");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    showToast("Canceled Discovery Device.");
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device == null || device.getName() == null || device.getName().isEmpty()) return;
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
        if (HBUtil.getInstance().isAdapterEnabled()) {
            showToast("Bluetooth Adapter Is Alreay Enabled.");
        } else {
            HBUtil.getInstance().requestEnableAdapter(this);
        }
    }

    @OnClick(R.id.close_bluetooth_button) void closeBluetooth() {
        if (HBUtil.getInstance().isAdapterEnabled()) {
            HBUtil.getInstance().getAdapter().disable();
        } else {
            showToast("Bluetooth Adapter Is Already Disabled.");
        }
    }

    @OnClick(R.id.discoverable_button) void discoverable() {
        if (HBUtil.getInstance().isAdapterEnabled()) {
            HBUtil.getInstance().requestDiscoverable(this, 150);
        } else {
            showToast("Bluetooth Adapter Is Disabled!");
        }
    }

    @OnClick(R.id.start_discovery_button) void startDiscovery() {

        String[] neededPermissions = new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        if (!checkPermissions(neededPermissions)) {
            requestPermissions(neededPermissions, 0);
        } else {
            if (HBUtil.getInstance().isAdapterEnabled()) {
                if (!HBUtil.getInstance().startDiscovery()) {
                    showToast("Start Discovery Failed!");
                }
            } else {
                showToast("Bluetooth Adapter Is Disabled!");
            }
        }
    }

    @OnClick(R.id.cancel_discovery_button) void cancelDiscovery() {
        if (HBUtil.getInstance().isAdapterEnabled()) {
            if (!HBUtil.getInstance().cancelDiscovery()) {
                showToast("Cancel Discovery Failed!");
            }
        } else {
            showToast("Bluetooth Adapter Is Disabled!");
        }
    }

    @OnClick(R.id.start_accept) void startAccept() {
        if (HBUtil.getInstance().isAdapterEnabled()) {
            HBUtil.getInstance().startAccept("server", UUID.fromString(Constant.SPP_UUID),
                    new HBAcceptThread.AcceptCallback() {
                @Override
                public void onClientConnected(HBConnection connection) {
                    showToast("Device Connected: " + connection.getDeviceName());
                    connection.startRead();
                    connection.registerListener(MainActivity.class.getName(), new HBConnectionListener() {
                        @Override
                        public void onDisconnected(String address) {
                            refreshDeviceState(address, BlueDeviceStatus.DISCONNECTED);
                        }

                        @Override
                        public void onRead(byte[] cache) {

                        }

                        @Override
                        public void onError(int code) {

                        }
                    });
                    if (listContains(connection.getDevcieAddress())) {
                        refreshDeviceState(connection.getDevcieAddress(), BlueDeviceStatus.CONNECTED);
                    } else {
                        BlueDevice newDevice = new BlueDevice(connection.getDeviceName(),
                                connection.getDevcieAddress());
                        newDevice.setStatus(BlueDeviceStatus.CONNECTED);
                        mDeviceList.add(newDevice);
                        runOnUiThread(() -> mDeviceAdapter.notifyDataSetChanged());
                    }
                }

                @Override
                public void onFailed(int code) {
                    showToast("Accept Error With Code: " + code);
                }
            });
            showToast("Start Accept Client.");
        } else {
            showToast("Bluetooth Adapter Is Disabled!");
        }
    }

    @OnClick(R.id.cancel_accept) void cancelAccept() {
        if (HBUtil.getInstance().isAdapterEnabled()) {
            showToast("Cancel Accept.");
            HBUtil.getInstance().cancelAccept();
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

        HBUtil.getInstance().init(new HBUtil.HBInitCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int code) {
                delayFinish(3000);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBroadcastReceiver);

        HBUtil.getInstance().release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode);
        Log.d(TAG, "onActivityResult: resultCode: " + resultCode);
        switch (requestCode) {
            case HBConstant.REQ_CODE_ACTION_REQUEST_ENABLE:
                if (resultCode != RESULT_OK) {
                    showToast("You Refuse Permission!");
                }
                break;
            case HBConstant.REQ_CODE_ACTION_REQUEST_DISCOVERABLE:
                if (resultCode == RESULT_CANCELED) {
                    showToast("You Refuse Permission!");
                } else {
                    showToast("You Device Will Be Discoverable In " + resultCode + " Seconds.");
                }
                break;
        }
    }

    @Override
    protected void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == 0) {
            if (isAllGranted) {
                startDiscovery();
            }
        }
    }

    private void initView() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mDeviceListView.setLayoutManager(manager);

        mDeviceAdapter = new BluetoothDeviceAdapter(mDeviceList, device -> {
            Log.d(TAG, "onClick: device name: " + device.getName());

            if (device.getStatus() == BlueDeviceStatus.DISCONNECTED) {
                refreshDeviceState(device.getAddress(), BlueDeviceStatus.CONNECTING);
                HBUtil.getInstance().connectDevice(
                        device.getAddress(),
                        UUID.fromString(Constant.SPP_UUID),
                        new HBConnectThread.HBConnectCallback() {
                            @Override
                            public void onConnected(HBConnection connection) {
                                Log.d(TAG, "onSuccess: connect device: " +
                                        connection.getDeviceName());
                                connection.startRead();
                                refreshDeviceState(connection.getDevcieAddress(),
                                        BlueDeviceStatus.CONNECTED);
                                showToast("Device " + connection.getDeviceName()
                                        + " Is Connected Success.");
                                connection.registerListener(MainActivity.class.getName(), new HBConnectionListener() {
                                    @Override
                                    public void onDisconnected(String address) {
                                        refreshDeviceState(address, BlueDeviceStatus.DISCONNECTED);
                                    }

                                    @Override
                                    public void onRead(byte[] cache) {
                                        Log.d(TAG, "onRead: " + cache.toString());
                                    }

                                    @Override
                                    public void onError(int code) {
                                        Log.d(TAG, "onError: " + code);
                                    }
                                });
                            }

                            @Override
                            public void onError(int code) {
                                showToast("Connect Device Failed With Code " + code);
                                refreshDeviceState(device.getAddress(), BlueDeviceStatus.DISCONNECTED);
                            }
                        });
            } else if (device.getStatus() == BlueDeviceStatus.CONNECTING) {
                showToast("Device Is Connecting!");
            } else if (device.getStatus() == BlueDeviceStatus.CONNECTED) {
//                if (HBUtil.getInstance().disconnectDevice(device.getAddress())) {
//                    refreshDeviceState(device.getAddress(), BlueDeviceStatus.DISCONNECTED);
//                } else {
//                    showToast("Disconnect Device Failed!");
//                }
                Intent intent = new Intent(MainActivity.this, WriteActivity.class);
                intent.putExtra("address", device.getAddress());
                startActivity(intent);
            }
        });

        mDeviceListView.setAdapter(mDeviceAdapter);
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
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

    private boolean listContains(String address) {
        for (BlueDevice device: mDeviceList) {
            if (device.getAddress().equals(address)) {
                return true;
            }
        }
        return false;
    }

    private void refreshDeviceState(String address, BlueDeviceStatus state) {
        for (BlueDevice device: mDeviceList) {
            if (device.getAddress().equals(address)) {
                device.setStatus(state);
                break;
            }
        }
        runOnUiThread(() -> mDeviceAdapter.notifyDataSetChanged());
    }
}