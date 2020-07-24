package com.lhh.hbluetooth;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.d(TAG, "onReceive: ACTION_DISCOVERY_STARTED");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.d(TAG, "onReceive: ACTION_DISCOVERY_FINISHED");
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getName() == null || device.getName().isEmpty()) return;
                    Log.d(TAG, "onReceive: ACTION_FOUND: " + device.getName());
                    BlueDevice blueDevice = new BlueDevice(device.getName(), device.getAddress());
                    if (!mDeviceList.contains(blueDevice)) {
                        mDeviceList.add(blueDevice);
                        mDeviceAdapter.notifyItemInserted(mDeviceList.size()-1);
                    }
                    break;
            }
        }
    };

    private static final String TAG = "MainActivity";

    @OnClick(R.id.open_bluetooth_button) void openBluetooth() {
        HBluetoothUtil.getInstance().enableAdapter(this);
    }

    @OnClick(R.id.close_bluetooth_button) void closeBluetooth() {
        HBluetoothUtil.getInstance().getAdapter().disable();
    }

    @OnClick(R.id.start_discovery_button) void startDiscovery() {
        HBluetoothUtil.getInstance().startDiscovery();
    }

    @OnClick(R.id.cancel_discovery_button) void cancelDiscovery() {
        HBluetoothUtil.getInstance().cancelDiscovery();
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
        if (requestCode == HBluetoothConstant.REQ_CODE_ACTION_REQUEST_ENABLE) {
            if (resultCode == RESULT_OK) {
                showToast("Enable Bluetooth Success.");
            } else {
                showToast("Enable Bluetooth Failed.");
            }
        }
    }

    private void initView() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mDeviceListView.setLayoutManager(manager);

        mDeviceAdapter = new BluetoothDeviceAdapter(mDeviceList, new BluetoothDeviceItemOnClickListener() {
            @Override
            public void onClick(BlueDevice device) {
                Log.d(TAG, "onClick: device name: " + device.getName());
            }
        });

        mDeviceListView.setAdapter(mDeviceAdapter);
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }
}