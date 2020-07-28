package com.lhh.hbluetooth;

import java.util.Arrays;

public class Util {

    /**
     * 拼接字节数组
     * @param first 第一个字节数组
     * @param second 第二个字节数组
     * @return 拼接好的字节数组
     */
    public static byte[] bytesConcat (byte[] first, byte[] second) {
        byte[] concatBytes = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, concatBytes, first.length, second.length);
        return concatBytes;
    }
}
