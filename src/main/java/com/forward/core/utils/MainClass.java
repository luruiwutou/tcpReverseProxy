package com.forward.core.utils;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import java.lang.reflect.Field;

public class MainClass {
    public static void main(String[] args) throws  Exception {
//        String libraryPath = "resources/ddl"; // 替换为正确的库文件路径

//        System.setProperty("java.library.path", libraryPath);

        // 强制重新加载 java.library.path
//        try {
//            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
//        ClassLoader.class.;
//            fieldSysPath.setAccessible(true);
//            fieldSysPath.set(null, null);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return;
//        }

        // 读取块数据
        int blockIndex = 0;
        String pin = "FFFFFFFFFFFF";
        Pointer buff = new Memory(16); // 创建一个16字节的缓冲区
        int result = M1ReaderDLL.INSTANCE.M1_ReadBlock(blockIndex, pin, buff);
        if (result == 0) {
            byte[] data = buff.getByteArray(0, 16); // 从缓冲区中获取数据
            System.out.println("读取到的块数据：" + bytesToHexString(data));
        } else {
            System.out.println("读取块数据失败");
        }

        // 写入块数据
        String dataToWrite = "11223344556677881122334455667788";
        result = M1ReaderDLL.INSTANCE.M1_WriteBlock(blockIndex, pin, dataToWrite);
        if (result == 0) {
            System.out.println("写入块数据成功");
        } else {
            System.out.println("写入块数据失败");
        }

        // ...
    }

    // 将字节数组转换为十六进制字符串
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
