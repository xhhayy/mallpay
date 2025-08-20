package com.imooc.mall.from;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/25 12:09
 */
@Data
public class ShippingFrom {
    @NotBlank
    private String receiverName;
    private String receiverPhone;
    @NotBlank
    private String receiverMobile;
    @NotBlank
    private String receiverProvince;
    @NotBlank
    private String receiverCity;
    @NotBlank
    private String receiverDistrict;
    @NotBlank
    private String receiverAddress;
    @NotBlank
    private String receiverZip;

}
