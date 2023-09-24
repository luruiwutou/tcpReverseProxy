package com.forward.core.constant;

import io.netty.util.AttributeKey;

public class Constants {
    /**
     * 重新加载环境锁key
     */
    public static final String RELOAD_EMN_KEY = "RELOAD_EMN_KEY";
    /**
     *
     */
    public static final String LOCAL_PORT_RULE_SINGLE = "-";

    public static final String MYBATIS_LOCAL_CLIENT_PORT_SPLIT_REGEX = "&";
    public static final String COMMA = ",";
    public static final String COLON = ":";
    public static final String UNDERLINE = "_";
    public static final String ASTERISK = "*";
    /**
     * MDC中键名traceId
     */
    public static final String TRACE_ID = "traceId";
    public static final String SERVER = "SERVER";
    public static final String CLIENT = "CLIENT";
    /**
     *
     */
    public static final String DEFAULT_FIELD_LENGTH_KEY = "DEFAULT_FIELD_LENGTH_KEY";
    public static final String PROXY_CLIENT_OPEN_SSL = "PROXY_CLIENT_OPEN_SSL";
    public static final String PROXY_SERVER_OPEN_SSL = "PROXY_SERVER_OPEN_SSL";
    public static final String TRUE_STR = "TRUE";
    public static final String FALSE_STR = "FALSE";
    public static final String PATH_SSL_TSL_PEM_PATH= "PATH_SSL_TSL_PEM_PATH";
    public static final String PATH_SSL_TSL_KEY_PATH= "PATH_SSL_TSL_KEY_PATH";
    public static final String PATH_SSL_TSL_CERT_PATH = "PATH_SSL_TSL_CERT_PATH";
    public static final AttributeKey<String> TRACE_ID_KEY = AttributeKey.valueOf(Constants.TRACE_ID);
//    public static final String[] DEFAULT_FIELD_LENGTH_VALUE=new String[]{"10240", "0",  "4" , "0","0"};
}
