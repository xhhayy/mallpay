package com.imooc.pay.service;

import com.imooc.pay.pojo.PayInfo;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;

import java.math.BigDecimal;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/21 10:00
 */
public interface IPayService {
    /**
     * 创建/发起支付
     */
    PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum);

    /**
     * 异步通知处理
     * @param notifyData
     * async：异步
     */
    String asyncNotify(String notifyData);
    /**
     * 查询支付记录（通过订单号）
     * @param orderId
     */
    PayInfo queryByOrderId(String orderId);
}
