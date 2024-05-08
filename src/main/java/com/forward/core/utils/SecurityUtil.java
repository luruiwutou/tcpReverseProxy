package com.forward.core.utils;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtil {
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    /**
     * Jasypt
     * Algorithm:PBEWithMD5AndDES
     * @param plaintext 明文密码
     * @param publicKey 加密密钥
     * @return
     */
    public static String encrypt(String plaintext,String publicKey) {
        //加密工具
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        //加密配置
        EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
        // 算法类型
        config.setAlgorithm("PBEWithMD5AndDES");
        //生成秘钥的公钥
        config.setPassword(publicKey);
        //应用配置
        encryptor.setConfig(config);
        //加密
        String ciphertext = encryptor.encrypt(plaintext);
        return ciphertext;
    }
    /**
     * 解密
     *
     * @param publicKey             密钥
     * @param encryptPassword 加密密码
     * @return String
     */
    public static String decrypt(String publicKey, String encryptPassword) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(publicKey);
        //加密配置
        EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
        // 算法类型
        config.setAlgorithm("PBEWithMD5AndDES");
        //生成秘钥的公钥
        config.setPassword(publicKey);
        //应用配置
        encryptor.setConfig(config);
        return encryptor.decrypt(encryptPassword);
    }
    public static class MD5 {
        public static final String KEY_MD5 = "MD5";

        public static String getResult(String inputStr) {
            logger.debug("=======加密前的数据:{}", inputStr);

            String result = null;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] hash = md.digest(inputStr.getBytes());

                StringBuilder sb = new StringBuilder();
                for (byte b : hash) {
                    sb.append(String.format("%02x", b));
                }
                result = sb.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            logger.debug("MD5加密后:{}", result);
            return result;
        }

    }

    public static class SHA {
        public static final String KEY_SHA = "SHA";

        public static String getResult(String inputStr) {
            BigInteger sha = null;
            logger.debug("=======加密前的数据:" + inputStr);
            byte[] inputData = inputStr.getBytes();
            try {
                MessageDigest messageDigest = MessageDigest.getInstance(KEY_SHA);
                messageDigest.update(inputData);
                sha = new BigInteger(messageDigest.digest());
                logger.debug("SHA加密后:" + sha.toString(32));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return sha.toString(32);
        }

//        public static void main(String args[]) {
//            try {
//                String inputStr = "简单加密";
//                getResult(inputStr);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }

    }
}
