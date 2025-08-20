package com.imooc.pay.service.impl;

import com.imooc.pay.PayApplicationTests;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import org.junit.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;



/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/21 12:42
 */
public class PayServiceImplTest extends PayApplicationTests {
    @Autowired
    private PayServiceImpl payServiceImpl;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Test
    public void create() {
        payServiceImpl.create("1234567891434", BigDecimal.valueOf(0.01),BestPayTypeEnum.WXPAY_NATIVE);
    }

    @Test
    public void sendMQMsg(){
        amqpTemplate.convertAndSend("payNotify","我不好");
    }
}