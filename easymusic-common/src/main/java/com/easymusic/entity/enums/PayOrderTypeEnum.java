package com.easymusic.entity.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author cyt
 * * @date 2025/11/20 15:42:44
 */

// 支付方式枚举
public enum PayOrderTypeEnum {

    PAY_CODE(0, "", "付款码支付"),
    PAY_WECHAT(1, "payChannel4Wechat", "微信支付"),
    PAY_ALIPAY(2, "payChannel4Alipay", "支付宝支付");


    private Integer type;
    private String beanName;
    private String desc;

    PayOrderTypeEnum(Integer status, String beanName, String desc) {
        this.type = status;
        this.beanName = beanName;
        this.desc = desc;
    }


    public Integer getType() {
        return type;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getDesc() {
        return desc;
    }

    public static PayOrderTypeEnum getByType(Integer type) {
        Optional<PayOrderTypeEnum> typeEnum = Arrays.stream(PayOrderTypeEnum.values())
                .filter(value -> value.getType().equals(type)).findFirst();
        return typeEnum.orElse(null);
    }
}
