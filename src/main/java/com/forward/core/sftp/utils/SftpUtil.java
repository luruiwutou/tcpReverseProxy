package com.forward.core.sftp.utils;

import com.jcraft.jsch.*;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

public class SftpUtil {

    private static Logger logger = LoggerFactory.getLogger(SftpUtil.class);
    private static ThreadLocal<Session> sessionCache = new ThreadLocal<>();
    private static ThreadLocal<ChannelSftp> channelSftpCache = new ThreadLocal<>();


    /**
     * 获取ChannelSftp 密码连接
     */
    public static void getChannelSftp(String host, String portStr,
                                      String username, String password) throws Exception {
        Integer port = null;
        if (StringUtil.isNotEmpty(portStr)) {
            port = Integer.parseInt(portStr);
        }

        try {
            JSch jsch = new JSch();
            jsch.getSession(username, host, port);

            Session sshSession = null;
            sshSession = jsch.getSession(username, host, port);
            sshSession.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            sshSession.connect();

            ChannelSftp sftp = null;
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            sessionCache.set(sshSession);
            channelSftpCache.set(sftp);
        } catch (Exception e) {
            logger.info("getChannelSftp exception:{}", e.getMessage());
            throw e;
        }
    }


    /**
     * 密钥文件连接
     */

    public static void getPriKeyChannelSftp(String host, String portStr,
                                            String username, String priKeyFile, String passphrase) throws JSchException {
        Integer port = null;
        if (StringUtil.isNotEmpty(portStr)) {
            port = Integer.parseInt(portStr);
        }
        try {
            JSch jsch = new JSch();
            if (priKeyFile != null && !"".equals(priKeyFile)) {
                if (passphrase != null && !"".equals(passphrase)) {
                    jsch.addIdentity(priKeyFile, passphrase);
                } else {
                    jsch.addIdentity(priKeyFile);
                }
            }

            Session sshSession = null;
            sshSession = jsch.getSession(username, host, port);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(config);
            sshSession.connect();

            ChannelSftp sftp = null;
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            sessionCache.set(sshSession);
            channelSftpCache.set(sftp);

        } catch (JSchException e) {
            logger.info("getPriKeyChannelSftp exception:{}", e.getCause());
            throw e;
        }
    }


    /**
     * 下载
     */
    public static InputStream download(String directory, String fileName) throws Exception {
        try {
            ChannelSftp sftp = channelSftpCache.get();
            logger.info("enter {}", directory);
            sftp.cd(directory);
            logger.info("entered.., ready to get {}", fileName);
            InputStream is = sftp.get(fileName);
            logger.info("got inputStream..");
            return is;
        } catch (Exception e) {
            logger.info("download exception:{}", e.getCause());
            throw e;
        }
    }

    /**
     * 下载
     */
    public static void downloadCompress(String remoteDirectory, String localFileName) throws Exception {
        try {
            ChannelSftp sftp = channelSftpCache.get();
            logger.info("enter {}", remoteDirectory);
            Vector<ChannelSftp.LsEntry> fileList = sftp.ls(remoteDirectory);
            if (fileList != null) {
                FileOutputStream fos = new FileOutputStream(localFileName);
                GZIPOutputStream gos = new GZIPOutputStream(fos);
                TarArchiveOutputStream tos = new TarArchiveOutputStream(gos);

                for (ChannelSftp.LsEntry entry : fileList) {
                    if (!entry.getAttrs().isDir()) {
                        String remoteFile = remoteDirectory + "/" + entry.getFilename();
                        InputStream is = sftp.get(remoteFile);
                        TarArchiveEntry tarEntry = new TarArchiveEntry(entry.getFilename());
                        tarEntry.setSize(entry.getAttrs().getSize());
                        tos.putArchiveEntry(tarEntry);
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            tos.write(buffer, 0, len);
                        }
                        tos.closeArchiveEntry();
                        is.close();
                    }
                }

                tos.finish();
                tos.close();
                gos.close();
                fos.close();
            }
        } catch (Exception e) {
            logger.info("download exception:{}", e.getCause());
            throw e;
        }
        logger.info("download compress file :{}",localFileName);
    }

    /**
     * 释放连接
     */
    public static void release() {
        ChannelSftp sftp = channelSftpCache.get();
        Session sshSession = sessionCache.get();
        try {
            if (sftp != null) {
                if (sftp.isConnected()) {
                    sftp.disconnect();
                }
            }

            if (sshSession != null) {
                if (sshSession.isConnected()) {
                    sshSession.disconnect();
                }
            }
            channelSftpCache.remove();
            sessionCache.remove();
            logger.info("释放sftp");
            logger.info("threadLocal channelSftpCache.get() is null: {}", channelSftpCache.get() == null);
            logger.info("threadLocal sessionCache.get() is null:{}", sessionCache.get() == null);
        } catch (Exception e) {
            logger.info(" sftp 释放连接异常");
            logger.info("sftp release:{}", e.getMessage());
        }
    }
}
