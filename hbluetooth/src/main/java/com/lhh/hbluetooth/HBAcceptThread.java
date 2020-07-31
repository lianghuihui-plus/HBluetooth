package com.lhh.hbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * 服务端等待连接线程
 */
public class HBAcceptThread extends Thread {

    public interface AcceptCallback {

        void onClientConnected(HBConnection connection);

        void onFailed(int code);
    }

    private BluetoothAdapter adapter;

    private String name;

    private UUID uuid;

    private AcceptCallback callback;

    private BluetoothServerSocket serverSocket;

    public HBAcceptThread(BluetoothAdapter adapter, String name, UUID uuid, AcceptCallback callback) {
        this.adapter = adapter;
        this.name = name;
        this.uuid = uuid;
        this.callback = callback;
    }

    @Override
    public void run() {
        super.run();
        try {
            serverSocket = adapter.listenUsingRfcommWithServiceRecord(name, uuid);
            BluetoothDevice device;
            HBConnection connection;
            while (true) {
                BluetoothSocket socket = serverSocket.accept();
                device = socket.getRemoteDevice();
                connection = new HBConnection(device.getName(), device.getAddress(), socket);
                callback.onClientConnected(connection);
            }
        } catch (IOException e) {
            e.printStackTrace();
            callback.onFailed(HBConstant.ERROR_CODE_ACCEPT_FAILED);
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void cancel() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
