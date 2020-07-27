package com.lhh.hbluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

public class HDeviceConnectThread extends Thread {

    private BluetoothDevice device;

    private UUID uuid;

    private HDeviceConnectListener listener;

    public HDeviceConnectThread(BluetoothDevice device, UUID uuid, HDeviceConnectListener listener) {
        this.device = device;
        this.uuid = uuid;
        this.listener = listener;
    }

    @Override
    public void run() {
        super.run();

        BluetoothSocket socket;
        try {
            socket = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            listener.onFailed(HBluetoothConstant.ERROR_CODE_BLUETOOTH_ADAPTER_IS_DISABLED);
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
            listener.onFailed(HBluetoothConstant.ERROR_CODE_CONNECT_DEVICE_FAILED);
        }
        HBluetoothConnection connection = new HBluetoothConnection(device.getName(), device.getAddress(), socket);
        listener.onSuccess(connection);
    }
}
