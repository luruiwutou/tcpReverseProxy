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
        if (list == null || list.size() == 0) return null;
        if (list.size() == 1) return list.get(0);
        log.info("轮询index值：{}", index);
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