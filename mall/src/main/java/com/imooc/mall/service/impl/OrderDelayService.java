package com.imooc.mall.service.impl;

import com.imooc.mall.delay.OrderDelay;
import com.imooc.mall.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.DelayQueue;

@Service
public class OrderDelayService {
    private final DelayQueue<OrderDelay> delayQueue = new DelayQueue<>();

    @Autowired
    private IOrderService orderService;

    @PostConstruct
    public void init() {
        // 启动处理线程
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    OrderDelay item = delayQueue.take();
                    Long orderId = item.getOrderId();
                    // 取消超时订单
                    orderService.cancelOrder(orderId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public void addOrderToDelayQueue(Long orderId, long delayTime) {
        OrderDelay item = new OrderDelay(orderId, delayTime);
        delayQueue.add(item);
    }
}