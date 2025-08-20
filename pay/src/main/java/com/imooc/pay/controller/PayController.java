package com.imooc.pay.controller;

import com.imooc.pay.pojo.PayInfo;
import com.imooc.pay.service.impl.PayServiceImpl;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;



/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/21 16:19
 */
@Slf4j
@Controller
@RequestMapping("/pay")
public class PayController {
    @Autowired
    private PayServiceImpl payServiceImpl;
    @Autowired
    private WxPayConfig wxPayConfig;
    @GetMapping("/create")
    /**
     * 创建订单
     */
    public ModelAndView create(@RequestParam("orderId") String orderId,
                               @RequestParam("amount") BigDecimal amount,
                               @RequestParam("payType") BestPayTypeEnum bestPayTypeEnum
                               ) {
        PayResponse response = payServiceImpl.create(orderId, amount, bestPayTypeEnum);
        //支付方式不同，渲染就不同， WXPAY_NATIVA使用codeUrl, ALIPAY_PC使用body
        Map<String, String> map = new HashMap<>();
        if (bestPayTypeEnum == BestPayTypeEnum.WXPAY_NATIVE) {
            map.put("codeUrl", response.getCodeUrl());
            map.put("orderId",orderId);
            map.put("returnUrl",wxPayConfig.getReturnUrl());
            return new ModelAndView("createForWxNative", map);
        } else if (bestPayTypeEnum == BestPayTypeEnum.WXPAY_NATIVE) {
            map.put("body",response.getBody());
            return new ModelAndView("createForAlipayPc", map);
        }
            throw new RuntimeException("暂不支持的支付类型");
    }

    /**
     * 异步通知，接受微信返回的通知（post请求）
     */
    @PostMapping("/notify")
    @ResponseBody
    public String asyncNotify(@RequestBody String notifyData){
        return payServiceImpl.asyncNotify(notifyData);
    }

    /**
     *查询支付记录
     */
    @GetMapping("/queryByOrderId")
    @ResponseBody
    public PayInfo queryByOrderId(@RequestParam String orderId){
        log.info("查询支付记录。。。");
        return payServiceImpl.queryByOrderId(orderId);
    }

    /**
     *支付成功返回地址
     */
    @GetMapping("/payReturn")
    public ModelAndView payReturn(){
        return new ModelAndView("payReturn");
    }
}

