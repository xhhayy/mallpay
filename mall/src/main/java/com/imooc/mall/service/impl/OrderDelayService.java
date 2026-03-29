package com.imooc.mall.service.impl;

import com.imooc.mall.delay.OrderDelay;
import com.imooc.mall.service.IOrderService;
import com.imooc.mall.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单延迟队列服务
 * 处理超时订单自动取消
 */
@Service
@Slf4j
public class OrderDelayService {
    // 内存延迟队列
    private final DelayQueue<OrderDelay> delayQueue = new DelayQueue<>();
    // 订单ID到OrderDelay的映射，用于快速移除任务
    private final ConcurrentMap<Long, OrderDelay> orderDelayMap = new ConcurrentHashMap<>();
    // 线程池，用于处理取消订单任务
    private final ExecutorService executorService;
    // 最大重试次数
    @Value("${order.cancel.max-retry:3}")
    private int maxRetryCount;
    // Redis键前缀
    private static final String ORDER_DELAY_KEY_PREFIX = "order:delay:";

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 构造方法
     * 初始化线程池
     */
    public OrderDelayService() {
        // 核心线程数：CPU核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        // 最大线程数：核心线程数的2倍
        int maxPoolSize = corePoolSize * 2;
        // 线程池
        this.executorService = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "order-delay-worker-" + threadNumber.getAndIncrement());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：由调用者线程执行
        );
    }

    /**
     * 初始化方法
     * 启动延迟队列处理线程
     * 从Redis加载未处理的延迟任务
     */
    @PostConstruct
    public void init() {
        // 从Redis加载未处理的延迟任务
        loadTasksFromRedis();

        // 启动处理线程
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 从延迟队列中获取过期任务
                    OrderDelay item = delayQueue.take();
                    Long orderId = item.getOrderId();
                    
                    // 从映射中移除
                    orderDelayMap.remove(orderId);
                    // 从Redis中移除
                    redisUtil.delete(ORDER_DELAY_KEY_PREFIX + orderId);
                    
                    // 提交到线程池处理
                    executorService.submit(() -> {
                        processOrderCancel(item);
                    });
                } catch (InterruptedException e) {
                    log.error("延迟队列处理线程被中断", e);
                    Thread.currentThread().interrupt();
                }
            }
        }, "order-delay-processor").start();

        log.info("订单延迟队列服务初始化完成");
    }

    /**
     * 从Redis加载未处理的延迟任务
     */
    private void loadTasksFromRedis() {
        try {
            // 这里简化处理，实际项目中可能需要使用Redis的SCAN命令扫描所有相关键
            // 或者使用Redis的Set存储所有延迟任务的键
            log.info("从Redis加载未处理的延迟任务");
            // 实际实现需要根据具体的Redis使用方式调整
        } catch (Exception e) {
            log.error("从Redis加载延迟任务失败", e);
        }
    }

    /**
     * 添加订单到延迟队列
     * @param orderId 订单ID
     * @param delayTime 延迟时间（毫秒）
     */
    public void addOrderToDelayQueue(Long orderId, long delayTime) {
        try {
            // 创建延迟任务
            OrderDelay item = new OrderDelay(orderId, delayTime);
            
            // 添加到内存队列
            delayQueue.add(item);
            // 添加到映射
            orderDelayMap.put(orderId, item);
            // 持久化到Redis
            redisUtil.set(ORDER_DELAY_KEY_PREFIX + orderId, item, delayTime / 1000);
            
            log.info("订单 {} 已添加到延迟队列，延迟时间: {}ms", orderId, delayTime);
        } catch (Exception e) {
            log.error("添加订单到延迟队列失败，orderId: {}", orderId, e);
        }
    }

    /**
     * 从延迟队列中移除订单
     * @param orderId 订单ID
     */
    public void removeOrderFromDelayQueue(Long orderId) {
        try {
            // 从映射中获取并移除
            OrderDelay item = orderDelayMap.remove(orderId);
            if (item != null) {
                // 从延迟队列中移除
                delayQueue.remove(item);
                // 从Redis中移除
                redisUtil.delete(ORDER_DELAY_KEY_PREFIX + orderId);
                log.info("订单 {} 已从延迟队列中移除", orderId);
            }
        } catch (Exception e) {
            log.error("从延迟队列中移除订单失败，orderId: {}", orderId, e);
        }
    }

    /**
     * 处理订单取消
     * @param item 延迟任务
     */
    private void processOrderCancel(OrderDelay item) {
        Long orderId = item.getOrderId();
        int retryCount = item.getRetryCount();
        
        try {
            log.info("处理订单取消，orderId: {}, 重试次数: {}", orderId, retryCount);
            
            // 调用订单服务取消订单
            orderService.cancelOrder(orderId);
            
            log.info("订单取消成功，orderId: {}", orderId);
        } catch (Exception e) {
            log.error("订单取消失败，orderId: {}, 重试次数: {}", orderId, retryCount, e);
            
            // 检查是否达到最大重试次数
            if (retryCount < maxRetryCount) {
                // 增加重试次数，重新加入队列
                OrderDelay newItem = item.incrementRetryCount();
                delayQueue.add(newItem);
                orderDelayMap.put(orderId, newItem);
                // 持久化到Redis
                redisUtil.set(ORDER_DELAY_KEY_PREFIX + orderId, newItem, newItem.getInitialDelay() / 1000);
                
                log.info("订单取消失败，已重新加入延迟队列，orderId: {}, 下次重试时间: {}ms", 
                        orderId, newItem.getInitialDelay());
            } else {
                // 达到最大重试次数，记录失败
                log.error("订单取消达到最大重试次数，orderId: {}", orderId);
                // 可以考虑发送告警或记录到失败表
            }
        }
    }

    /**
     * 销毁方法
     * 关闭线程池
     */
    @PreDestroy
    public void destroy() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("订单延迟队列服务线程池已关闭");
        }
    }

    /**
     * 获取延迟队列大小
     * @return 队列大小
     */
    public int getQueueSize() {
        return delayQueue.size();
    }

    /**
     * 获取线程池状态
     * @return 线程池状态信息
     */
    public Map<String, Object> getThreadPoolStatus() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) executorService;
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("corePoolSize", executor.getCorePoolSize());
        status.put("maximumPoolSize", executor.getMaximumPoolSize());
        status.put("activeCount", executor.getActiveCount());
        status.put("completedTaskCount", executor.getCompletedTaskCount());
        status.put("queueSize", executor.getQueue().size());
        return status;
    }
}