package com.forward.core.tcpReverseProxy.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HsmUtils {
    /**
     * byte数组转hex
     *
     * @param bytes
     * @return
     */
    public static String byteToHex(byte[] bytes) {
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim();
    }

    /**
     * 解析消息成字符串
     *
     * @param bArray
     * @return 9A开始的是正确答复 9E开始的是错误的答复
     */

    public static String resolveResult(byte[] bArray) {
        log.info("HSM return message: {}", byteToHex(bArray));
        byte[] byteLength = subByte(bArray, 0 ,2);
        String code = byteToHex(byteLength);
        int length = Integer.parseInt(code,16);
        return byteToHex(subByte(bArray, 2 ,length));
    }


    public static byte[] subByte(byte[] bytes, int beginIndex, int length) {
        if (bytes == null || bytes.length < beginIndex + length) {
            throw new RuntimeException("byte array is too short!");
        }
        byte[] result = new byte[length];
        System.arraycopy(bytes, beginIndex, result, 0, length);
        return result;
    }


    public static byte[] addHexLength(byte[] message, int lengthOfLength) {
        return byteMergerAll(hexToByte(getHexLength(message, lengthOfLength * 2)), message);
    }

    public static byte[] byteMergerAll(byte[]... values) {
        int lengthByte = 0;
        for (int i = 0; i < values.length; i++) {
            lengthByte += values[i].length;
        }
        byte[] allByte = new byte[lengthByte];
        int countLength = 0;
        for (int i = 0; i < values.length; i++) {
            byte[] b = values[i];
            System.arraycopy(b, 0, allByte, countLength, b.length);
            countLength += b.length;
        }
        return allByte;
    }
    public static byte[] hexToByte(String hex) {
        int m = 0, n = 0;
        // 每两个字符描述一个字节
        int byteLen = hex.length() / 2;
        byte[] ret = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
            ret[i] = (byte) intVal;
        }
        return ret;
    }

    public static String getHexLength(byte[] message, int lengthOfLength) {
        return leftPaddingZero(Integer.toHexString(message.length), lengthOfLength).toUpperCase();
    }
    public static String getHexLength(String message, int lengthOfLength) {
        return leftPaddingZero(Integer.toHexString(message.length()), lengthOfLength).toUpperCase();
    }

    public static String leftPaddingZero(String msg, int length) {
        int reallength = msg.length();
        for (int i = 0; i < length - reallength; i++) {
            msg = "0" + msg;
        }
        return msg;
    }
    public static byte[] leftPaddingZero(byte[] bytes, int length) {
        int reallength = bytes.length;
        for (int i = 0; i < length - reallength; i++) {
            bytes = HsmUtils.byteMergerAll(new byte[]{0x00}, bytes);
        }
        return bytes;
    }
    public static String hexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        // 49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            // grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            // convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            // convert the decimal to character
            sb.append((char) decimal);
            temp.append(decimal);
        }
        return sb.toString();
    }

}
