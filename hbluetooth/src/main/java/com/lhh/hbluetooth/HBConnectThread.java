package com.lhh.hbluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * 客户端连接线程
 */
public class HBConnectThread extends Thread {

    public interface HBConnectCallback {

        void onConnected(HBConnection connection);

        void onError(int code);
    }

    private static final String TAG = "HBConnectThread";

    private BluetoothDevice device;

    private UUID uuid;

    private HBConnectCallback callback;

    public HBConnectThread(BluetoothDevice device, UUID uuid, HBConnectCallback callback) {
        HBLog.i(TAG, "[ConnectThread-" + device.getName() + "] Connect thread create: "
                + uuid);
        this.device = device;
        this.uuid = uuid;
        this.callback = callback;
    }

    @Override
    public void run() {
        super.run();
        HBLog.i(TAG, "[ConnectThread-" + device.getName() + "] Connect to device");
        BluetoothSocket socket;
        try {
            socket = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            HBLog.e(TAG, "[ConnectThread-" + device.getName() + "] Get socket failed: "
                    + e.getMessage());
            callback.onError(HBConstant.ERROR_CODE_BLUETOOTH_ADAPTER_IS_DISABLED);
            return;
        }
        try {
            socket.connect();
        } catch (IOException e) {
            HBLog.e(TAG, "[ConnectThread-" + device.getName() + "] Connect device failed: "
                    + e.getMessage());
            try {
                socket.close();
            } catch (IOException closeException) {
                HBLog.e(TAG, "[ConnectThread-" + device.getName() + "] Close device failed: "
                        + closeException.getMessage());
            }
            callback.onError(HBConstant.ERROR_CODE_CONNECT_DEVICE_FAILED);
            return;
        }
        HBConnection connection = new HBConnection(device.getName(), device.getAddress(), socket);
        callback.onConnected(connection);
    }
}
