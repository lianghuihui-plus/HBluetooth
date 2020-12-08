package com.lhh.hbluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final int REQUEST_BLUETOOTH_ADAPTER_ENABLE = 0x0101;
    private static final int REQUEST_BLUETOOTH_ADAPTER_DISCOVERABLE = 0x0102;
    private static final int REQUEST_NEEDED_PERMISSIONS = 0x0103;
    private static final String[] NEEDED_PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;
    @BindView(R.id.btn_enabled_adapter)
    public Button enabledAdapterButton;
    @BindView(R.id.btn_disabled_adapter)
    public Button disabledAdapterButton;
    @BindView(R.id.btn_start_discovery)
    public Button startDiscoveryButton;
    @BindView(R.id.btn_cancel_discovery)
    public Button cancelDiscoveryButton;
    @BindView(R.id.btn_discoverable)
    public Button discoverableButton;
    @BindView(R.id.btn_start_accept)
    public Button startAcceptButton;
    @BindView(R.id.btn_cancel_accept)
    public Button cancelAcceptButton;

    private BluetoothDeviceAdapter adapter;
    private List<MyBluetoothDevice> deviceList = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        HBUtil.setLogLevel(HBUtil.HLogLevel.D);

        initView();
        registerReceiver();

        checkPermissions();

        BluetoothManager manager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = manager.getAdapter();
        if (bluetoothAdapter == null) {
            showLongToast("Device not support bluetooth!");
            finish();
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BLUETOOTH_ADAPTER_ENABLE);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_enabled_adapter:
                if (!bluetoothAdapter.isEnabled()) {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_BLUETOOTH_ADAPTER_ENABLE);
                } else {
                    showToast("蓝牙已是打开状态！");
                }
                break;
            case R.id.btn_disabled_adapter:
                if (bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.disable();
                    showToast("蓝牙已关闭");
                } else {
                    showToast("蓝牙已是关闭状态！");
                }
                break;
            case R.id.btn_start_discovery:
                if (!bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.startDiscovery();
                    showToast("开始扫描设备");
                } else {
                    showToast("蓝牙正在扫描设备，请勿重复操作！");
                }
                break;
            case R.id.btn_cancel_discovery:
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                    showToast("停止扫描设备");
                } else {
                    showToast("蓝牙未开始扫描设备！");
                }
                break;
            case R.id.btn_discoverable:
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                startActivityForResult(intent, REQUEST_BLUETOOTH_ADAPTER_DISCOVERABLE);
                break;
            case R.id.btn_start_accept:
                HBluetooth.getInstance().startAccept(
                        bluetoothAdapter,
                        "serverName",
                        UUID.fromString(SPP_UUID),
                        acceptListener
                );
                showToast("开始等待设备连接");
                break;
            case R.id.btn_cancel_accept:
                HBluetooth.getInstance().cancelAccept();
                showToast("停止接收设备连接");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BLUETOOTH_ADAPTER_ENABLE) {
            if (resultCode != RESULT_OK)  {
                showToast("请求打开蓝牙被拒绝！");
            }
        } else if (requestCode == REQUEST_BLUETOOTH_ADAPTER_DISCOVERABLE) {
            if (resultCode == RESULT_CANCELED) {
                showToast("请求蓝牙可被发现被拒绝！");
            } else {
                showToast("蓝牙设备在" + resultCode + "秒内可见");
            }
        }
    }

    @Override
    protected void afterRequestPermission(int requestCode, boolean allGranted) {
        super.afterRequestPermission(requestCode, allGranted);
        if (requestCode == REQUEST_NEEDED_PERMISSIONS) {
            if (!allGranted) {
                showToast("申请权限被拒绝！");
                finish();
            }
        }
    }

    private void checkPermissions() {
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, REQUEST_NEEDED_PERMISSIONS);
        }
    }

    private void initView() {
        adapter = new BluetoothDeviceAdapter(deviceList, onDeviceItemClickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        enabledAdapterButton.setOnClickListener(this);
        disabledAdapterButton.setOnClickListener(this);
        startDiscoveryButton.setOnClickListener(this);
        cancelDiscoveryButton.setOnClickListener(this);
        discoverableButton.setOnClickListener(this);
        startAcceptButton.setOnClickListener(this);
        cancelAcceptButton.setOnClickListener(this);
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, intentFilter);
    }

    private void unregisterReceiver() {
        unregisterReceiver(receiver);
    }

    private boolean isDeviceExist(BluetoothDevice device) {
        for (MyBluetoothDevice myDevice : deviceList) {
            if (myDevice.getDevice().getAddress().equals(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    private BluetoothDeviceAdapter.OnDeviceItemClickListener onDeviceItemClickListener = new BluetoothDeviceAdapter.OnDeviceItemClickListener() {
        @Override
        public void onClick(MyBluetoothDevice device) {
            if (device.getStatus() == MyBluetoothDevice.BluetoothDeviceStatus.Connected) {
                HBConnection connection = HBluetooth.getInstance().getConnection(device.getDevice().getAddress());
                if (connection != null) {
                    connection.disconnect();
                    device.setStatus(MyBluetoothDevice.BluetoothDeviceStatus.Disconnect);
                    adapter.notifyDataSetChanged();
                }
            } else if (device.getStatus() == MyBluetoothDevice.BluetoothDeviceStatus.Disconnect) {
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                device.setStatus(MyBluetoothDevice.BluetoothDeviceStatus.Connecting);
                adapter.notifyDataSetChanged();
                HBluetooth.getInstance().connect(device.getDevice(), UUID.fromString(SPP_UUID), new ConnectDeviceCallback() {
                    @Override
                    public void onConnected(HBConnection connection) {
                        Log.i(TAG, "onConnected: " + connection.getDeviceName());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                device.setStatus(MyBluetoothDevice.BluetoothDeviceStatus.Connected);
                                adapter.notifyDataSetChanged();
                            }
                        });
                        connection.registerListener(getClass().getName(), new HBConnectionListener() {
                            @Override
                            public void onRead(byte[] bytes) {
                                String string = new String(bytes);
                                Log.i(TAG, "onRead: " + string);
                            }

                            @Override
                            public void onDisconnected(String address) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        device.setStatus(MyBluetoothDevice.BluetoothDeviceStatus.Disconnect);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "onError: ", e);
                            }
                        });
                        connection.startRead();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "onError: ", e);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                device.setStatus(MyBluetoothDevice.BluetoothDeviceStatus.Disconnect);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        }
    };

    private HBAcceptDeviceListener acceptListener = new HBAcceptDeviceListener() {
        @Override
        public void onAccepted(HBConnection connection) {

        }

        @Override
        public void onError(Exception e) {

        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: action: " + action);
            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (!TextUtils.isEmpty(device.getName()) && !isDeviceExist(device)) {
                        MyBluetoothDevice myDevice = new MyBluetoothDevice(device, MyBluetoothDevice.BluetoothDeviceStatus.Disconnect);
                        deviceList.add(myDevice);
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };
}