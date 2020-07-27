package com.lhh.hbluetooth;

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
        TextView deviceStatusText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceView = itemView;
            deviceNameText = itemView.findViewById(R.id.device_name);
            deviceStatusText = itemView.findViewById(R.id.device_status);
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
        holder.deviceView.setOnClickListener(v -> {
            BlueDevice device = deviceList.get(holder.getAdapterPosition());
            listener.onClick(device);
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlueDevice device = deviceList.get(position);
        holder.deviceNameText.setText(device.getName());
        String state = "";
        switch (device.getStatus()) {
            case DISCONNECTED:
                state = "未连接";
                break;
            case CONNECTING:
                state = "正在连接";
                break;
            case CONNECTED:
                state = "已连接";
                break;
        }
        holder.deviceStatusText.setText(state);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }
}
