package com.forward.core.netty;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ZhaoLing
 * <p>
 * ThalesHsm客戶端配置信息
 * @date
 */
@ConfigurationProperties(prefix = "chiyu.hsm")
public class HsmClientProperties {

    // ！需要配置  Hsm客户端地址 【写死的是UAT的地址】
    private String host = "25.6.72.49";
    //！需要配置 hsm端口 【写死的是UAT的端口】
    private int port = 1500;
    // 默认超时时间
    private long timout = 2000L;
    // 用来区分消息的帧 每个消息最大长度，超出长度会抛出异常
    private int maxFrameLength = 1024;
    // 用来表示长度的字节的长度
    private int frameLengthFieldLength = 2;
    // 长度字节的偏移量
    private int lengthFieldOffset = 0;
    // 要添加到长度字段值的补偿值
    private int lengthAdjustment = 0;
    // 从解码帧中第一个字节开始去除的个数
    private int initialBytesToStrip = 0;
    // 错误了是否抛出异常
    private boolean replyOnError = true;
    // 空闲时间（SECONDS）达到idleTimeout处理 用于发送心跳消息
    /**
     * 读超时处理时间
     */
    private int readIdleTimeout = 0;
    /**
     * 写超时处理时间
     */
    private int writeIdleTimeout = 300;
    // hsm客户端工作线程数
    private int workerThreadsCount = 100;
    // 间隔多久尝试重连
    //！需要配置 MDB配置为3000MS  3秒钟
    private int reconnectInterval = 60000;
    //ATMC报文是否校验MAC
    //！SIT可以配置为false 跳过验证MAC
    private boolean validateMac = true;
    //ATMC报文是否生成MAC
    //！SIT可以配置为false 跳过生成MAC
    private boolean calculationMac = true;

    private int poolSize = 5;

    private int zakIndex = 0x02;

    private int zpkIndex = 0x03;

    public int getZpkIndex() {
        return zpkIndex;
    }

    public void setZpkIndex(int zpkIndex) {
        this.zpkIndex = zpkIndex;
    }

    public int getZakIndex() {
        return zakIndex;
    }

    public void setZakIndex(int zakIndex) {
        this.zakIndex = zakIndex;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public boolean getCalculationMac() {
        return calculationMac;
    }

    public void setCalculationMac(boolean calculationMac) {
        this.calculationMac = calculationMac;
    }

    public boolean getValidateMac() {
        return validateMac;
    }

    public void setValidateMac(boolean validateMac) {
        this.validateMac = validateMac;
    }

    public int getReadIdleTimeout() {
        return readIdleTimeout;
    }

    public void setReadIdleTimeout(int readIdleTimeout) {
        this.readIdleTimeout = readIdleTimeout;
    }

    public int getWriteIdleTimeout() {
        return writeIdleTimeout;
    }

    public void setWriteIdleTimeout(int writeIdleTimeout) {
        this.writeIdleTimeout = writeIdleTimeout;
    }

    public int getLengthFieldOffset() {
        return lengthFieldOffset;
    }

    public void setLengthFieldOffset(int lengthFieldOffset) {
        this.lengthFieldOffset = lengthFieldOffset;
    }

    public int getLengthAdjustment() {
        return lengthAdjustment;
    }

    public void setLengthAdjustment(int lengthAdjustment) {
        this.lengthAdjustment = lengthAdjustment;
    }

    public int getInitialBytesToStrip() {
        return initialBytesToStrip;
    }

    public void setInitialBytesToStrip(int initialBytesToStrip) {
        this.initialBytesToStrip = initialBytesToStrip;
    }

    public boolean isReplyOnError() {
        return replyOnError;
    }


    public int getReconnectInterval() {
        return reconnectInterval;
    }

    public void setReconnectInterval(int reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    public int getWorkerThreadsCount() {
        return workerThreadsCount;
    }

    public void setWorkerThreadsCount(int workerThreadsCount) {
        this.workerThreadsCount = workerThreadsCount;
    }

    public boolean getReplyOnError() {
        return replyOnError;
    }

    public void setReplyOnError(boolean replyOnError) {
        this.replyOnError = replyOnError;
    }

    public int getFrameLengthFieldLength() {
        return frameLengthFieldLength;
    }

    public void setFrameLengthFieldLength(int frameLengthFieldLength) {
        this.frameLengthFieldLength = frameLengthFieldLength;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getTimout() {
        return timout;
    }

    public void setTimout(long timout) {
        this.timout = timout;
    }

    public int getMaxFrameLength() {
        return maxFrameLength;
    }

    public void setMaxFrameLength(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
    }
}
