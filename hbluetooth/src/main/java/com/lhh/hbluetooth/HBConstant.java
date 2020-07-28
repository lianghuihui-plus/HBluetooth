package com.lhh.hbluetooth;

/**
 * 常量类
 */
public class HBConstant {

    //请求打开蓝牙适配器
    public static final int REQ_CODE_ACTION_REQUEST_ENABLE = 0x0101;
    //请求让蓝牙可被发现
    public static final int REQ_CODE_ACTION_REQUEST_DISCOVERABLE = 0x0102;

    //513 无效的设备地址
    public static final int ERROR_CODE_INVALID_DEVICE_ADDRESS = 0x0201;
    //514 蓝牙适配器未打开
    public static final int ERROR_CODE_BLUETOOTH_ADAPTER_IS_DISABLED = 0x0202;
    //515 设备已连接
    public static final int ERROR_CODE_DEVICE_ALREADY_CONNECTED = 0x0203;
    //516 连接设备失败，未知原因
    public static final int ERROR_CODE_CONNECT_DEVICE_FAILED = 0x0204;
    //517 接受客户端连接失败，未知原因
    public static final int ERROR_CODE_ACCEPT_FAILED = 0x0205;
}
