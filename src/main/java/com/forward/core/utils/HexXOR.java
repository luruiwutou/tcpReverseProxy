package com.forward.core.utils;

import org.apache.tomcat.util.buf.HexUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class HexXOR {
    public static void main(String[] args) {
        String hexString1 = "22B0395043407AC7"; // 第一个16进制字符串
        String hexString2 = "0000730100252160"; // 第二个16进制字符串
//        String resultHexString = hexTwoHexStr(hexString1, hexString2);

        // 输出结果
//        System.out.println(resultHexString);
//
        String[] pin_block = {"4E91C151B6E8749F", "0000730100252160"}; // 16进制字符串数组

        String[] imkac = {"AE0B7685764FAEFE97E04ADF490EF4A8", "F46402DA51AE758C70807C6426C7A76E", "D673D3689B6B3D3BAD9EC2D90179371A"}; // 16进制字符串数组
//        String[] imkac = {"15DCA7C81AE6EFA1B9AE1FB334161A70", "6EA11F29DC1FFB3798EFAE3B9789DADC", "F80D9BF262F83B1C915715314652AE5E"}; // 16进制字符串数组
//        String[] imkenc = {"16C823B03EBF9875F2029BF2B3761A8F", "DFC70B97B664BF2043E9616231517AF4", "DA58AD26581537D5F1E63BEF7CF41CB6"}; // 16进制字符串数组
//        String[] imkmac = {"200749B3B5D049DA79AD192F2594B6E3", "7554088A2AE39D02B0E658F26ECB2575", "A8795B8C02799E575B5226AB2F7C7A10"}; // 16进制字符串数组
//
//        // 输出结果
        System.out.println("pin-block:" + getString(pin_block));
        System.out.println("imkac:" + getString(imkac));
//        System.out.println("imkenc:" + getString(imkenc));
//        System.out.println("imkmac:" + getString(imkmac));
//        decodeIMB();
    }
    //IMKac(ARAC):83702313A4012F8AB016A4B9E5CD6EF2
    //IMKsmc(ENc):13578501D0CE1080400DC17FFED37CCD
    //IMKsmi(MAC):FD2A1AB59D4A4A8F921967766423E986
    public static void  decodeIMB() {
//        String hexString1 = "F1F9F6F2F1F7F4F2F7F3F0F1F0F0F2F5F2F1F6F0F8F3F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F4F1F7F1F2F1F9F0F2F1F2F4F3F5F8F1F2F1F9F0F2F0F4F1F7F5F7F0F3F0F4F1F7F6F0F1F1F3F4F4F0F2F1F0F0F0F0F2F0F6F0F8F5F0F5F0F0F4F4F6F0F8F5F0F5F0F0F4F4F6F3F7F6F2F1F7F4F2F7F3F0F1F0F0F2F5F2F1F6F0F87EF5F7F0F3F2F2F0F0F0F0F0F0F1F6F6F0F0F0F0F0F0F0F0F0F1F3F4F0F3F0F0F0F0F0F1F0F0F0F1F0F0F1F5F8F4F0F5F4F1F1F0F0F0F2E385A2A38995874094859983888195A340F240404040404040889695879296958740404040C8D2C7F0F0F0F0F0F3F4F495A06E278C308E38F2F6F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F2F7F0F0F0F0F0F2F0F0F0F1F0F0F0F0F0F0F0F0F0F0F0F0F1F1F0F0F1F0F0F0F0F8F4F7F9F9F0F3F4F4F0F0F0F3F0F5F1F1404040404040404040404040404040404040404040404040404040F0F0F0F0F0F0F0F2F8F2F3F0F4F1F7F5F3F3F6F1F4C3E4D7E8F0F1C3E4D7E8F0F0F0F1D1C3F6C5F6F8F1C5C6F5";
//        //3034323343555059434a30323130f23e66c1aaf19a1200000000140000c731393632313734323733303130303235323136303830303030303030303030303030303430303030343138313734353036313234333733313734353036303431383537303330343138353431313334343032313030303030303630383530353030343436303835303530303434363337363231373432373330313030323532313630383d35373033323230303030303031363630303030303030303031333431383939303030313030303130303135383430353431313030303254657374696e67206d65726368616e74203220202020202020686f6e676b6f6e6720202020484b4731333331323631353233313039303030303033343495a06e278c308e383236303030303030303030303030303030303030323730303030303230303033303030303030303030303030313130303130303030383437393930333434303030333035313120202020202020202020202020202020202020202020202020202030303030303030323832333034313835343131333643555059303143555059303030334a434530363346303839
//        byte[] bytes = HexUtil.hexToString(hexString1).getBytes();
//        byte[] byteArray = DatatypeConverter.parseHexBinary(hexString1);
//        String ibm1047 = new String(bytes,Charset.forName("IBM1047"));
//        System.out.println(ibm1047); // E0D22BA4
////        byte[] ibm1047s = ibm1047.getBytes(Charset.forName("IBM1047"));
//        byte[] ibm1047s = ibm1047.getBytes(Charset.forName("utf-8"));
//
//        String hexString2 = DatatypeConverter.printHexBinary(ibm1047s);
//
//        System.out.println(hexString2);

        String hexString1 = "f0f7f1f1f0f0f3f9d1c3f0f0f0f1f0f0f0f2f2f3f0f4f2f6f2f3f0f4f2f6f6f0f7f4f7f3e3e3c6c3f9f9f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f2f3f0f4f2f6f0f0f6f0f7f4f7f340404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f1f1f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f4f3f6f140f8f4c2c4f8f6f0f7f0f1f5f0f5f1f0f7f3f0f0f0f0f0f9f9f0f740404040404040f0f2f3f4f4f0f3f9f7f3f0f0f0f0f0f9f8f8f14040404040f0f2f3f4f45a0a6217427300000990708f5f340101950500000408009f1e0830303030303030329f100807630103a0a002019f36020088df202a540039073000009907ff0200034403973000009923ffffff0200015603973000009881ffffff02000344df21020039df220200010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        byte[] byteArray = DatatypeConverter.parseHexBinary(hexString1);
//        byte[] byteArray1 = HexUtils.hexToByte(hexString1);
//        String hexString2 = DatatypeConverter.printHexBinary(byteArray);
        System.out.println(new String(byteArray, Charset.forName("IBM1047"))); // E0D22BA4
//        System.out.println(new String(byteArray1, Charset.forName("IBM1047"))); // E0D22BA4

    }

    private static String getString(String[] hexStrs) {
        // 将所有16进制字符串转换为字节数组
        byte[][] byteArrays = new byte[hexStrs.length][];
        for (int i = 0; i < hexStrs.length; i++) {
            byteArrays[i] = hexStringToByteArray(hexStrs[i]);
        }

        // 执行异或递归方法
        byte[] resultArray = xorByteArrays(byteArrays);

        // 将结果字节数组转换为16进制字符串
        return byteArrayToHexString(resultArray);
    }

    // 将16进制字符串转换为字节数组
    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] byteArray = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return byteArray;
    }

    // 将字节数组转换为16进制字符串
    private static String byteArrayToHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    // 递归方法：异或所有字节数组
    private static byte[] xorByteArrays(byte[][] byteArrays) {
        if (byteArrays.length == 1) {
            return byteArrays[0]; // 如果只有一个字节数组，则直接返回该数组
        } else if (byteArrays.length == 2) {
            byte[] resultArray = new byte[byteArrays[0].length];
            for (int i = 0; i < byteArrays[0].length; i++) {
                resultArray[i] = (byte) (byteArrays[0][i] ^ byteArrays[1][i]);
            }
            return resultArray; // 如果有两个字节数组，则将它们异或并返回结果数组
        } else {
            // 如果有三个或更多的字节数组，则将前两个异或，并将结果与后面的所有字节数组递归异或
            byte[] resultArray = xorByteArrays(new byte[][]{byteArrays[0], byteArrays[1]});
            byte[][] remainingArrays = Arrays.copyOfRange(byteArrays, 2, byteArrays.length);
            return xorByteArrays(concatArrays(new byte[][]{resultArray}, remainingArrays));
        }
    }

    // 连接两个二维字节数组
    private static byte[][] concatArrays(byte[][] array1, byte[][] array2) {
        byte[][] resultArray = new byte[array1.length + array2.length][];
        System.arraycopy(array1, 0, resultArray, 0, array1.length);
        System.arraycopy(array2, 0, resultArray, array1.length, array2.length);
        return resultArray;
    }

}
