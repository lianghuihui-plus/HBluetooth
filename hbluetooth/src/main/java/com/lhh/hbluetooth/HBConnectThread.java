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

    private BluetoothDevice device;

    private UUID uuid;

    private HBConnectCallback callback;

    public HBConnectThread(BluetoothDevice device, UUID uuid, HBConnectCallback callback) {
        this.device = device;
        this.uuid = uuid;
        this.callback = callback;
    }

    @Override
    public void run() {
        super.run();

        BluetoothSocket socket;
        try {
            socket = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            callback.onError(HBConstant.ERROR_CODE_BLUETOOTH_ADAPTER_IS_DISABLED);
            return;
        }
        try {
            socket.connect();
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
            callback.onError(HBConstant.ERROR_CODE_CONNECT_DEVICE_FAILED);
            return;
        }
        HBConnection connection = new HBConnection(device.getName(), device.getAddress(), socket);
        callback.onConnected(connection);
    }
}
