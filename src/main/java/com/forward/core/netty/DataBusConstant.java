package com.forward.core.netty;

public class DataBusConstant {
    public static final String DELIMITER = "%#_#%";

    public static final String HEART_BEAT = "ping-pong-ping-pong";

    /**
     * 最大连接数
     */
    public static final int MAX_CONNECTIONS = Integer.MAX_VALUE;

    /**
     * 核心链接数，该数目内的通道 在没有业务请求时发送心跳防止失活，超过部分的通道close掉
     */
    public static final int CORE_CONNECTIONS = 1;

    /**
     * 同一个线程使用同一个全局唯一的随机数，保证从同一个池中获取和释放资源，同时使用改随机数作为Key获取返回值
     */
    public static final String RANDOM_KEY = "randomID";

    /**
     * 服务端丢失心跳次数，达到该次数，则关闭通道，默认3次
     */
    public static final int LOOS_HEART_BEAT_COUNT = 3;
    /**
     * CUPY,JETCO,EPSCO,ATMC对应的MessageType
     */
    public static final String MESSAGE_TYPE_CUPY = "C";
    public static final String MESSAGE_TYPE_JETCO = "J";
    public static final String MESSAGE_TYPE_ATMC = "A";
    public static final String MESSAGE_TYPE_EPSCO = "E";

}
