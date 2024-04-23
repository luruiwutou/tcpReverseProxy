package com.forward.core.tcpReverseProxy.utils.balance;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 顺序
 *
 * @param <T>
 */
@Slf4j
public class QueueBalance<T> implements Balance<T> {

    private volatile int index = 0;

    public synchronized T chooseOne(List<T> list) {
        log.info("轮询index值：{}", index);
        if (list == null || list.size() == 0) return null;
        int sum = list.size();
        int temp = index % sum;
        T t = list.get(temp);
        if (index < Integer.MAX_VALUE)
            index++;
        else
            index = 0;
        return t;
    }


}