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

    private static final String TAG = "HBAcceptThread";

    private BluetoothAdapter adapter;

    private String name;

    private UUID uuid;

    private AcceptCallback callback;

    private BluetoothServerSocket serverSocket;

    private boolean userCanceled = false;

    public HBAcceptThread(BluetoothAdapter adapter, String name, UUID uuid, AcceptCallback callback) {
        HBLog.i(TAG, "[AcceptThread-" + name + "] Accept thread create: " + uuid);
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
            if (!userCanceled) {
                HBLog.e(TAG, "[AcceptThread-" + name + "] Accept device failed: "
                        + e.getMessage());
                callback.onFailed(HBConstant.ERROR_CODE_ACCEPT_FAILED);
            } else {
                serverSocket = null;
            }
        } finally {
            release();
        }
    }

    public void cancel() {
        HBLog.i(TAG, "[AcceptThread-" + name + "] Cancel");
        userCanceled = true;
        // 触发serverSocket的IO异常，已停止线程
        try {
            HBLog.i(TAG, "[AcceptThread-" + name + "] Close server socket");
            serverSocket.close();
        } catch (IOException e) {
            HBLog.e(TAG, "[AcceptThread-" + name + "] Close server socket failed: "
                    + e.getMessage());
        }
    }

    private void release() {
        if (serverSocket != null) {
            HBLog.i(TAG, "[AcceptThread-" + name + "] Close server socket");
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {
                HBLog.e(TAG, "[AcceptThread-" + name + "] Close server socket failed: "
                        + e.getMessage());
            }
        }
    }
}
