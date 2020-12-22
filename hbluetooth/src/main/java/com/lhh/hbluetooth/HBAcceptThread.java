package com.lhh.hbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * 等待蓝牙设备连接线程
 * 唯一观察者为创建该实例的{@link HBluetooth}对象
 * 在被设备成功连接后，该线程并不会停止，而是会继续等待新的设备连接，所以需要手动停止该线程
 */
public class HBAcceptThread extends Thread {

    private static final String TAG = "HBAcceptThread";

    private BluetoothAdapter adapter;
    private String name;
    private UUID uuid;
    private HBAcceptDeviceListener listener;
    private BluetoothServerSocket serverSocket;

    public HBAcceptThread(BluetoothAdapter adapter, String name, UUID uuid, HBAcceptDeviceListener listener) {
        this.adapter = adapter;
        this.name = name;
        this.uuid = uuid;
        this.listener = listener;
        HBLog.d(TAG, "[HBAcceptThread-"+name+"] Created");
    }

    @Override
    public void run() {
        super.run();
        try {
            serverSocket = adapter.listenUsingRfcommWithServiceRecord(name, uuid);
            while (true) {
                BluetoothSocket socket = serverSocket.accept();
                BluetoothDevice device = socket.getRemoteDevice();
                HBConnection connection = new HBConnection(device.getName(), device.getAddress(), socket);
                listener.onAccepted(connection);
            }
        } catch (IOException e) {
            listener.onError(e);
        } finally {
            release();
        }
    }

    public void cancel() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void release() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = null;
            HBLog.d(TAG, "[HBAcceptThread-"+name+"] ServerSocket is closed");
        }

        listener = null;
    }
}
