package com.forward.core.tcpReverseProxy.utils.balance;

import java.util.List;
import java.util.Random;

/**
 * 随机
 * @param <T>
 */
public class RandomBalance<T> implements Balance<T> {

    private Random random = new Random();

    @Override
    public T chooseOne(List<T> list) {
        int sum = list.size();
        return list.get(random.nextInt(sum));
    }
}