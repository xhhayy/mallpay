package com.imooc.pay.controller;

import com.google.gson.Gson;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
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
     * 为前端 Vue 项目提供的 JSON 接口（模拟支付模式）
     * POST /pay/create?orderNo=xxx&amount=xxx
     */
    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createForJson(@RequestParam("orderNo") String orderNo,
                                             @RequestParam(value = "amount", required = false) BigDecimal amount) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 如果前端没传金额，查询支付记录
            if (amount == null) {
                PayInfo existPayInfo = payServiceImpl.queryByOrderId(orderNo);
                if (existPayInfo != null && existPayInfo.getPayAmount() != null) {
                    amount = existPayInfo.getPayAmount();
                    log.info("使用已有支付记录的金额: {}", amount);
                } else {
                    log.warn("未提供金额且无支付记录，使用默认值 0.01");
                    amount = new BigDecimal("0.01");
                }
            }
            
            // 检查支付记录是否存在，不存在则创建
            PayInfo payInfo = payServiceImpl.queryByOrderId(orderNo);
            if (payInfo == null) {
                // 创建支付记录
                payInfo = new PayInfo(
                    Long.parseLong(orderNo),
                    2,  // 微信支付
                    "NOTPAY",  // 初始状态为未支付
                    amount
                );
                payServiceImpl.createPayInfo(payInfo);
                log.info("创建支付记录: orderNo={}, amount={}", orderNo, amount);
            }
            
            // 模拟支付模式：不调用真实微信接口，直接返回模拟数据
            result.put("code", 0);
            result.put("msg", "成功");
            Map<String, Object> data = new HashMap<>();
            data.put("codeUrl", "MOCK_PAYMENT"); // 标记为模拟支付
            data.put("orderId", orderNo);
            data.put("amount", amount.toString());
            result.put("data", data);
            
            log.info("创建模拟支付成功, orderNo={}, amount={}", orderNo, amount);
        } catch (Exception e) {
            log.error("创建支付失败", e);
            result.put("code", -1);
            result.put("msg", "创建支付失败: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * 从 mall 服务查询订单金额
     */
    private BigDecimal getOrderAmountFromMall(String orderNo) {
        try {
            String mallUrl = "http://localhost:9090/orders/" + orderNo;
            URL url = new URL(mallUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // 解析 JSON 响应
                Gson gson = new Gson();
                Map<String, Object> responseMap = gson.fromJson(response.toString(), Map.class);
                if (responseMap != null && responseMap.get("Data") != null) {
                    Map<String, Object> data = (Map<String, Object>) responseMap.get("Data");
                    Object payment = data.get("payment");
                    if (payment != null) {
                        return new BigDecimal(payment.toString());
                    }
                }
            }
            log.warn("从 mall 服务查询订单失败，响应码: {}", responseCode);
        } catch (Exception e) {
            log.error("调用 mall 服务异常", e);
        }
        return null;
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
    
    /**
     * 模拟支付成功（仅用于测试）
     * POST /pay/mockPaySuccess?orderNo=xxx
     */
    @PostMapping("/mockPaySuccess")
    @ResponseBody
    public Map<String, Object> mockPaySuccess(@RequestParam("orderNo") String orderNo) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("模拟支付成功, orderNo={}", orderNo);
            
            // 查询支付记录
            PayInfo payInfo = payServiceImpl.queryByOrderId(orderNo);
            if (payInfo == null) {
                result.put("code", -1);
                result.put("msg", "支付记录不存在");
                return result;
            }
            
            // 更新支付状态
            if (!"SUCCESS".equals(payInfo.getPlatformStatus())) {
                payInfo.setPlatformStatus("SUCCESS");
                payInfo.setPlatformNumber("MOCK_" + System.currentTimeMillis());
                payServiceImpl.updatePayInfo(payInfo);
                
                log.info("支付状态已更新为 SUCCESS");
            }
            
            result.put("code", 0);
            result.put("msg", "成功");
            result.put("data", payInfo);
        } catch (Exception e) {
            log.error("模拟支付失败", e);
            result.put("code", -1);
            result.put("msg", "操作失败: " + e.getMessage());
        }
        return result;
    }
}

