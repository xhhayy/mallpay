package com.imooc.mall.delay;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 订单延迟任务
 * 实现Delayed接口，用于DelayQueue
 */
public class OrderDelay implements Delayed {
    private final Long orderId;          // 订单ID
    private final long expireTime;       // 过期时间
    private int retryCount;              // 重试次数
    private final long initialDelay;     // 初始延迟时间

    /**
     * 构造方法
     * @param orderId 订单ID
     * @param delayTime 延迟时间（毫秒）
     */
    public OrderDelay(Long orderId, long delayTime) {
        this.orderId = orderId;
        this.expireTime = System.currentTimeMillis() + delayTime;
        this.retryCount = 0;
        this.initialDelay = delayTime;
    }

    /**
     * 构造方法（用于重试）
     * @param orderId 订单ID
     * @param delayTime 延迟时间（毫秒）
     * @param retryCount 重试次数
     */
    public OrderDelay(Long orderId, long delayTime, int retryCount) {
        this.orderId = orderId;
        this.expireTime = System.currentTimeMillis() + delayTime;
        this.retryCount = retryCount;
        this.initialDelay = delayTime;
    }

    public Long getOrderId() {
        return orderId;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    /**
     * 增加重试次数
     * @return 新的OrderDelay实例
     */
    public OrderDelay incrementRetryCount() {
        // 指数退避策略：重试间隔翻倍
        long newDelay = initialDelay * (1 << retryCount);
        // 最大重试间隔限制为5分钟
        newDelay = Math.min(newDelay, 5 * 60 * 1000);
        return new OrderDelay(orderId, newDelay, retryCount + 1);
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