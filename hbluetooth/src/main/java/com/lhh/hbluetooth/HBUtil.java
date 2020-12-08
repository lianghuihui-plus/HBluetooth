package com.lhh.hbluetooth;

/**
 * 蓝牙辅助工具类
 */
public class HBUtil {
    public enum HLogLevel {
        V(1),
        D(2),
        I(3),
        W(4),
        E(5),
        N(6);

        public int value;

        HLogLevel(int value) {
            this.value = value;
        }
    }

    public static void setLogLevel(HLogLevel level) {
        HBLog.setLogLevel(level.value);
    }
}
