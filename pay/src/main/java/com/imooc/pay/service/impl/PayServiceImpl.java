package com.imooc.pay.service.impl;

import com.google.gson.Gson;
import com.imooc.pay.dao.PayInfoMapper;
import com.imooc.pay.pojo.PayInfo;
import com.imooc.pay.service.IPayService;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/21 10:03
 */
@Slf4j
@Service
public class PayServiceImpl implements IPayService {
    private final static String QUEUE_PAY_NOTIFY = "payNotify";
    @Autowired
    private BestPayService bestPayService;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired//用来发送消息的对象  MQ消息
    private AmqpTemplate amqpTemplate;
    /**
     * 创建/发起支付
     */
    @Override
    public PayResponse create(String orderId, BigDecimal amount,BestPayTypeEnum bestPayTypeEnum) {
        //把支付订单写进数据库
        PayInfo payInfo = new PayInfo(Long.parseLong(orderId),
                2,
                OrderStatusEnum.NOTPAY.name(),
                amount);
        payInfoMapper.insertSelective(payInfo);

        PayRequest request = new PayRequest();
        request.setPayTypeEnum(BestPayTypeEnum.WXPAY_NATIVE);//支付方式
        request.setOrderId(orderId);//订单id
        request.setOrderName("小浣浣的订单");//订单名字
        request.setOrderAmount(amount.doubleValue());//订单金额

        PayResponse response = bestPayService.pay(request);
        log.info("response={}",response);
        return response;
    }
    /**
     * 异步通知处理
     */
    @Override
    public String asyncNotify(String notifyData) {
        //1.签名校验、
        /**payResponse是微信异步通知传来的结果
         * 因为notify_url必须是以https://或http://开头的完整全路径地址，并且确保URL中的域名和IP是外网可以访问的，
         * 所以接收不到微信发来的异步通知
         */
        PayResponse payResponse = bestPayService.asyncNotify(notifyData);
        log.info("异步通知 payResponse", payResponse);
        //2.金额校验（从数据库查订单）
        //比较严重（正常情况下不会发生）发出警告：钉钉，短信
        PayInfo payInfo = payInfoMapper.selectByOrderNo(Long.parseLong(payResponse.getOrderId()));
        if(payInfo == null){
            //告警
            throw new RuntimeException("通过查询orderNo的结果是null");
        }
        //
        if(! payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())){
            //Double类型不好比较，精度问题
            if(payInfo.getPayAmount().compareTo(BigDecimal.valueOf(payResponse.getOrderAmount())) != 0){
                throw new RuntimeException("微信异步通知的金额与数据库中的金额不一致orderNo="+payResponse.getOrderId());
            }
            //3.修改订单支付状态
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            payInfo.setPlatformNumber(payResponse.getOutTradeNo());//获取微信异步通知中的流水号
            payInfoMapper.updateByPrimaryKeySelective(payInfo);
        }

        //TODO pay发送MQ消息，mall接收MQ消息
        amqpTemplate.convertAndSend(QUEUE_PAY_NOTIFY, new Gson().toJson(payInfo));



        if(payResponse.getPayPlatformEnum() == BestPayPlatformEnum.WX){
            //4.告诉微信不要通知了
            return "<xml>\n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";
        }else if(payResponse.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY){
            //告诉支付宝不要通知了，暂时还没有实现支付宝的功能
            return "success";
        }else
            throw new RuntimeException("支付平台错误");
    }
    /**
     * 查询支付记录（通过订单号）
     */
    @Override
    public PayInfo queryByOrderId(String orderId) {
        return payInfoMapper.selectByOrderNo(Long.parseLong(orderId));
    }
}
