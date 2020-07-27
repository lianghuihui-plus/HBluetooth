package com.lhh.hbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.UUID;

public class HBluetoothAcceptTask extends AsyncTask<Void, Void, Void> {

    private BluetoothAdapter adapter;

    private String name;

    private java.util.UUID uuid;

    private HServerAcceptListener listener;

    private BluetoothServerSocket serverSocket;

    public HBluetoothAcceptTask(BluetoothAdapter adapter, String name, UUID uuid, HServerAcceptListener listener) {
        this.adapter = adapter;
        this.name = name;
        this.uuid = uuid;
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            serverSocket = adapter.listenUsingRfcommWithServiceRecord(name, uuid);
            BluetoothDevice device;
            HBluetoothConnection connection;
            while (true) {
                BluetoothSocket socket = serverSocket.accept();
                device = socket.getRemoteDevice();
                connection = new HBluetoothConnection(device.getName(), device.getAddress(), socket);
                listener.onClientConnect(connection);
            }
        } catch (IOException e) {
            e.printStackTrace();
            listener.onFailed(e);
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
