package com.forward.core.utils;

import javax.smartcardio.*;

public class MifareCardReader {

    public static void main(String[] args) {
        try {
            // 获取智能卡终端
            TerminalFactory factory = TerminalFactory.getDefault();
            CardTerminals terminals = factory.terminals();

            // 获取第一个智能卡终端
            CardTerminal terminal = terminals.list().get(0);

            // 连接到智能卡
            Card card = terminal.connect("*");

            // 获取与智能卡通信的卡片通道
            CardChannel channel = card.getBasicChannel();

            // 发送APDU命令读取扇区和块数据
            byte[] readCommand = {(byte) 0xFF, (byte) 0xB0, 0x00, 0x00, 0x10}; // 读取第0扇区第0块的数据
            ResponseAPDU response = channel.transmit(new CommandAPDU(readCommand));

            // 检查响应状态码
            if (response.getSW() == 0x9000) {
                // 读取成功，获取数据
                byte[] data = response.getData();
                System.out.println("读取数据：" + bytesToHex(data));
            } else {
                System.out.println("读取失败");
            }

            // 断开与智能卡的连接
            card.disconnect(false);
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
}
