package com.forward.core.proxy.util;

import com.forward.core.proxy.myClient.MyClient;
import org.springframework.stereotype.Component;

@Component
public class SimpleClassUtil {

    private static MyClient myClient;

    public static MyClient getMyClientInstance(){
        if (myClient == null){
            synchronized (SimpleClassUtil.class){
                if (myClient == null){
                    myClient = (MyClient) SpringContextUtil.getBean("myClient");
                }
            }
        }
        return myClient;
    }
}
