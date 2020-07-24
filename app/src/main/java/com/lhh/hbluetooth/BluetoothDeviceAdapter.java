package com.lhh.hbluetooth;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {

        View deviceView;
        TextView deviceNameText;
        TextView deviceAddressText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceView = itemView;
            deviceNameText = itemView.findViewById(R.id.device_name);
            deviceAddressText = itemView.findViewById(R.id.device_address);
        }
    }

    private List<BlueDevice> deviceList;

    private BluetoothDeviceItemOnClickListener listener;

    public BluetoothDeviceAdapter(List<BlueDevice> deviceList, BluetoothDeviceItemOnClickListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_item,
                parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.deviceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BlueDevice device = deviceList.get(holder.getAdapterPosition());
                listener.onClick(device);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlueDevice device = deviceList.get(position);
        holder.deviceNameText.setText(device.getName());
        holder.deviceAddressText.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }
}
