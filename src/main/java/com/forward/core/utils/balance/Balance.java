package com.forward.core.utils.balance;

import java.util.List;

/**
 * 轮询算法接口
 */
public interface Balance<T> {

    T chooseOne(List<T> list);
}
 