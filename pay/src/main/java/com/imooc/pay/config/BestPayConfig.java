package com.imooc.pay.config;

import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.service.BestPayService;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/21 21:56
 */
@Component
public class BestPayConfig {
    @Autowired
    private WxAccountConfig wxAccountConfig;
    @Bean//配置微信native支付参数
    public WxPayConfig wxPayConfig() {
        WxPayConfig wxPayConfig = new WxPayConfig();
        wxPayConfig.setAppId(wxAccountConfig.getAppId());//公众号appId
        wxPayConfig.setMchId(wxAccountConfig.getMchId());//商户号
        wxPayConfig.setMchKey(wxAccountConfig.getMchKey());//商户密钥
        wxPayConfig.setNotifyUrl(wxAccountConfig.getNotifyUrl());//微信异步通知地址
        wxPayConfig.setReturnUrl(wxAccountConfig.getReturnUrl());//支付后返回地址
        return wxPayConfig;
    }
    @Bean
    public BestPayService bestPayService(WxPayConfig wxPayConfig) {
        BestPayServiceImpl bestPayService = new BestPayServiceImpl();
        bestPayService.setWxPayConfig(wxPayConfig);
        return bestPayService;
    }
}

