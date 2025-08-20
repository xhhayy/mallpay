package com.imooc.mall.delay;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class OrderDelay implements Delayed {
    private final Long orderId;
    private final long expireTime;

    public OrderDelay(Long orderId, long delayTime) {
        this.orderId = orderId;
        this.expireTime = System.currentTimeMillis() + delayTime;
    }

    public Long getOrderId() {
        return orderId;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = expireTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        return Long.compare(this.expireTime, ((OrderDelay) other).expireTime);
    }
}