package com.forward.core.utils;

import javax.smartcardio.*;

public class M1CardReader {

    public static void main(String[] args) {
        try {
            // 获取智能卡终端
            TerminalFactory terminalFactory = TerminalFactory.getDefault();
            CardTerminals cardTerminals = terminalFactory.terminals();

            // 获取智能卡
            CardTerminal cardTerminal = cardTerminals.list().get(2); // 假设只有一个智能卡终端
            Card card = cardTerminal.connect("*"); // 连接智能卡

            // 获取卡片APDU通道
            CardChannel cardChannel = card.getBasicChannel();
            String key = "FF FF FF FF FF FF";
            // 构造读取块数据的APDU命令
            int blockIndex = 0;
            //0xFF, 0x82 ,0x20, 0x00,0x06 ,0xFF, 0xFF ,0xFF,0xFF ,0xFF, 0xFF
            byte[] loadKey = {(byte) 0xFF, (byte) 0x82,0x20, 0x00,0x06 , (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            byte[] readCommandAPDU = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) blockIndex, (byte) 0x10};

            // 发送APDU命令并获取响应
            ResponseAPDU responseAPDU = cardChannel.transmit(new CommandAPDU(loadKey));

            // 检查响应状态码
            if (responseAPDU.getSW() == 0x9000) {
                // 读取成功
                byte[] responseData = responseAPDU.getData();
                String hexData = bytesToHex(responseData); // 将字节数组转换为十六进制字符串
                System.out.println("读取到的块数据：" + hexData);

            } else {
                // 读取失败
                System.out.println("读取块数据失败");
            }

            // 关闭智能卡连接
            card.disconnect(true);

        } catch (CardException e) {
            e.printStackTrace();
        }
    }

    // 将字节数组转换为十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    public static void load(String pin,int blockIndex) {
        try {
            // 获取智能卡终端
            TerminalFactory terminalFactory = TerminalFactory.getDefault();
            CardTerminals cardTerminals = terminalFactory.terminals();

            // 获取智能卡
            CardTerminal cardTerminal = cardTerminals.list().get(0); // 假设只有一个智能卡终端
            Card card = cardTerminal.connect("T=0"); // 使用 T=0 协议连接智能卡

            // 获取卡片APDU通道
            CardChannel cardChannel = card.getBasicChannel();

            // 构造认证密钥的APDU命令
            String pinHex = pin.replaceAll("\\s", ""); // 去除空格
            byte[] pinBytes = hexStringToByteArray(pinHex); // 将密钥转换为字节数组
            byte[] authenticateCommandAPDU = {(byte) 0xFF, (byte) 0x86, (byte) 0x00, (byte) 0x00, (byte) 0x05};
            byte[] authenticateData = new byte[5 + pinBytes.length];
            System.arraycopy(authenticateCommandAPDU, 0, authenticateData, 0, authenticateCommandAPDU.length);
            System.arraycopy(pinBytes, 0, authenticateData, authenticateCommandAPDU.length, pinBytes.length);

            // 发送认证密钥的APDU命令并获取响应
            ResponseAPDU authenticateResponseAPDU = cardChannel.transmit(new CommandAPDU(authenticateData));

            // 检查认证密钥的响应状态码
            if (authenticateResponseAPDU.getSW() == 0x9000) {
                // 认证密钥成功，构造读取块数据的APDU命令
                byte[] readCommandAPDU = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) blockIndex, (byte) 0x10};

                // 发送读取块数据的APDU命令并获取响应
                ResponseAPDU readResponseAPDU = cardChannel.transmit(new CommandAPDU(readCommandAPDU));

                // 检查读取块数据的响应状态码
                if (readResponseAPDU.getSW() == 0x9000) {
                    // 读取成功
                    byte[] responseData = readResponseAPDU.getData();
                    // 将读取到的数据转换为16进制字符串
                    String hexData = byteArrayToHexString(responseData);
                    // 处理读取到的数据
                    // ...
                } else {
                    // 读取失败
                    System.out.println("读取块数据失败");
                }
            } else {
                // 认证密钥失败
                System.out.println("认证密钥失败");
            }

            // 关闭智能卡连接
            card.disconnect(true);

        } catch (CardException e) {
            e.printStackTrace();
        }
    }

    // 辅助方法：将字节数组转换为16进制字符串
    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    // 辅助方法：将16进制字符串转换为字节数组
    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}
