package com.imooc.mall.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/25 11:26
 */
@Data
public class ShippingVo {
    private Integer id;

    private Integer userId;

    private String receiverName;

    private String receiverPhone;

    private String receiverMobile;

    private String receiverProvince;

    private String receiverCity;

    private String receiverDistrict;

    private String receiverAddress;

    private String receiverZip;

    private Date createTime;

    private Date updateTime;
}
