package com.lhh.hbluetooth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private List<MyBluetoothDevice> deviceList;
    private OnDeviceItemClickListener onItemClickListener;

    public BluetoothDeviceAdapter(List<MyBluetoothDevice> deviceList, OnDeviceItemClickListener listener) {
        this.deviceList = deviceList;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bluetooth_device_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.view.setOnClickListener(v->{
            MyBluetoothDevice device = deviceList.get(viewHolder.getAdapterPosition());
            onItemClickListener.onClick(device);
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyBluetoothDevice device = deviceList.get(position);
        holder.deviceName.setText(device.getDevice().getName());
        String status = "";
        switch (device.getStatus()) {
            case Disconnect:
                status = "未连接";
                break;
            case Connecting:
                status = "连接中";
                break;
            case Connected:
                status = "已连接";
                break;
        }
        holder.deviceStatus.setText(status);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView deviceName;
        TextView deviceStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            deviceName = itemView.findViewById(R.id.device_name);
            deviceStatus = itemView.findViewById(R.id.device_status);
        }
    }

    public interface OnDeviceItemClickListener {
        void onClick(MyBluetoothDevice device);
    }
}
